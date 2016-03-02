/*
 * Copyright 2013-2016 must-be.org
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

package org.mustbe.consulo.csharp.lang;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.impl.FilePropertyPusher;
import com.intellij.openapi.roots.impl.PushedFilePropertiesUpdater;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.FileAttribute;
import com.intellij.util.io.DataInputOutputUtil;
import com.intellij.util.messages.MessageBus;

/**
 * @author VISTALL
 * @since 02.03.2016
 */
public class CSharpFilePropertyPusher implements FilePropertyPusher<List<String>>
{
	public static final Key<List<String>> ourKey = Key.create("csharp.virtual.file.preprocessor.defines");
	private static final FileAttribute ourFileAttribute = new FileAttribute("file_preprocessor_defines", 1, false);

	@Override
	public void initExtra(@NotNull Project project, @NotNull MessageBus bus, @NotNull Engine languageLevelUpdater)
	{
	}

	@NotNull
	@Override
	public Key<List<String>> getFileDataKey()
	{
		return ourKey;
	}

	@Override
	public boolean pushDirectoriesOnly()
	{
		return false;
	}

	@NotNull
	@Override
	public List<String> getDefaultValue()
	{
		return Collections.emptyList();
	}

	@Nullable
	@Override
	public List<String> getImmediateValue(@NotNull Project project, @Nullable VirtualFile file)
	{
		if(file == null)
		{
			return Collections.emptyList();
		}
		Module moduleForFile = ModuleUtilCore.findModuleForFile(file, project);
		if(moduleForFile == null)
		{
			return Collections.emptyList();
		}
		return getImmediateValue(moduleForFile);
	}

	@Nullable
	@Override
	public List<String> getImmediateValue(@NotNull Module module)
	{
		DotNetSimpleModuleExtension<?> extension = ModuleUtilCore.getExtension(module, DotNetSimpleModuleExtension.class);
		if(extension != null)
		{
			return extension.getVariables();
		}
		return Collections.emptyList();
	}

	@Override
	public boolean acceptsFile(@NotNull VirtualFile file)
	{
		return file.getFileType() == CSharpFileType.INSTANCE;
	}

	@Override
	public boolean acceptsDirectory(@NotNull VirtualFile file, @NotNull Project project)
	{
		return false;
	}

	@Override
	public void persistAttribute(@NotNull final Project project, @NotNull final VirtualFile fileOrDir, @NotNull List<String> newValue) throws IOException
	{
		List<String> oldValue = null;

		final DataInputStream iStream = ourFileAttribute.readAttribute(fileOrDir);
		if(iStream != null)
		{
			try
			{
				oldValue = readList(iStream);
				if(oldValue.equals(newValue))
				{
					return;
				}
			}
			finally
			{
				iStream.close();
			}
		}

		final DataOutputStream oStream = ourFileAttribute.writeAttribute(fileOrDir);
		writeList(oStream, newValue);
		oStream.close();

		PushedFilePropertiesUpdater.getInstance(project).filePropertiesChanged(fileOrDir);
	}

	@NotNull
	private static List<String> readList(DataInputStream stream) throws IOException
	{
		int size = DataInputOutputUtil.readINT(stream);
		if(size == 0)
		{
			return Collections.emptyList();
		}
		List<String> list = new ArrayList<String>(size);
		for(int j = 0; j < size; j++)
		{
			list.add(stream.readUTF());
		}
		return list;
	}

	private static void writeList(DataOutputStream stream, List<String> list) throws IOException
	{
		DataInputOutputUtil.writeINT(stream, list.size());
		if(list.isEmpty())
		{
			return;
		}
		for(String var : list)
		{
			stream.writeUTF(var);
		}
	}

	@Override
	public void afterRootsChanged(@NotNull Project project)
	{
		PushedFilePropertiesUpdater.getInstance(project).pushAll(this);
	}
}
