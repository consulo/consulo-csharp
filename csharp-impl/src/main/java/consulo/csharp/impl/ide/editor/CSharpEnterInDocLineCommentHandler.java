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

package consulo.csharp.impl.ide.editor;

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.codeEditor.action.EditorActionHandler;
import consulo.csharp.lang.doc.psi.CSharpDocRoot;
import consulo.dataContext.DataContext;
import consulo.document.Document;
import consulo.language.editor.action.EnterHandlerDelegateAdapter;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.lang.ref.Ref;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 */
@ExtensionImpl
public class CSharpEnterInDocLineCommentHandler extends EnterHandlerDelegateAdapter
{
	private static final String DOC_LINE_START = "///";

	@Override
	@RequiredUIAccess
	public Result preprocessEnter(@Nonnull final PsiFile file,
			@Nonnull final Editor editor,
			@Nonnull final Ref<Integer> caretOffsetRef,
			@Nonnull final Ref<Integer> caretAdvance,
			@Nonnull final DataContext dataContext,
			final EditorActionHandler originalHandler)
	{
		final int caretOffset = caretOffsetRef.get();
		final Document document = editor.getDocument();
		final PsiElement psiAtOffset = file.findElementAt(caretOffset);
		final PsiElement probablyDocComment = psiAtOffset instanceof PsiWhiteSpace && psiAtOffset.getText().startsWith("\n") ? psiAtOffset.getPrevSibling() : psiAtOffset == null && caretOffset > 0
				&& caretOffset == document.getTextLength() ? file.findElementAt(caretOffset - 1) : psiAtOffset;

		if(probablyDocComment != null && PsiTreeUtil.getParentOfType(probablyDocComment, CSharpDocRoot.class, false) != null)
		{
			document.insertString(caretOffset, DOC_LINE_START + " ");
			caretAdvance.set(4);
			return Result.Default;
		}

		return Result.Continue;
	}
}
