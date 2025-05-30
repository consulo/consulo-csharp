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

package consulo.csharp.impl.ide.refactoring.rename.inplace;

import jakarta.annotation.Nonnull;

import consulo.codeEditor.Editor;
import consulo.language.psi.PsiReference;
import consulo.util.lang.Comparing;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import consulo.language.editor.refactoring.rename.inplace.MemberInplaceRenamer;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpNamedElement;
import consulo.csharp.lang.impl.psi.source.CSharpPsiUtilImpl;
import consulo.dotnet.psi.DotNetNamedElement;

/**
 * @author VISTALL
 * @since 14-Nov-17
 */
public class CSharpMemberInplaceRenamer extends MemberInplaceRenamer
{
	@RequiredReadAction
	public CSharpMemberInplaceRenamer(@Nonnull PsiNamedElement elementToRename, PsiElement substituted, Editor editor)
	{
		super(elementToRename, substituted, editor, CSharpNamedElement.getEscapedName((DotNetNamedElement) elementToRename), CSharpNamedElement.getEscapedName((DotNetNamedElement) elementToRename));
	}

	@Override
	protected boolean acceptReference(PsiReference reference)
	{
		final PsiElement element = reference.getElement();
		final TextRange textRange = reference.getRangeInElement();
		final String referenceText = element.getText().substring(textRange.getStartOffset(), textRange.getEndOffset());
		return Comparing.strEqual(CSharpPsiUtilImpl.getNameWithoutAt(referenceText), myElementToRename.getName());
	}
}
