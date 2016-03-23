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

package org.mustbe.consulo.csharp.ide.actions;

import java.text.ParseException;
import java.util.Properties;
import java.util.UUID;

import org.consulo.psi.PsiPackage;
import org.consulo.psi.PsiPackageManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.CSharpIcons;
import org.mustbe.consulo.csharp.assemblyInfo.CSharpAssemblyConstants;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.module.extension.CSharpSimpleModuleExtension;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import org.mustbe.consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import org.mustbe.consulo.roots.ContentEntryFileListener;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IconDescriptor;
import com.intellij.ide.IdeView;
import com.intellij.ide.actions.CreateFileAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.actions.CreateFromTemplateAction;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public class CSharpCreateFileAction extends CreateFromTemplateAction<PsiFile>
{
	public CSharpCreateFileAction()
	{
		super(null, null, CSharpFileType.INSTANCE.getIcon());
	}

	@Override
	@RequiredDispatchThread
	protected boolean isAvailable(DataContext dataContext)
	{
		Module module = findModule(dataContext);
		if(module != null)
		{
			DotNetModuleExtension extension = ModuleUtilCore.getExtension(module, DotNetModuleExtension.class);
			if(extension != null && extension.isAllowSourceRoots())
			{
				final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
				if(view == null)
				{
					return false;
				}

				PsiDirectory orChooseDirectory = view.getOrChooseDirectory();
				if(orChooseDirectory == null)
				{
					return false;
				}
				PsiPackage aPackage = PsiPackageManager.getInstance(module.getProject()).findPackage(orChooseDirectory, DotNetModuleExtension.class);

				if(aPackage == null)
				{
					return false;
				}
			}
		}
		return module != null && ModuleUtilCore.getExtension(module, CSharpSimpleModuleExtension.class) != null;
	}

	@RequiredReadAction
	private static Module findModule(DataContext dataContext)
	{
		Project project = CommonDataKeys.PROJECT.getData(dataContext);
		assert project != null;
		final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
		if(view == null)
		{
			return null;
		}

		final PsiDirectory orChooseDirectory = view.getOrChooseDirectory();
		if(orChooseDirectory == null)
		{
			return null;
		}

		Module resolve = findModuleByPsiDirectory(project, orChooseDirectory);
		if(resolve != null)
			return resolve;
		return LangDataKeys.MODULE.getData(dataContext);
	}

	@Nullable
	@RequiredReadAction
	private static Module findModuleByPsiDirectory(Project project, final PsiDirectory orChooseDirectory)
	{
		LightVirtualFile l = new LightVirtualFile("test.cs", CSharpFileType.INSTANCE, "")
		{
			@Override
			public VirtualFile getParent()
			{
				return orChooseDirectory.getVirtualFile();
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
			Module resolve = o.resolve(project, l);
			if(resolve != null)
			{
				return resolve;
			}
		}
		return null;
	}

	@Override
	@RequiredReadAction
	protected PsiFile createFile(String name, String templateName, final PsiDirectory dir)
	{
		DotNetSimpleModuleExtension<?> extension = ModuleUtilCore.getExtension(dir, DotNetSimpleModuleExtension.class);
		if(extension == null)
		{
			Module moduleByPsiDirectory = findModuleByPsiDirectory(dir.getProject(), dir);
			if(moduleByPsiDirectory != null)
			{
				extension = ModuleUtilCore.getExtension(moduleByPsiDirectory, DotNetSimpleModuleExtension.class);
			}
		}

		String namespace = null;
		if(extension != null)
		{
			namespace = extension.getNamespaceGeneratePolicy().calculateNamespace(dir);
		}

		FileTemplate template = FileTemplateManager.getInstance().getInternalTemplate(templateName);
		return createFileFromTemplate(name, namespace, template, dir);
	}

	@SuppressWarnings("DialogTitleCapitalization")
	@Nullable
	@RequiredReadAction
	public static PsiFile createFileFromTemplate(@Nullable String name,
			@Nullable String namespaceName,
			@NotNull FileTemplate template,
			@NotNull PsiDirectory dir)
	{
		CreateFileAction.MkDirs mkdirs = new CreateFileAction.MkDirs(name, dir);
		name = mkdirs.newName;
		dir = mkdirs.directory;
		PsiElement element;
		Project project = dir.getProject();
		try
		{
			Properties defaultProperties = FileTemplateManager.getInstance().getDefaultProperties(project);
			if(!StringUtil.isEmpty(namespaceName))
			{
				defaultProperties.put("NAMESPACE_NAME", namespaceName);
			}

			if(template.getName().equals("CSharpAssemblyFile"))
			{
				Module module = ModuleUtilCore.findModuleForPsiElement(dir);
				assert module != null;
				defaultProperties.put("MODULE", module.getName());
				defaultProperties.put("GUID", UUID.randomUUID().toString());
			}

			element = FileTemplateUtil.createFromTemplate(template, name, defaultProperties, dir);
			PsiFile psiFile = element.getContainingFile();

			VirtualFile virtualFile = psiFile.getVirtualFile();
			if(virtualFile != null)
			{
				FileEditorManager.getInstance(project).openFile(virtualFile, true);

				return psiFile;
			}
		}
		catch(ParseException e)
		{
			Messages.showErrorDialog(project, "Error parsing Velocity template: " + e.getMessage(), "Create File from Template");
			return null;
		}
		catch(IncorrectOperationException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			LOG.error(e);
		}

		return null;
	}

	@Override
	@RequiredDispatchThread
	protected void buildDialog(Project project, PsiDirectory psiDirectory, CreateFileFromTemplateDialog.Builder builder)
	{
		builder.addKind("Class", new IconDescriptor(AllIcons.Nodes.Class).addLayerIcon(CSharpIcons.Lang).toIcon(), "CSharpClass");
		builder.addKind("Interface", new IconDescriptor(AllIcons.Nodes.Interface).addLayerIcon(CSharpIcons.Lang).toIcon(), "CSharpInterface");
		builder.addKind("Enum", new IconDescriptor(AllIcons.Nodes.Enum).addLayerIcon(CSharpIcons.Lang).toIcon(), "CSharpEnum");
		builder.addKind("Struct", new IconDescriptor(AllIcons.Nodes.Struct).addLayerIcon(CSharpIcons.Lang).toIcon(), "CSharpStruct");
		builder.addKind("Attribute", new IconDescriptor(AllIcons.Nodes.Attribute).addLayerIcon(CSharpIcons.Lang).toIcon(), "CSharpAttribute");
		if(isCreationOfAssemblyFileAvailable(psiDirectory))
		{
			builder.addKind("Assembly File", AllIcons.FileTypes.Config, "CSharpAssemblyFile");
		}
		builder.addKind("Empty File", CSharpFileType.INSTANCE.getIcon(), "CSharpFile");
	}

	@RequiredReadAction
	private static boolean isCreationOfAssemblyFileAvailable(PsiDirectory directory)
	{
		Module module = ModuleUtilCore.findModuleForPsiElement(directory);
		if(module != null)
		{
			DotNetModuleExtension extension = ModuleUtilCore.getExtension(module, DotNetModuleExtension.class);
			if(extension != null && extension.isAllowSourceRoots())
			{
				return false;
			}
		}
		if(module == null || ModuleUtilCore.getExtension(module, CSharpSimpleModuleExtension.class) == null)
		{
			return false;
		}

		final Ref<VirtualFile> ref = Ref.create();
		VirtualFile moduleDir = module.getModuleDir();
		if(moduleDir == null)
		{
			return false;
		}
		VfsUtil.visitChildrenRecursively(moduleDir, new VirtualFileVisitor<Object>()
		{
			@Override
			public boolean visitFile(@NotNull VirtualFile file)
			{
				if(file.getName().equals(CSharpAssemblyConstants.FileName))
				{
					ref.set(file);
					return false;
				}
				return true;
			}
		});

		return ref.get() == null;
	}

	@Override
	protected String getActionName(PsiDirectory psiDirectory, String s, String s2)
	{
		return "Create C# File";
	}
}