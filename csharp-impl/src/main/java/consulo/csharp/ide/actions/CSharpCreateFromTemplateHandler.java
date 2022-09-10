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

package consulo.csharp.ide.actions;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.ide.refactoring.util.CSharpNameSuggesterUtil;
import consulo.csharp.lang.CSharpFileType;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.fileTemplate.CreateFromTemplateHandler;
import consulo.fileTemplate.FileTemplate;
import consulo.language.file.light.LightVirtualFile;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.util.QualifiedName;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.content.NewFileModuleResolver;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileSystem;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.virtualFileSystem.fileType.FileTypeRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author VISTALL
 * @since 24.03.2016
 */
@ExtensionImpl
public class CSharpCreateFromTemplateHandler implements CreateFromTemplateHandler
{
	@Nonnull
	public static CSharpCreateFromTemplateHandler getInstance()
	{
		return EP_NAME.findExtensionOrFail(CSharpCreateFromTemplateHandler.class);
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

			@Nonnull
			@Override
			public VirtualFileSystem getFileSystem()
			{
				return LocalFileSystem.getInstance();
			}
		};
		return directory.getProject().getExtensionPoint(NewFileModuleResolver.class).computeSafeIfAny(it -> it.resolveModule(directory.getVirtualFile(), CSharpFileType.INSTANCE));
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

		consulo.module.Module module = ModuleUtilCore.findModuleForPsiElement(directory);
		if(module != null)
		{
			props.put("MODULE", module.getName());
		}
		props.put("GUID", UUID.randomUUID().toString());

		DotNetSimpleModuleExtension<?> extension = ModuleUtilCore.getExtension(directory, DotNetSimpleModuleExtension.class);
		if(extension == null)
		{
			consulo.module.Module moduleByPsiDirectory = findModuleByPsiDirectory(directory);
			if(moduleByPsiDirectory != null)
			{
				extension = ModuleUtilCore.getExtension(moduleByPsiDirectory, DotNetSimpleModuleExtension.class);
			}
		}

		String namespace = null;
		if(extension != null)
		{
			namespace = formatNamespace(extension.getNamespaceGeneratePolicy().calculateNamespace(directory));
		}
		props.put("NAMESPACE_NAME", namespace);
	}

	@Nullable
	private static String formatNamespace(@Nullable String original)
	{
		if(StringUtil.isEmpty(original))
		{
			return null;
		}

		QualifiedName qualifiedName = QualifiedName.fromDottedString(original);

		List<String> components = qualifiedName.getComponents();

		List<String> newComponents = new ArrayList<>(components.size());
		for(String component : components)
		{
			if(CSharpNameSuggesterUtil.isKeyword(component))
			{
				newComponents.add("@" + component);
			}
			else
			{
				newComponents.add(component);
			}
		}
		return String.join(".", newComponents);
	}
}
