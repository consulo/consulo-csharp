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

package consulo.csharp.ide.refactoring.rename.inplace;

import javax.annotation.Nonnull;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.lang.LanguageRefactoringSupport;
import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.rename.inplace.MemberInplaceRenameHandler;
import com.intellij.refactoring.rename.inplace.MemberInplaceRenamer;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpNamedElement;

/**
 * @author VISTALL
 * @since 14-Nov-17
 */
public class CSharpMemberInplaceRenameHandler extends MemberInplaceRenameHandler
{
	@Override
	@RequiredReadAction
	protected boolean isAvailable(PsiElement element, Editor editor, PsiFile file)
	{
		final PsiElement nameSuggestionContext = file.findElementAt(editor.getCaretModel().getOffset());
		if(element == null && LookupManager.getActiveLookup(editor) != null)
		{
			element = PsiTreeUtil.getParentOfType(nameSuggestionContext, PsiNamedElement.class);
		}
		final RefactoringSupportProvider supportProvider = element != null ? LanguageRefactoringSupport.INSTANCE.forLanguage(element.getLanguage()) : null;
		return editor.getSettings().isVariableInplaceRenameEnabled() && supportProvider != null && supportProvider.isMemberInplaceRenameAvailable(element, nameSuggestionContext) && element
				instanceof CSharpNamedElement;
	}

	@Override
	@Nonnull
	@RequiredReadAction
	protected MemberInplaceRenamer createMemberRenamer(@Nonnull PsiElement element, PsiNameIdentifierOwner elementToRename, Editor editor)
	{
		return new CSharpMemberInplaceRenamer(elementToRename, element, editor);
	}
}
