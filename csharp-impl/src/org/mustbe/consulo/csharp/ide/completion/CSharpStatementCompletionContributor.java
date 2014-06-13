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

import static com.intellij.patterns.StandardPatterns.or;
import static com.intellij.patterns.StandardPatterns.psiElement;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpPseudoMethod;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.UsefulPsiTreeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpExpressionStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpForStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpForeachStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpLabeledStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpSwitchStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTryStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpNativeTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Condition;
import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.NotNullPairFunction;
import com.intellij.util.ProcessingContext;
import lombok.val;

/**
 * @author VISTALL
 * @since 12.06.14
 */
public class CSharpStatementCompletionContributor extends CompletionContributor implements CSharpTokenSets
{
	private static final TokenSet ourElseStatementKeywords = TokenSet.create(NEW_KEYWORD, ELSE_KEYWORD);

	private static final TokenSet ourParStatementKeywords = TokenSet.create(IF_KEYWORD, FOR_KEYWORD, FOREACH_KEYWORD, FOREACH_KEYWORD,
			FIXED_KEYWORD, UNCHECKED_KEYWORD, CHECKED_KEYWORD, SWITCH_KEYWORD, USING_KEYWORD, WHILE_KEYWORD, DO_KEYWORD, TRY_KEYWORD);

	private static final TokenSet ourCaseAndDefaultKeywords = TokenSet.create(CASE_KEYWORD, DEFAULT_KEYWORD);

	private static final TokenSet ourContinueAndBreakKeywords = TokenSet.create(BREAK_KEYWORD, CONTINUE_KEYWORD, GOTO_KEYWORD);

	private static final TokenSet ourReturnKeywords = TokenSet.create(RETURN_KEYWORD);

	private static final TokenSet ourCatchFinallyKeywords = TokenSet.create(CATCH_KEYWORD, FINALLY_KEYWORD);

	private static final ElementPattern<? extends PsiElement> ourContinueAndBreakPattern = psiElement().inside(or(psiElement().inside
			(CSharpForeachStatementImpl.class), psiElement().inside(CSharpForStatementImpl.class)));

	private static final ElementPattern<? extends PsiElement> ourGotoPattern = psiElement().inside(psiElement().inside(CSharpLabeledStatementImpl
			.class));

	private static final ElementPattern<? extends PsiElement> ourReturnPattern = psiElement().inside(psiElement().inside(CSharpPseudoMethod.class));

	public CSharpStatementCompletionContributor()
	{
		extend(CompletionType.BASIC, ourContinueAndBreakPattern, new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(
					@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpCompletionUtil.tokenSetToLookup(result, ourContinueAndBreakKeywords, new NotNullPairFunction<LookupElementBuilder,
						IElementType, LookupElementBuilder>()

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
								insertionContext.getDocument().insertString(offset, ";");
								insertionContext.getEditor().getCaretModel().moveToOffset(offset + 1);
							}
						});
						return t;
					}
				}, null);
			}
		});

		extend(CompletionType.BASIC, ourReturnPattern, new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(
					@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				val pseudoMethod = PsiTreeUtil.getParentOfType(parameters.getPosition(), CSharpPseudoMethod.class);
				assert pseudoMethod != null;
				CSharpCompletionUtil.tokenSetToLookup(result, ourReturnKeywords, new NotNullPairFunction<LookupElementBuilder, IElementType,
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
								boolean isVoidReturnType = pseudoMethod.getReturnTypeRef() == CSharpNativeTypeRef.VOID;
								if(!isVoidReturnType)
								{
									insertionContext.getDocument().insertString(offset, " ;");
								}
								else
								{
									insertionContext.getDocument().insertString(offset, ";");
								}

								insertionContext.getEditor().getCaretModel().moveToOffset(offset + 1);
								Editor editor = parameters.getEditor();
								if(!isVoidReturnType)
								{
									AutoPopupController.getInstance(editor.getProject()).autoPopupMemberLookup(editor, null);
								}
							}
						});
						return t;
					}
				}, null);
			}
		});

		extend(CompletionType.BASIC, ourGotoPattern, new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(
					@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpCompletionUtil.tokenSetToLookup(result, ourContinueAndBreakKeywords, new NotNullPairFunction<LookupElementBuilder,
						IElementType, LookupElementBuilder>()

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
								insertionContext.getDocument().insertString(offset, " ;");

								insertionContext.getEditor().getCaretModel().moveToOffset(offset + 1);
								Editor editor = parameters.getEditor();
								AutoPopupController.getInstance(editor.getProject()).autoPopupMemberLookup(editor, null);
							}
						});
						return t;
					}
				}, null);
			}
		});

		extend(CompletionType.BASIC, psiElement().withSuperParent(4, CSharpSwitchStatementImpl.class), new CompletionProvider<CompletionParameters>()

		{
			@Override
			protected void addCompletions(
					@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpCompletionUtil.tokenSetToLookup(result, ourCaseAndDefaultKeywords, new NotNullPairFunction<LookupElementBuilder, IElementType,
						LookupElementBuilder>()
				{
					@NotNull
					@Override
					public LookupElementBuilder fun(LookupElementBuilder t, final IElementType v)
					{
						t = t.withInsertHandler(new InsertHandler<LookupElement>()
						{
							@Override
							public void handleInsert(InsertionContext insertionContext, LookupElement item)
							{
								int offset = insertionContext.getEditor().getCaretModel().getOffset();
								insertionContext.getDocument().insertString(offset, " :");

								insertionContext.getEditor().getCaretModel().moveToOffset(offset + 1);
							}
						});

						return t;
					}
				}, null);
			}
		});

		extend(CompletionType.BASIC, psiElement().withSuperParent(2, CSharpExpressionStatementImpl.class),
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
					public LookupElementBuilder fun(LookupElementBuilder t, final IElementType v)
					{
						t = t.withInsertHandler(new InsertHandler<LookupElement>()
						{
							@Override
							public void handleInsert(InsertionContext insertionContext, LookupElement item)
							{
								int offset = insertionContext.getEditor().getCaretModel().getOffset();
								if(v == DO_KEYWORD || v == TRY_KEYWORD)
								{
									insertionContext.getDocument().insertString(offset, "{}");
								}
								else
								{
									insertionContext.getDocument().insertString(offset, "()");
								}
								insertionContext.getEditor().getCaretModel().moveToOffset(offset + 1);
							}
						});

						return t;
					}
				}, null);
			}
		});
		extend(CompletionType.BASIC, psiElement(), new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(
					@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				DotNetStatement statement = PsiTreeUtil.getParentOfType(parameters.getPosition(), DotNetStatement.class);
				assert statement != null;

				final PsiElement maybeTryStatement = UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(statement, true);
				if(maybeTryStatement instanceof CSharpTryStatementImpl)
				{
					CSharpCompletionUtil.tokenSetToLookup(result, ourCatchFinallyKeywords, new NotNullPairFunction<LookupElementBuilder,
									IElementType, LookupElementBuilder>()
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
											insertionContext.getDocument().insertString(offset, "{}");
											insertionContext.getEditor().getCaretModel().moveToOffset(offset + 1);
										}
									});
									return t;
								}
							}, new Condition<IElementType>()
							{
								@Override
								public boolean value(IElementType elementType)
								{
									CSharpTryStatementImpl st = (CSharpTryStatementImpl) maybeTryStatement;
									return st.getFinallyStatement() == null;
								}
							}
					);
				}
			}
		});
	}
}
