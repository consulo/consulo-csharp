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

package consulo.csharp.impl.ide.actions.generate;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.Editor;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.psi.CSharpIdentifier;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.language.editor.action.CodeInsightAction;
import consulo.language.editor.action.CodeInsightActionHandler;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;

/**
 * @author VISTALL
 * @since 25.06.14
 */
public abstract class CSharpGenerateAction extends CodeInsightAction
{
	@Nullable
	@RequiredReadAction
	public static CSharpTypeDeclaration findTypeDeclaration(@Nonnull Editor editor, @Nonnull PsiFile file)
	{
		if(file.getFileType() != CSharpFileType.INSTANCE)
		{
			return null;
		}
		final int offset = editor.getCaretModel().getOffset();
		final PsiElement elementAt = file.findElementAt(offset);
		if(elementAt == null)
		{
			return null;
		}

		PsiElement parent = elementAt.getParent();
		if(parent instanceof CSharpIdentifier)
		{
			parent = parent.getParent();
		}
		return parent instanceof CSharpTypeDeclaration ? (CSharpTypeDeclaration) parent : null;
	}

	private final CodeInsightActionHandler myHandler;

	public CSharpGenerateAction(CodeInsightActionHandler handler)
	{
		myHandler = handler;
	}

	@Nonnull
	@Override
	protected CodeInsightActionHandler getHandler()
	{
		return myHandler;
	}
}
