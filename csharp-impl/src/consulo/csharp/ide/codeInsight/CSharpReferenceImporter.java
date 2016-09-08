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

package consulo.csharp.ide.codeInsight;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.codeInsight.actions.UsingNamespaceFix;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import com.intellij.codeInsight.daemon.ReferenceImporter;
import com.intellij.codeInsight.daemon.impl.CollectHighlightsUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;

/**
 * @author VISTALL
 * @since 30.12.13.
 */
public class CSharpReferenceImporter implements ReferenceImporter
{
	@Override
	@RequiredReadAction
	public boolean autoImportReferenceAtCursor(@NotNull Editor editor, @NotNull PsiFile file)
	{
		if(!file.getViewProvider().getLanguages().contains(CSharpLanguage.INSTANCE))
		{
			return false;
		}

		int caretOffset = editor.getCaretModel().getOffset();
		Document document = editor.getDocument();
		int lineNumber = document.getLineNumber(caretOffset);
		int startOffset = document.getLineStartOffset(lineNumber);
		int endOffset = document.getLineEndOffset(lineNumber);

		List<PsiElement> elements = CollectHighlightsUtil.getElementsInRange(file, startOffset, endOffset);
		for(PsiElement element : elements)
		{
			if(element instanceof PsiReference)
			{
				if(handleReference((PsiReference) element, editor))
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	@RequiredReadAction
	public boolean autoImportReferenceAt(@NotNull Editor editor, @NotNull PsiFile file, int offset)
	{
		if(!file.getViewProvider().getLanguages().contains(CSharpLanguage.INSTANCE))
		{
			return false;
		}

		PsiReference element = file.findReferenceAt(offset);
		return handleReference(element, editor);
	}

	@RequiredReadAction
	private static boolean handleReference(PsiReference reference, Editor editor)
	{
		if(reference instanceof CSharpReferenceExpression)
		{
			if(UsingNamespaceFix.isValidReference(((CSharpReferenceExpression) reference).kind(), (CSharpReferenceExpression) reference))
			{
				new UsingNamespaceFix((CSharpReferenceExpression) reference).doFix(editor);
				return true;
			}
		}
		return false;
	}
}
