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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.csharp.lang.psi.CSharpNamedElement;
import consulo.language.editor.completion.lookup.LookupManager;
import consulo.language.editor.refactoring.RefactoringSupportProvider;
import consulo.language.editor.refactoring.rename.inplace.MemberInplaceRenameHandler;
import consulo.language.editor.refactoring.rename.inplace.MemberInplaceRenamer;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiNameIdentifierOwner;
import consulo.language.psi.PsiNamedElement;
import consulo.language.psi.util.PsiTreeUtil;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 14-Nov-17
 */
@ExtensionImpl
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
		final RefactoringSupportProvider supportProvider = element != null ? RefactoringSupportProvider.forLanguage(element.getLanguage()) : null;
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
