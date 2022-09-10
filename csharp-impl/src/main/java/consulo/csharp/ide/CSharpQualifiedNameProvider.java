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

package consulo.csharp.ide;

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.language.editor.QualifiedNameProvider;
import consulo.language.psi.PsiElement;
import consulo.project.Project;

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 19.01.14
 */
@ExtensionImpl
public class CSharpQualifiedNameProvider implements QualifiedNameProvider
{
	@Nullable
	@Override
	public PsiElement adjustElementToCopy(PsiElement element)
	{
		return element;
	}

	@Nullable
	@Override
	public String getQualifiedName(PsiElement element)
	{
		if(element instanceof DotNetQualifiedElement)
		{
			return ((DotNetQualifiedElement) element).getPresentableQName();
		}
		return null;
	}

	@Nullable
	@Override
	public PsiElement qualifiedNameToElement(String s, Project project)
	{
		return null;
	}

	@Override
	public void insertQualifiedName(String s, PsiElement element, Editor editor, Project project)
	{
		editor.getDocument().insertString(editor.getCaretModel().getOffset(), s);
	}
}
