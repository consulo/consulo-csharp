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

package org.mustbe.consulo.csharp.ide.completion;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpExpressionStatementImpl;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.NotNullPairFunction;
import com.intellij.util.ProcessingContext;

/**
 * @author VISTALL
 * @since 12.06.14
 */
public class CSharpStatementCompletionContributor extends CompletionContributor
{
	private static final TokenSet ourElseStatementKeywords = TokenSet.create(CSharpTokens.NEW_KEYWORD, CSharpTokens.TRY_KEYWORD,
			CSharpTokens.ELSE_KEYWORD);

	private static final TokenSet ourParStatementKeywords = TokenSet.create(CSharpTokenSets.IF_KEYWORD, CSharpTokens.FOR_KEYWORD,
			CSharpTokens.FOREACH_KEYWORD, CSharpTokens.FOREACH_KEYWORD, CSharpTokens.FIXED_KEYWORD, CSharpTokens.UNCHECKED_KEYWORD,
			CSharpTokens.CHECKED_KEYWORD, CSharpTokens.SWITCH_KEYWORD, CSharpTokens.USING_KEYWORD);

	public CSharpStatementCompletionContributor()
	{
		extend(CompletionType.BASIC, StandardPatterns.psiElement().withSuperParent(2, CSharpExpressionStatementImpl.class),
				new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(
					@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpCompletionUtil.tokenSetToLookup(result, ourElseStatementKeywords, null, null);

				CSharpCompletionUtil.tokenSetToLookup(result, ourParStatementKeywords, new NotNullPairFunction<LookupElementBuilder, IElementType,
						LookupElementBuilder>()
				{
					@NotNull
					@Override
					public LookupElementBuilder fun(LookupElementBuilder t, IElementType v)
					{
						t = t.withInsertHandler(new InsertHandler<LookupElement>()
						{
							@Override
							public void handleInsert(InsertionContext insertionContext, LookupElement item)
							{
								int offset = insertionContext.getEditor().getCaretModel().getOffset();
								insertionContext.getDocument().insertString(offset, "()");

								insertionContext.getEditor().getCaretModel().moveToOffset(offset + 1);
							}
						});

						return t;
					}
				}, null);
			}
		});
	}
}
