/*
 * Copyright 2013-2018 consulo.io
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

package consulo.csharp.lang.roots.impl;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.impl.FilePropertyPusher;
import com.intellij.openapi.roots.impl.PushedFilePropertiesUpdater;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.FileAttribute;
import com.intellij.util.messages.MessageBus;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpSimpleModuleExtension;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.module.extension.ModuleExtension;
import gnu.trove.TIntHashSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author VISTALL
 * @since 2018-03-04
 */
public class CSharpFilePropertyPusher implements FilePropertyPusher<CSharpFileAttribute>
{
	public static final Key<CSharpFileAttribute> ourCSharpFileAttributeKey = Key.create("CSharpFilePropertyPusher.PREPROCESSOR_VARIABLES");
	private static final FileAttribute ourFileAttribute = new FileAttribute("csharp-file-preprocessor-variables", 2, false);

	private static final Key<Set<String>> ourChangedModulesKey = Key.create("CSharpFilePropertyPusher.ourChangedModules");

	@Override
	public void initExtra(@Nonnull Project project, @Nonnull MessageBus messageBus)
	{
		project.getMessageBus().connect().subscribe(ModuleExtension.CHANGE_TOPIC, (oldExtension, newExtension) ->
		{
			if(oldExtension instanceof CSharpSimpleModuleExtension && newExtension instanceof CSharpSimpleModuleExtension)
			{
				if(((CSharpSimpleModuleExtension) oldExtension).getLanguageVersion() != ((CSharpSimpleModuleExtension) newExtension).getLanguageVersion())
				{
					addChanged(project, newExtension);
				}
			}

			if(oldExtension instanceof DotNetSimpleModuleExtension && newExtension instanceof DotNetSimpleModuleExtension)
			{
				if(!((DotNetSimpleModuleExtension) oldExtension).getVariables().equals(((DotNetSimpleModuleExtension) newExtension).getVariables()))
				{
					addChanged(project, newExtension);
				}
			}
		});
	}

	private void addChanged(Project project, ModuleExtension<?> newExtension)
	{
		Set<String> changedModules = project.getUserData(ourChangedModulesKey);
		if(changedModules == null)
		{
			changedModules = new HashSet<>();
		}

		project.putUserData(ourChangedModulesKey, changedModules);

		changedModules.add(newExtension.getModule().getName());
	}

	@Nonnull
	@Override
	public Key<CSharpFileAttribute> getFileDataKey()
	{
		return ourCSharpFileAttributeKey;
	}

	@Override
	public boolean pushDirectoriesOnly()
	{
		return false;
	}

	@Nonnull
	@Override
	public CSharpFileAttribute getDefaultValue()
	{
		return CSharpFileAttribute.DEFAULT;
	}

	@Nullable
	@Override
	@RequiredReadAction
	public CSharpFileAttribute getImmediateValue(@Nonnull Project project, @Nullable VirtualFile virtualFile)
	{
		if(virtualFile == null)
		{
			return CSharpFileAttribute.DEFAULT;
		}

		Module moduleForFile = ModuleUtilCore.findModuleForFile(virtualFile, project);
		return moduleForFile == null ? CSharpFileAttribute.DEFAULT : getImmediateValue(moduleForFile);
	}

	@Nullable
	@Override
	@RequiredReadAction
	public CSharpFileAttribute getImmediateValue(@Nonnull Module module)
	{
		DotNetSimpleModuleExtension<?> extension = ModuleUtilCore.getExtension(module, DotNetSimpleModuleExtension.class);
		if(extension != null)
		{
			CSharpSimpleModuleExtension csharpExtension = ModuleUtilCore.getExtension(module, CSharpSimpleModuleExtension.class);
			return new CSharpFileAttribute(csharpExtension == null ? CSharpLanguageVersion.HIGHEST : csharpExtension.getLanguageVersion(), varHashCode(extension.getVariables()));
		}
		return CSharpFileAttribute.DEFAULT;
	}

	private static int varHashCode(@Nonnull Collection<String> vars)
	{
		Set<String> sortedSet = new TreeSet<>(vars);
		TIntHashSet intSet = new TIntHashSet();
		for(String varName : sortedSet)
		{
			intSet.add(varName.hashCode());
		}
		return intSet.hashCode();
	}

	@Override
	public boolean acceptsFile(@Nonnull VirtualFile virtualFile)
	{
		return virtualFile.getFileType() == CSharpFileType.INSTANCE;
	}

	@Override
	public boolean acceptsDirectory(@Nonnull VirtualFile virtualFile, @Nonnull Project project)
	{
		return true;
	}

	@Override
	public void persistAttribute(@Nonnull Project project, @Nonnull VirtualFile virtualFile, @Nonnull CSharpFileAttribute newAttribute) throws IOException
	{
		DataInputStream inputStream = ourFileAttribute.readAttribute(virtualFile);
		if(inputStream != null)
		{
			try
			{
				CSharpFileAttribute oldAttribute = CSharpFileAttribute.read(inputStream);
				if(oldAttribute.equals(newAttribute))
				{
					return;
				}
			}
			catch(IOException e)
			{
				inputStream.close();
			}
		}

		if(newAttribute != CSharpFileAttribute.DEFAULT || inputStream != null)
		{
			try (DataOutputStream oStream = ourFileAttribute.writeAttribute(virtualFile))
			{
				CSharpFileAttribute.write(oStream, newAttribute);
			}

			PushedFilePropertiesUpdater.getInstance(project).filePropertiesChanged(virtualFile, Conditions.alwaysTrue());
		}
	}

	@Override
	public void afterRootsChanged(@Nonnull Project project)
	{
		Set<String> changedModules = project.getUserData(ourChangedModulesKey);
		if(changedModules == null)
		{
			return;
		}

		project.putUserData(ourChangedModulesKey, null);

		if(!changedModules.isEmpty())
		{
			PushedFilePropertiesUpdater.getInstance(project).pushAll(this);
		}
	}
}
