/*
 * Copyright 2013-2021 consulo.io
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

package consulo.csharp.spellchecker;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.spellchecker.inspections.IdentifierSplitter;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.impl.source.CSharpPsiUtilImpl;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 20/10/2021
 */
public class CSharpNameIdentifierOwnerTokenizer extends Tokenizer<PsiNameIdentifierOwner>
{
	public static final CSharpNameIdentifierOwnerTokenizer INSTANCE = new CSharpNameIdentifierOwnerTokenizer();

	@Override
	@RequiredReadAction
	public void tokenize(@Nonnull PsiNameIdentifierOwner owner, TokenConsumer tokenConsumer)
	{
		PsiElement identifier = owner.getNameIdentifier();
		if(identifier == null)
		{
			return;
		}
		PsiElement parent = owner;

		TextRange range = identifier.getTextRange();
		if(range.isEmpty())
		{
			return;
		}

		String oldText = identifier.getText();
		if(oldText.charAt(0) == '@')
		{
			range = new TextRange(range.getStartOffset() + 1, range.getEndOffset());
		}

		int offset = range.getStartOffset() - parent.getTextRange().getStartOffset();
		if(offset < 0)
		{
			parent = PsiTreeUtil.findCommonParent(identifier, owner);
			offset = range.getStartOffset() - parent.getTextRange().getStartOffset();
		}

		String text = CSharpPsiUtilImpl.getNameWithoutAt(oldText);
		tokenConsumer.consumeToken(parent, text, true, offset, TextRange.allOf(text), IdentifierSplitter.getInstance());
	}
}
