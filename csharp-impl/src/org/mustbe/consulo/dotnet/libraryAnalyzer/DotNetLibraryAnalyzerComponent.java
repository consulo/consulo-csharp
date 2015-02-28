/*
 * Copyright 2013-2014 must-be.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mustbe.consulo.dotnet.libraryAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.consulo.ide.eap.EarlyAccessProgramDescriptor;
import org.consulo.ide.eap.EarlyAccessProgramManager;
import org.consulo.module.extension.ModuleExtension;
import org.consulo.module.extension.ModuleExtensionChangeListener;
import org.jboss.netty.util.internal.ConcurrentWeakKeyHashMap;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.lang.psi.impl.stub.MsilHelper;
import org.mustbe.consulo.dotnet.module.extension.DotNetLibraryOpenCache;
import org.mustbe.consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbModeAction;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.types.BinariesOrderRootType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.messages.MessageBusConnection;
import edu.arizona.cs.mbel.mbel.ModuleParser;
import edu.arizona.cs.mbel.mbel.TypeDef;
import edu.arizona.cs.mbel.parse.MSILParseException;

/**
 * @author VISTALL
 * @since 06.12.14
 */
public class DotNetLibraryAnalyzerComponent extends AbstractProjectComponent
{
	public static class EapDescriptor extends EarlyAccessProgramDescriptor
	{
		@NotNull
		@Override
		public String getName()
		{
			return "C#: support adding external library via using fix";
		}

		@NotNull
		@Override
		public String getDescription()
		{
			return getName();
		}
	}

	@NotNull
	public static DotNetLibraryAnalyzerComponent getInstance(Project project)
	{
		return project.getComponent(DotNetLibraryAnalyzerComponent.class);
	}

	/**
	 * Map of
	 * <p/>
	 * key - File
	 * <p/>
	 * value -
	 * key - typeName
	 * value
	 * - p1 - libraryName
	 * - p2 - namespace
	 */
	private final Map<Module, MultiMap<String, NamespaceReference>> myCacheMap = new ConcurrentWeakKeyHashMap<Module, MultiMap<String,
			NamespaceReference>>();

	public DotNetLibraryAnalyzerComponent(Project project)
	{
		super(project);
	}

	@Override
	public void initComponent()
	{
		if(!EarlyAccessProgramManager.is(EapDescriptor.class))
		{
			return;
		}

		MessageBusConnection connect = myProject.getMessageBus().connect();

		connect.subscribe(DumbService.DUMB_MODE, new DumbService.DumbModeListener()
		{
			@Override
			public void enteredDumbMode()
			{
				Module[] modules = ModuleManager.getInstance(myProject).getModules();
				for(Module module : modules)
				{
					DotNetSimpleModuleExtension extension = ModuleUtilCore.getExtension(module, DotNetSimpleModuleExtension.class);
					if(extension == null)
					{
						continue;
					}
					runAnalyzerFor(extension);
				}
			}

			@Override
			public void exitDumbMode()
			{

			}
		});

		connect.subscribe(ModuleExtension.CHANGE_TOPIC, new ModuleExtensionChangeListener()
		{
			@Override
			public void beforeExtensionChanged(@NotNull ModuleExtension<?> moduleExtension, @NotNull final ModuleExtension<?> moduleExtension2)
			{
				if(moduleExtension2 instanceof DotNetSimpleModuleExtension && moduleExtension2.isEnabled())
				{
					ApplicationManager.getApplication().invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							runAnalyzerFor((DotNetSimpleModuleExtension) moduleExtension2);
						}
					});
				}
			}
		});

	}

	private void runAnalyzerFor(@NotNull final DotNetSimpleModuleExtension<?> extension)
	{
		new Task.Backgroundable(extension.getProject(), "Analyzing .NET libraries for module: " + extension.getModule().getName())
		{
			@Override
			public void run(@NotNull ProgressIndicator indicator)
			{
				myCacheMap.remove(extension.getModule());

				Map<String, String> availableSystemLibraries = extension.getAvailableSystemLibraries();
				if(availableSystemLibraries.isEmpty())
				{
					return;
				}

				MultiMap<String, NamespaceReference> map = new MultiMap<String, NamespaceReference>();
				for(String libraryName : availableSystemLibraries.keySet())
				{
					String[] systemLibraryUrls = extension.getSystemLibraryUrls(libraryName, BinariesOrderRootType.getInstance());
					if(systemLibraryUrls.length == 0)
					{
						continue;
					}

					String libraryPath = PathUtil.toPresentableUrl(systemLibraryUrls[0]);
					File availableSystemLibrary = new File(libraryPath);

					map.putAllValues(buildCache(availableSystemLibrary, libraryName));
				}
				myCacheMap.put(extension.getModule(), map);
			}

			@Override
			public DumbModeAction getDumbModeAction()
			{
				return DumbModeAction.WAIT;
			}
		}.queue();
	}

	private static MultiMap<String, NamespaceReference> buildCache(File key, String libraryName)
	{
		try
		{
			ModuleParser parser = DotNetLibraryOpenCache.acquireWithNext(key.getPath());

			MultiMap<String, NamespaceReference> map = new MultiMap<String, NamespaceReference>();
			TypeDef[] typeDefs = parser.getTypeDefs();

			for(TypeDef typeDef : typeDefs)
			{
				String namespace = typeDef.getNamespace();
				if(StringUtil.isEmpty(namespace))
				{
					continue;
				}
				map.putValue(MsilHelper.cutGenericMarker(typeDef.getName()), new NamespaceReference(namespace, libraryName));
			}
			return map;
		}
		catch(IOException e)
		{
		}
		catch(MSILParseException e)
		{
		}
		return MultiMap.emptyInstance();
	}

	/**
	 * @return couple library + namespace
	 */
	@NotNull
	public Collection<NamespaceReference> get(@NotNull Module module, @NotNull String typeName)
	{
		MultiMap<String, NamespaceReference> map = myCacheMap.get(module);
		if(map == null)
		{
			return Collections.emptyList();
		}
		return map.get(typeName);
	}

	@NotNull
	@SuppressWarnings("unchecked")
	public Collection<NamespaceReference> getAll(@NotNull Module module)
	{
		MultiMap<String, NamespaceReference> map = myCacheMap.get(module);
		if(map == null)
		{
			return Collections.emptyList();
		}
		return (Collection<NamespaceReference>) map.values();
	}
}
