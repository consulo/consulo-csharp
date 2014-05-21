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

import org.consulo.psi.PsiPackage;
import org.consulo.psi.PsiPackageManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.CSharpIcons;
import org.mustbe.consulo.csharp.module.extension.BaseCSharpModuleExtension;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IconDescriptor;
import com.intellij.ide.IdeView;
import com.intellij.ide.actions.CreateFileAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.actions.CreateFromTemplateAction;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import lombok.val;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public class NewCSharpClassAction extends CreateFromTemplateAction<PsiFile>
{
	public NewCSharpClassAction()
	{
		super(null, null, new IconDescriptor(AllIcons.Nodes.Class).addLayerIcon(CSharpIcons.Lang).toIcon());
	}

	@Override
	protected boolean isAvailable(DataContext dataContext)
	{
		val module = LangDataKeys.MODULE.getData(dataContext);
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
				PsiPackage aPackage = PsiPackageManager.getInstance(module.getProject()).findPackage(orChooseDirectory,
						DotNetModuleExtension.class);

				if(aPackage == null)
				{
					return false;
				}
			}
		}
		return module != null && ModuleUtilCore.getExtension(module, BaseCSharpModuleExtension.class) != null;
	}

	@Override
	protected PsiFile createFile(String name, String templateName, PsiDirectory dir)
	{
		PsiPackage aPackage = PsiPackageManager.getInstance(dir.getProject()).findPackage(dir, DotNetModuleExtension.class);
		String namespace = null;
		if(aPackage != null)
		{
			namespace = aPackage.getQualifiedName();
		}
		else
		{
			int index = name.lastIndexOf('.');

			if(index > 0)
			{
				namespace = name.substring(0, index);
				name = name.substring(index + 1, name.length());
			}
		}

		val template = FileTemplateManager.getInstance().getInternalTemplate(templateName);
		return createFileFromTemplate(name, namespace, template, dir);
	}

	@SuppressWarnings("DialogTitleCapitalization")
	@Nullable
	public static PsiFile createFileFromTemplate(@Nullable String name, @Nullable String namespaceName, @NotNull FileTemplate template,
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
			if(namespaceName != null)
			{
				defaultProperties.put("NAMESPACE_NAME", namespaceName);
			}

			element = FileTemplateUtil.createFromTemplate(template, name, defaultProperties, dir);
			val psiFile = element.getContainingFile();

			val virtualFile = psiFile.getVirtualFile();
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
	protected void buildDialog(Project project, PsiDirectory psiDirectory, CreateFileFromTemplateDialog.Builder builder)
	{
		builder.addKind("Class", new IconDescriptor(AllIcons.Nodes.Class).addLayerIcon(CSharpIcons.Lang).toIcon(), "CSharpClass");
		builder.addKind("Interface", new IconDescriptor(AllIcons.Nodes.Interface).addLayerIcon(CSharpIcons.Lang).toIcon(), "CSharpInterface");
		builder.addKind("Enum", new IconDescriptor(AllIcons.Nodes.Enum).addLayerIcon(CSharpIcons.Lang).toIcon(), "CSharpEnum");
		builder.addKind("Struct", new IconDescriptor(AllIcons.Nodes.Static).addLayerIcon(CSharpIcons.Lang).toIcon(), "CSharpStruct");
	}

	@Override
	protected String getActionName(PsiDirectory psiDirectory, String s, String s2)
	{
		return "Create C# Class";
	}
}