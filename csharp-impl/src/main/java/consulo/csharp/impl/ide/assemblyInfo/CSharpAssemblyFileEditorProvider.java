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

package consulo.csharp.impl.ide.assemblyInfo;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationManager;
import consulo.application.eap.EarlyAccessProgramManager;
import consulo.application.util.function.Computable;
import consulo.csharp.lang.impl.CSharpAssemblyConstants;
import consulo.csharp.lang.impl.psi.source.CSharpFileImpl;
import consulo.fileEditor.FileEditor;
import consulo.fileEditor.FileEditorPolicy;
import consulo.fileEditor.FileEditorProvider;
import consulo.fileEditor.FileEditorState;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.jdom.Element;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 09.03.14
 */
@ExtensionImpl
public class CSharpAssemblyFileEditorProvider implements FileEditorProvider
{

	@Override
	public boolean accept(@Nonnull final Project project, @Nonnull final VirtualFile virtualFile)
	{
		if(!EarlyAccessProgramManager.getInstance().getState(CSharpAssemblyFileEditorEapDescriptor.class))
		{
			return false;
		}
		PsiFile file = ApplicationManager.getApplication().runReadAction(new Computable<PsiFile>()
		{
			@Override
			public PsiFile compute()
			{
				return PsiManager.getInstance(project).findFile(virtualFile);
			}
		});
		return virtualFile.getName().equals(CSharpAssemblyConstants.FileName) && file instanceof CSharpFileImpl;
	}

	@Nonnull
	@Override
	public FileEditor createEditor(@Nonnull Project project, @Nonnull VirtualFile virtualFile)
	{
		return new CSharpAssemblyFileEditor(project, virtualFile);
	}

	@Override
	public void disposeEditor(@Nonnull FileEditor fileEditor)
	{

	}

	@Nonnull
	@Override
	public FileEditorState readState(@Nonnull Element element, @Nonnull Project project, @Nonnull VirtualFile virtualFile)
	{
		return FileEditorState.INSTANCE;
	}

	@Override
	public void writeState(@Nonnull FileEditorState fileEditorState, @Nonnull Project project, @Nonnull Element element)
	{

	}

	@Nonnull
	@Override
	public String getEditorTypeId()
	{
		return "csharp.assembly.editor";
	}

	@Nonnull
	@Override
	public FileEditorPolicy getPolicy()
	{
		return FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR;
	}
}
