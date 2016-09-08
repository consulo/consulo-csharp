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
import org.mustbe.consulo.csharp.ide.completion.insertHandler.CSharpTailInsertHandler;
import org.mustbe.consulo.csharp.ide.completion.patterns.CSharpPatterns;
import org.mustbe.consulo.csharp.ide.completion.util.ExpressionOrStatementInsertHandler;
import org.mustbe.consulo.csharp.ide.completion.util.SpaceInsertHandler;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.UsefulPsiTreeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpImplicitReturnModel;
import org.mustbe.consulo.csharp.lang.psi.impl.source.*;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpModuleUtil;
import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import consulo.annotations.RequiredReadAction;
import consulo.codeInsight.completion.CompletionProvider;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetStatement;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefUtil;
import consulo.util.NotNullPairFunction;

/**
 * @author VISTALL
 * @since 12.06.14
 */
public class CSharpStatementCompletionContributor extends CompletionContributor implements CSharpTokenSets
{
	private static class ReturnStatementInsertHandler implements InsertHandler<LookupElement>
	{
		private final CSharpSimpleLikeMethodAsElement myPseudoMethod;

		public ReturnStatementInsertHandler(CSharpSimpleLikeMethodAsElement pseudoMethod)
		{
			myPseudoMethod = pseudoMethod;
		}

		@Override
		public void handleInsert(InsertionContext insertionContext, LookupElement item)
		{
			Editor editor = insertionContext.getEditor();
			int offset = editor.getCaretModel().getOffset();
			boolean isVoidReturnType = DotNetTypeRefUtil.isVmQNameEqual(myPseudoMethod.getReturnTypeRef(), myPseudoMethod, DotNetTypes.System.Void);
			if(!isVoidReturnType)
			{
				TailType.insertChar(editor, offset, ' ');
				TailType.insertChar(editor, offset + 1, ';');
			}
			else
			{
				TailType.insertChar(editor, offset, ';');
			}

			insertionContext.getEditor().getCaretModel().moveToOffset(offset + 1);
			if(!isVoidReturnType)
			{
				AutoPopupController.getInstance(editor.getProject()).autoPopupMemberLookup(editor, null);
			}
		}
	}

	private static final TokenSet ourParStatementKeywords = TokenSet.create(IF_KEYWORD, FOR_KEYWORD, FOREACH_KEYWORD, FOREACH_KEYWORD, FIXED_KEYWORD, UNCHECKED_KEYWORD, CHECKED_KEYWORD,
			SWITCH_KEYWORD, USING_KEYWORD, WHILE_KEYWORD, DO_KEYWORD, UNSAFE_KEYWORD, TRY_KEYWORD, LOCK_KEYWORD);

	private static final TokenSet ourCaseAndDefaultKeywords = TokenSet.create(CASE_KEYWORD, DEFAULT_KEYWORD);

	private static final TokenSet ourContinueAndBreakKeywords = TokenSet.create(BREAK_KEYWORD, CONTINUE_KEYWORD, GOTO_KEYWORD);

	private static final TokenSet ourCatchFinallyKeywords = TokenSet.create(CATCH_KEYWORD, FINALLY_KEYWORD);


	public CSharpStatementCompletionContributor()
	{
		extend(CompletionType.BASIC, CSharpPatterns.statementStart().inside(or(psiElement().inside(CSharpForeachStatementImpl.class), psiElement().inside(CSharpForStatementImpl.class),
				psiElement().inside(CSharpWhileStatementImpl.class), psiElement().inside(CSharpDoWhileStatementImpl.class))), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpCompletionUtil.tokenSetToLookup(result, ourContinueAndBreakKeywords, new NotNullPairFunction<LookupElementBuilder, IElementType, LookupElement>()

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

		extend(CompletionType.BASIC, CSharpPatterns.statementStart().inside(psiElement().inside(CSharpSimpleLikeMethodAsElement.class)).andNot(psiElement().inside(CSharpFinallyStatementImpl.class)),
				new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				final CSharpSimpleLikeMethodAsElement pseudoMethod = PsiTreeUtil.getParentOfType(parameters.getPosition(), CSharpSimpleLikeMethodAsElement.class);
				assert pseudoMethod != null;
				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.RETURN_KEYWORD, new NotNullPairFunction<LookupElementBuilder, IElementType, LookupElement>()

				{
					@NotNull
					@Override
					public LookupElement fun(LookupElementBuilder t, IElementType v)
					{
						t = t.withInsertHandler(new ReturnStatementInsertHandler(pseudoMethod));
						return t;
					}
				}, null);
			}
		});

