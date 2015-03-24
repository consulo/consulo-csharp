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
import org.mustbe.consulo.csharp.ide.codeStyle.CSharpCodeStyleSettings;
import org.mustbe.consulo.csharp.ide.completion.util.ExpressionOrStatementInsertHandler;
import org.mustbe.consulo.csharp.ide.completion.util.SpaceInsertHandler;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.UsefulPsiTreeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpImplicitReturnModel;
import org.mustbe.consulo.csharp.lang.psi.impl.source.*;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleUtil;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefUtil;
import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.TailType;
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
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
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

	private static final TokenSet ourParStatementKeywords = TokenSet.create(IF_KEYWORD, FOR_KEYWORD, FOREACH_KEYWORD, FOREACH_KEYWORD,
			FIXED_KEYWORD, UNCHECKED_KEYWORD, CHECKED_KEYWORD, SWITCH_KEYWORD, USING_KEYWORD, WHILE_KEYWORD, DO_KEYWORD, UNSAFE_KEYWORD,
			TRY_KEYWORD, LOCK_KEYWORD);

	private static final TokenSet ourCaseAndDefaultKeywords = TokenSet.create(CASE_KEYWORD, DEFAULT_KEYWORD);

	private static final TokenSet ourContinueAndBreakKeywords = TokenSet.create(BREAK_KEYWORD, CONTINUE_KEYWORD, GOTO_KEYWORD);

	private static final TokenSet ourCatchFinallyKeywords = TokenSet.create(CATCH_KEYWORD, FINALLY_KEYWORD);

	private static final ElementPattern<? extends PsiElement> ourStatementStart = or(psiElement().withElementType(CSharpTokens.SEMICOLON).inside
			(DotNetStatement.class), psiElement().withElementType(CSharpTokens.LBRACE).inside(DotNetStatement.class),
			psiElement().withElementType(CSharpTokens.RBRACE).inside(DotNetStatement.class));

	private static final ElementPattern<? extends PsiElement> ourContinueAndBreakPattern = psiElement().afterLeaf(ourStatementStart).inside(or
			(psiElement().inside(CSharpForeachStatementImpl.class), psiElement().inside(CSharpForStatementImpl.class),
					psiElement().inside(CSharpWhileStatementImpl.class), psiElement().inside(CSharpDoWhileStatementImpl.class)));

	private static final ElementPattern<? extends PsiElement> ourGotoPattern = psiElement().afterLeaf(ourStatementStart).inside(psiElement().inside
			(CSharpLabeledStatementImpl.class));

	private static final ElementPattern<? extends PsiElement> ourReturnPattern = psiElement().afterLeaf(ourStatementStart).inside(psiElement()
			.inside(CSharpSimpleLikeMethodAsElement.class)).andNot(psiElement().inside(CSharpFinallyStatementImpl.class));

	public CSharpStatementCompletionContributor()
	{
		extend(CompletionType.BASIC, ourContinueAndBreakPattern, new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull final CompletionParameters parameters,
					ProcessingContext context,
					@NotNull CompletionResultSet result)
			{
				CSharpCompletionUtil.tokenSetToLookup(result, ourContinueAndBreakKeywords, new NotNullPairFunction<LookupElementBuilder,
						IElementType, LookupElement>()

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
			protected void addCompletions(@NotNull final CompletionParameters parameters,
					ProcessingContext context,
					@NotNull CompletionResultSet result)
			{
				val pseudoMethod = PsiTreeUtil.getParentOfType(parameters.getPosition(), CSharpSimpleLikeMethodAsElement.class);
				assert pseudoMethod != null;
				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.RETURN_KEYWORD, new NotNullPairFunction<LookupElementBuilder,
						IElementType, LookupElement>()

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

		extend(CompletionType.BASIC, ourGotoPattern, new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull final CompletionParameters parameters,
					ProcessingContext context,
					@NotNull CompletionResultSet result)
			{
				CSharpCompletionUtil.tokenSetToLookup(result, ourContinueAndBreakKeywords, new NotNullPairFunction<LookupElementBuilder,
						IElementType, LookupElement>()

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

		extend(CompletionType.BASIC, psiElement().withSuperParent(4, CSharpSwitchStatementImpl.class), new CompletionProvider<CompletionParameters>()

		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpCompletionUtil.tokenSetToLookup(result, ourCaseAndDefaultKeywords, new NotNullPairFunction<LookupElementBuilder, IElementType,
						LookupElement>()
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

		extend(CompletionType.BASIC, psiElement().afterLeaf(ourStatementStart).withSuperParent(2, CSharpExpressionStatementImpl.class),
				new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpCompletionUtil.tokenSetToLookup(result, ourParStatementKeywords, new NotNullPairFunction<LookupElementBuilder, IElementType,
						LookupElement>()
				{
					@NotNull
					@Override
					public LookupElement fun(LookupElementBuilder t, final IElementType v)
					{
						t = t.withInsertHandler(buildInsertHandler(v));
						return t;
					}
				}, null);
				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.THROW_KEYWORD, new NotNullPairFunction<LookupElementBuilder, IElementType,
						LookupElement>()

				{
					@NotNull
					@Override
					public LookupElement fun(LookupElementBuilder t, IElementType v)
					{
						t = t.withInsertHandler(SpaceInsertHandler.INSTANCE);
						return t;
					}
				}, null);
			}
		});

		extend(CompletionType.BASIC, psiElement().afterLeaf(ourStatementStart).withSuperParent(2, CSharpExpressionStatementImpl.class),
				new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				val position = parameters.getPosition();
				CSharpCompletionUtil.elementToLookup(result, CSharpSoftTokens.YIELD_KEYWORD, new NotNullPairFunction<LookupElementBuilder,
								IElementType, LookupElement>()
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
								CSharpSimpleLikeMethodAsElement methodAsElement = PsiTreeUtil.getParentOfType(position,
										CSharpSimpleLikeMethodAsElement.class);
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

		extend(CompletionType.BASIC, psiElement().afterLeaf(psiElement().withElementType(CSharpSoftTokens.YIELD_KEYWORD)),
				new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				val position = parameters.getPosition();
				val methodAsElement = PsiTreeUtil.getParentOfType(position, CSharpSimpleLikeMethodAsElement.class);
				if(methodAsElement == null)
				{
					return;
				}
				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.BREAK_KEYWORD, null, null);
				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.RETURN_KEYWORD, new NotNullPairFunction<LookupElementBuilder,
						IElementType, LookupElement>()

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

		extend(CompletionType.BASIC, psiElement().afterLeaf(psiElement().withElementType(CSharpTokens.ELSE_KEYWORD)).withSuperParent(2,
				CSharpExpressionStatementImpl.class), new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.IF_KEYWORD, new NotNullPairFunction<LookupElementBuilder, IElementType,
						LookupElement>()
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

		extend(CompletionType.BASIC, psiElement(), new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				DotNetStatement statement = PsiTreeUtil.getParentOfType(parameters.getPosition(), DotNetStatement.class);
				if(statement == null)
				{
					return;
				}

				final PsiElement maybeTryStatement = UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(statement, true);
				if(maybeTryStatement instanceof CSharpTryStatementImpl)
				{
					CSharpCompletionUtil.tokenSetToLookup(result, ourCatchFinallyKeywords, new NotNullPairFunction<LookupElementBuilder,
									IElementType, LookupElement>()
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
				.class)), new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				if(CSharpModuleUtil.findLanguageVersion(parameters.getPosition()).isAtLeast(CSharpLanguageVersion._6_0))
				{
					CSharpCompletionUtil.elementToLookup(result, CSharpSoftTokens.WHEN_KEYWORD, new NotNullPairFunction<LookupElementBuilder,
							IElementType, LookupElement>()

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

		return new ExpressionOrStatementInsertHandler<LookupElement>(open, close)
		{
			@Override
			protected boolean canAddSpaceBeforePair(InsertionContext insertionContext, LookupElement item)
			{
				CommonCodeStyleSettings codeStyleSettings = insertionContext.getCodeStyleSettings();
				CSharpCodeStyleSettings customCodeStyleSettings = getCustomCodeStyleSettings(insertionContext);

				if(elementType == DO_KEYWORD)
				{
					return codeStyleSettings.SPACE_BEFORE_DO_LBRACE;
				}
				else if(elementType == TRY_KEYWORD)
				{
					return codeStyleSettings.SPACE_BEFORE_TRY_LBRACE;
				}
				else if(elementType == CATCH_KEYWORD)
				{
					return codeStyleSettings.SPACE_BEFORE_CATCH_LBRACE;
				}
				else if(elementType == FINALLY_KEYWORD)
				{
					return codeStyleSettings.SPACE_BEFORE_FINALLY_LBRACE;
				}
				else if(elementType == IF_KEYWORD)
				{
					return codeStyleSettings.SPACE_BEFORE_IF_PARENTHESES;
				}
				else if(elementType == SWITCH_KEYWORD)
				{
					return codeStyleSettings.SPACE_BEFORE_SWITCH_PARENTHESES;
				}
				else if(elementType == WHILE_KEYWORD)
				{
					return codeStyleSettings.SPACE_BEFORE_WHILE_PARENTHESES;
				}
				else if(elementType == FOR_KEYWORD)
				{
					return codeStyleSettings.SPACE_BEFORE_FOR_LBRACE;
				}
				else if(elementType == FOREACH_KEYWORD)
				{
					return customCodeStyleSettings.SPACE_BEFORE_FOREACH_PARENTHESES;
				}
				else if(elementType == USING_KEYWORD)
				{
					return customCodeStyleSettings.SPACE_BEFORE_USING_PARENTHESES;
				}
				else if(elementType == LOCK_KEYWORD)
				{
					return customCodeStyleSettings.SPACE_BEFORE_LOCK_PARENTHESES;
				}
				else if(elementType == UNSAFE_KEYWORD)
				{
					return customCodeStyleSettings.SPACE_BEFORE_UNSAFE_LBRACE;
				}
				return false;
			}
		};
	}
}
