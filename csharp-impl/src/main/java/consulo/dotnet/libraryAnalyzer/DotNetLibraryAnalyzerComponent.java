/*
 * Copyright 2013-2017 consulo.io
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

package consulo.dotnet.libraryAnalyzer;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.messages.MessageBusConnection;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.ide.eap.EarlyAccessProgramDescriptor;
import consulo.ide.eap.EarlyAccessProgramManager;
import consulo.internal.dotnet.asm.mbel.ModuleParser;
import consulo.internal.dotnet.asm.mbel.TypeDef;
import consulo.internal.dotnet.asm.parse.MSILParseException;
import consulo.internal.dotnet.msil.decompiler.util.MsilHelper;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.ModuleExtensionChangeListener;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * @author VISTALL
 * @since 06.12.14
 */
@Singleton
public class DotNetLibraryAnalyzerComponent
{
	public static class EapDescriptor extends EarlyAccessProgramDescriptor
	{
		@Nonnull
		@Override
		public String getName()
		{
			return "C#: support adding external library via using fix";
		}

		@Nonnull
		@Override
		public String getDescription()
		{
			return getName();
		}

		@Override
		public boolean isAvailable()
		{
			return false;
		}
	}

	@Nonnull
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
	//private final Map<Module, MultiMap<String, NamespaceReference>> myCacheMap = new ConcurrentWeakKeyHashMap<Module, MultiMap<String, NamespaceReference>>();

	@Inject
	public DotNetLibraryAnalyzerComponent(Project project)
	{
		if(!EarlyAccessProgramManager.is(EapDescriptor.class))
		{
			return;
		}

		MessageBusConnection connect = project.getMessageBus().connect();

		connect.subscribe(DumbService.DUMB_MODE, new DumbService.DumbModeListener()
		{
			@Override
			public void enteredDumbMode()
			{
				Module[] modules = ModuleManager.getInstance(project).getModules();
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
			public void beforeExtensionChanged(@Nonnull ModuleExtension<?> moduleExtension, @Nonnull final ModuleExtension<?> moduleExtension2)
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

	private void runAnalyzerFor(@Nonnull final DotNetSimpleModuleExtension<?> extension)
	{
		/*new Task.Backgroundable(extension.getProject(), "Analyzing .NET libraries for module: " + extension.getModule().getName())
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

			@NotNull
			@Override
			public DumbModeAction getDumbModeAction()
			{
				return DumbModeAction.WAIT;
			}
		}.queue(); */
	}

	private static MultiMap<String, NamespaceReference> buildCache(File key, String libraryName)
	{
		try
		{
			MultiMap<String, NamespaceReference> map = new MultiMap<String, NamespaceReference>();
			TypeDef[] typeDefs = new ModuleParser(key).getTypeDefs();

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
		catch(IOException | MSILParseException ignored)
		{
		}
		return MultiMap.emptyInstance();
	}

	/**
	 * @return couple library + namespace
	 */
	@Nonnull
	public Collection<NamespaceReference> get(@Nonnull Module module, @Nonnull String typeName)
	{
		/*MultiMap<String, NamespaceReference> map = myCacheMap.get(module);
		if(map == null)
		{
			return Collections.emptyList();
		}
		return map.get(typeName); */
		return Collections.emptyList();
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	public Collection<NamespaceReference> getAll(@Nonnull Module module)
	{
		/*MultiMap<String, NamespaceReference> map = myCacheMap.get(module);
		if(map == null)
		{
			return Collections.emptyList();
		}
		return (Collection<NamespaceReference>) map.values();  */
		return Collections.emptyList();
	}
}
