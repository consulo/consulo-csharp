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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.csharp.ide.refactoring.CSharpRefactoringSupportProvider;
import consulo.csharp.lang.psi.CSharpNamedElement;
import consulo.language.editor.refactoring.rename.inplace.VariableInplaceRenameHandler;
import consulo.language.editor.refactoring.rename.inplace.VariableInplaceRenamer;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiNamedElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 14-Nov-17
 */
@ExtensionImpl
public class CSharpVariableInplaceRenamerHandler extends VariableInplaceRenameHandler
{
	@Override
	@RequiredReadAction
	protected boolean isAvailable(PsiElement element, Editor editor, PsiFile file)
	{
		final PsiElement nameSuggestionContext = file.findElementAt(editor.getCaretModel().getOffset());

		return editor.getSettings().isVariableInplaceRenameEnabled() && element instanceof CSharpNamedElement && CSharpRefactoringSupportProvider.mayRenameInplace(element, nameSuggestionContext);
	}

	@Nullable
	@Override
	protected VariableInplaceRenamer createRenamer(@Nonnull PsiElement elementToRename, Editor editor)
	{
		return new CSharpVariableInplaceRenamer((PsiNamedElement) elementToRename, editor);
	}
}
