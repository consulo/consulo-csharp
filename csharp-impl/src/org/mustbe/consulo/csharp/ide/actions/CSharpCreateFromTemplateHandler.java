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

package org.mustbe.consulo.csharp.ide.actions;

import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import org.mustbe.consulo.roots.ContentEntryFileListener;
import com.intellij.ide.fileTemplates.DefaultCreateFromTemplateHandler;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.psi.PsiDirectory;
import com.intellij.testFramework.LightVirtualFile;
import consulo.lombok.annotations.Lazy;

/**
 * @author VISTALL
 * @since 24.03.2016
 */
public class CSharpCreateFromTemplateHandler extends DefaultCreateFromTemplateHandler
{
	@NotNull
	@Lazy
	public static CSharpCreateFromTemplateHandler getInstance()
	{
		return EP_NAME.findExtension(CSharpCreateFromTemplateHandler.class);
	}

	@Nullable
	@RequiredReadAction
	public static Module findModuleByPsiDirectory(final PsiDirectory directory)
	{
		LightVirtualFile l = new LightVirtualFile("test.cs", CSharpFileType.INSTANCE, "")
		{
			@Override
			public VirtualFile getParent()
			{
				return directory.getVirtualFile();
			}

			@NotNull
			@Override
			public VirtualFileSystem getFileSystem()
			{
				return LocalFileSystem.getInstance();
			}
		};
		for(ContentEntryFileListener.PossibleModuleForFileResolver o : ContentEntryFileListener.PossibleModuleForFileResolver.EP_NAME.getExtensions())
		{
			Module resolve = o.resolve(directory.getProject(), l);
			if(resolve != null)
			{
				return resolve;
			}
		}
		return null;
	}

	@Override
	public boolean canCreate(PsiDirectory[] dirs)
	{
		return false;
	}

	@Override
	public boolean handlesTemplate(FileTemplate template)
	{
		FileType fileTypeByExtension = FileTypeRegistry.getInstance().getFileTypeByExtension(template.getExtension());
		return fileTypeByExtension == CSharpFileType.INSTANCE;
	}

	@Override
	@RequiredReadAction
	public void prepareProperties(Map<String, Object> props)
	{
		PsiDirectory directory = (PsiDirectory) props.get("psiDirectory");
		if(directory == null)
		{
			return;
		}

		Module module = ModuleUtilCore.findModuleForPsiElement(directory);
		if(module != null)
		{
			props.put("MODULE", module.getName());
		}
		props.put("GUID", UUID.randomUUID().toString());

		DotNetSimpleModuleExtension<?> extension = ModuleUtilCore.getExtension(directory, DotNetSimpleModuleExtension.class);
		if(extension == null)
		{
			Module moduleByPsiDirectory = findModuleByPsiDirectory(directory);
			if(moduleByPsiDirectory != null)
			{
				extension = ModuleUtilCore.getExtension(moduleByPsiDirectory, DotNetSimpleModuleExtension.class);
			}
		}

		String namespace = null;
		if(extension != null)
		{
			namespace = extension.getNamespaceGeneratePolicy().calculateNamespace(directory);
		}
		props.put("NAMESPACE_NAME", namespace);
	}
}