		extend(CompletionType.BASIC, CSharpPatterns.statementStart().inside(CSharpLabeledStatementImpl.class), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpCompletionUtil.tokenSetToLookup(result, ourContinueAndBreakKeywords, new NotNullPairFunction<LookupElementBuilder, IElementType, LookupElement>()

				{
					@NotNull
					@Override
					public LookupElement fun(LookupElementBuilder t, IElementType v)
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

		extend(CompletionType.BASIC, CSharpPatterns.statementStart().withSuperParent(6, CSharpSwitchStatementImpl.class), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.BREAK_KEYWORD, null, null);
				CSharpCompletionUtil.tokenSetToLookup(result, ourCaseAndDefaultKeywords, new NotNullPairFunction<LookupElementBuilder, IElementType, LookupElement>()
				{
					@NotNull
					@Override
					public LookupElement fun(LookupElementBuilder t, final IElementType v)
					{
						if(v == CSharpTokens.DEFAULT_KEYWORD)
						{
							t = t.withInsertHandler(new CSharpTailInsertHandler(TailType.CASE_COLON));
						}
						else
						{
							t = t.withInsertHandler(SpaceInsertHandler.INSTANCE);
						}

						return t;
					}
				}, null);
			}
		});

		extend(CompletionType.BASIC, CSharpPatterns.statementStart(), new CompletionProvider()
		{
			@Override
			@RequiredReadAction
			public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpLocalVariable localVariable = PsiTreeUtil.getParentOfType(parameters.getPosition(), CSharpLocalVariable.class);
				assert localVariable != null;
				if(!CSharpPsiUtilImpl.isNullOrEmpty(localVariable))
				{
					return;
				}
				CSharpCompletionUtil.tokenSetToLookup(result, ourParStatementKeywords, new NotNullPairFunction<LookupElementBuilder, IElementType, LookupElement>()
				{
					@NotNull
					@Override
					public LookupElement fun(LookupElementBuilder t, final IElementType v)
					{
						t = t.withInsertHandler(buildInsertHandler(v));
						return t;
					}
				}, null);
				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.THROW_KEYWORD, CSharpCompletionUtil.ourSpaceInsert, null);
			}
		});

		extend(CompletionType.BASIC, CSharpPatterns.statementStart(), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				final PsiElement position = parameters.getPosition();
				CSharpCompletionUtil.elementToLookup(result, CSharpSoftTokens.YIELD_KEYWORD, new NotNullPairFunction<LookupElementBuilder, IElementType, LookupElement>()
						{
							@NotNull
							@Override
							public LookupElement fun(LookupElementBuilder t, final IElementType v)
							{
								t = t.withInsertHandler(new InsertHandler<LookupElement>()
								{
									@Override
									public void handleInsert(InsertionContext insertionContext, LookupElement item)
									{
										Editor editor = insertionContext.getEditor();

										if(insertionContext.getCompletionChar() == ';')
										{
											insertionContext.getDocument().insertString(insertionContext.getTailOffset(), " ");
											editor.getCaretModel().moveToOffset(insertionContext.getTailOffset() - 1);
										}
										else if(insertionContext.getCompletionChar() == ' ')
										{
											TailType.insertChar(editor, editor.getCaretModel().getOffset(), ';');
											editor.getCaretModel().moveToOffset(editor.getCaretModel().getOffset() - 1);
										}
										else
										{
											insertionContext.getDocument().insertString(insertionContext.getTailOffset(), " ;");
											editor.getCaretModel().moveToOffset(insertionContext.getTailOffset() - 1);
										}

										AutoPopupController.getInstance(insertionContext.getProject()).autoPopupMemberLookup(editor, null);
									}
								});
								return t;
							}
						}, new Condition<IElementType>()
						{
							@Override
							public boolean value(IElementType elementType)
							{
								CSharpSimpleLikeMethodAsElement methodAsElement = PsiTreeUtil.getParentOfType(position, CSharpSimpleLikeMethodAsElement.class);
								if(methodAsElement == null)
								{
									return false;
								}
								DotNetTypeRef returnTypeRef = methodAsElement.getReturnTypeRef();
								if(CSharpImplicitReturnModel.YieldEnumerable.extractTypeRef(returnTypeRef, position) != DotNetTypeRef.ERROR_TYPE)
								{
									return true;
								}
								if(CSharpImplicitReturnModel.YieldEnumerator.extractTypeRef(returnTypeRef, position) != DotNetTypeRef.ERROR_TYPE)
								{
									return true;
								}
								return false;
							}
						}
				);
			}
		});

		extend(CompletionType.BASIC, CSharpPatterns.statementStart(), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.CONST_KEYWORD, CSharpCompletionUtil.ourSpaceInsert, null);
			}
		});

		extend(CompletionType.BASIC, psiElement().afterLeaf(psiElement().withElementType(CSharpSoftTokens.YIELD_KEYWORD)), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				final PsiElement position = parameters.getPosition();
				final CSharpSimpleLikeMethodAsElement methodAsElement = PsiTreeUtil.getParentOfType(position, CSharpSimpleLikeMethodAsElement.class);
				if(methodAsElement == null)
				{
					return;
				}
				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.BREAK_KEYWORD, null, null);
				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.RETURN_KEYWORD, new NotNullPairFunction<LookupElementBuilder, IElementType, LookupElement>()

				{
					@NotNull
					@Override
					public LookupElement fun(LookupElementBuilder t, IElementType v)
					{
						t = t.withInsertHandler(new ReturnStatementInsertHandler(methodAsElement));
						return t;
					}
				}, null);
			}
		});

		extend(CompletionType.BASIC, psiElement().afterLeaf(psiElement().withElementType(CSharpTokens.ELSE_KEYWORD)).withSuperParent(2, CSharpExpressionStatementImpl.class), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.IF_KEYWORD, new NotNullPairFunction<LookupElementBuilder, IElementType, LookupElement>()
				{
					@NotNull
					@Override
					public LookupElement fun(LookupElementBuilder t, final IElementType v)
					{
						t = t.withInsertHandler(buildInsertHandler(v));

						return t;
					}
				}, null);
			}
		});

		extend(CompletionType.BASIC, CSharpPatterns.statementStart(), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				DotNetStatement statement = PsiTreeUtil.getParentOfType(parameters.getPosition(), DotNetStatement.class);
				if(statement == null)
				{
					return;
				}

				final PsiElement maybeTryStatement = UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(statement, true);
				if(maybeTryStatement instanceof CSharpTryStatementImpl)
				{
					CSharpCompletionUtil.tokenSetToLookup(result, ourCatchFinallyKeywords, new NotNullPairFunction<LookupElementBuilder, IElementType, LookupElement>()
							{
								@NotNull
								@Override
								public LookupElementBuilder fun(LookupElementBuilder t, IElementType v)
								{
									t = t.withInsertHandler(buildInsertHandler(v));
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
				else if(maybeTryStatement instanceof CSharpIfStatementImpl)
				{
					CSharpCompletionUtil.elementToLookup(result, CSharpTokens.ELSE_KEYWORD, null, null);
				}
			}
		});

		extend(CompletionType.BASIC, psiElement().afterLeaf(psiElement().withElementType(CSharpTokens.RPAR).withParent(CSharpCatchStatementImpl
				.class)), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				if(CSharpModuleUtil.findLanguageVersion(parameters.getPosition()).isAtLeast(CSharpLanguageVersion._6_0))
				{
					CSharpCompletionUtil.elementToLookup(result, CSharpSoftTokens.WHEN_KEYWORD, new NotNullPairFunction<LookupElementBuilder, IElementType, LookupElement>()

					{
						@NotNull
						@Override
						public LookupElement fun(LookupElementBuilder t, IElementType v)
						{
							t = t.withInsertHandler(buildInsertHandler(v));
							return t;
						}
					}, null);
				}
			}
		});
	}


	@RequiredReadAction
	@Override
	public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result)
	{
		super.fillCompletionVariants(parameters, CSharpCompletionSorting.modifyResultSet(parameters, result));
	}

	@NotNull
	public static InsertHandler<LookupElement> buildInsertHandler(final IElementType elementType)
	{
		char open = '(';
		char close = ')';

		if(elementType == DO_KEYWORD ||
				elementType == TRY_KEYWORD ||
				elementType == CATCH_KEYWORD ||
				elementType == UNSAFE_KEYWORD ||
				elementType == FINALLY_KEYWORD)
		{
			open = '{';
			close = '}';
		}

		return new ExpressionOrStatementInsertHandler<LookupElement>(open, close);
	}
}
