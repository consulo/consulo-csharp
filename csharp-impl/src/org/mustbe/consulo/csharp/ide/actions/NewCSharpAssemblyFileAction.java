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

import java.util.Properties;
import java.util.UUID;

import org.apache.velocity.runtime.parser.ParseException;
import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.assemblyInfo.CSharpAssemblyConstants;
import org.mustbe.consulo.csharp.module.extension.BaseCSharpModuleExtension;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeView;
import com.intellij.ide.actions.CreateFileAction;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import lombok.val;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
@Logger
public class NewCSharpAssemblyFileAction extends AnAction
{
	public NewCSharpAssemblyFileAction()
	{
		super(null, null, AllIcons.FileTypes.Config);
	}

	@Override
	public void actionPerformed(AnActionEvent anActionEvent)
	{
		DataContext dataContext = anActionEvent.getDataContext();
		final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
		if(view == null)
		{
			return;
		}

		final Project project = CommonDataKeys.PROJECT.getData(dataContext);

		final PsiDirectory dir = view.getOrChooseDirectory();
		if(dir == null || project == null)
		{
			return;
		}
		final FileTemplate template = FileTemplateManager.getInstance().getInternalTemplate("CSharpAssemblyFile");

		Module module = LangDataKeys.MODULE.getData(dataContext);

		assert module != null;

		createFileFromTemplate(CSharpAssemblyConstants.FileName, template, dir, module.getName());
	}

	@SuppressWarnings("DialogTitleCapitalization")
	@Nullable
	public static PsiFile createFileFromTemplate(@Nullable String name, @NotNull FileTemplate template, @NotNull PsiDirectory dir, String moduleName)
	{
		CreateFileAction.MkDirs mkdirs = new CreateFileAction.MkDirs(name, dir);
		name = mkdirs.newName;
		dir = mkdirs.directory;
		PsiElement element;
		Project project = dir.getProject();
		try
		{
			Properties defaultProperties = FileTemplateManager.getInstance().getDefaultProperties(project);
			defaultProperties.put("MODULE", moduleName);
			defaultProperties.put("GUID", UUID.randomUUID().toString());
			element = FileTemplateUtil.createFromTemplate(template, name, defaultProperties, dir);
			final PsiFile psiFile = element.getContainingFile();

			final VirtualFile virtualFile = psiFile.getVirtualFile();
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
			LOGGER.error(e);
		}

		return null;
	}


	@Override
	public void update(AnActionEvent e)
	{
		super.update(e);
		e.getPresentation().setEnabledAndVisible(isAvailable(e.getDataContext()));
	}

	private boolean isAvailable(DataContext dataContext)
	{
		val module = LangDataKeys.MODULE.getData(dataContext);
		if(module != null)
		{
			DotNetModuleExtension extension = ModuleUtilCore.getExtension(module, DotNetModuleExtension.class);
			if(extension != null && extension.isAllowSourceRoots())
			{
				return false;
			}
		}
		if(module == null || ModuleUtilCore.getExtension(module, BaseCSharpModuleExtension.class) == null)
		{
			return false;
		}

		final Ref<VirtualFile> ref = Ref.create();
		VfsUtil.visitChildrenRecursively(module.getModuleDir(), new VirtualFileVisitor<Object>()
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
}
