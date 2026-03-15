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


/**
 * @author VISTALL
 * @since 09.03.14
 */
@ExtensionImpl
public class CSharpAssemblyFileEditorProvider implements FileEditorProvider
{

	@Override
	public boolean accept(final Project project, final VirtualFile virtualFile)
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

	@Override
	public FileEditor createEditor(Project project, VirtualFile virtualFile)
	{
		return new CSharpAssemblyFileEditor(project, virtualFile);
	}

	@Override
	public void disposeEditor(FileEditor fileEditor)
	{

	}

	@Override
	public FileEditorState readState(Element element, Project project, VirtualFile virtualFile)
	{
		return FileEditorState.INSTANCE;
	}

	@Override
	public void writeState(FileEditorState fileEditorState, Project project, Element element)
	{

	}

	@Override
	public String getEditorTypeId()
	{
		return "csharp.assembly.editor";
	}

	@Override
	public FileEditorPolicy getPolicy()
	{
		return FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR;
	}
}
