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

package consulo.csharp.ide.completion;

import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.Editor;
import consulo.csharp.ide.completion.insertHandler.CSharpTailInsertHandler;
import consulo.csharp.ide.completion.patterns.CSharpPatterns;
import consulo.csharp.ide.completion.util.CSharpWeightInsertHandler;
import consulo.csharp.ide.completion.util.ExpressionOrStatementInsertHandler;
import consulo.csharp.ide.completion.util.SpaceInsertHandler;
import consulo.csharp.lang.impl.psi.CSharpImplicitReturnModel;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.impl.psi.UsefulPsiTreeUtil;
import consulo.csharp.lang.impl.psi.source.*;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.lang.psi.CSharpSoftTokens;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpModuleUtil;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetStatement;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.editor.AutoPopupController;
import consulo.language.editor.completion.*;
import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.TailType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;

import javax.annotation.Nonnull;

import static consulo.language.pattern.StandardPatterns.or;
import static consulo.language.pattern.StandardPatterns.psiElement;

/**
 * @author VISTALL
 * @since 12.06.14
 */
class CSharpStatementCompletionContributor implements CSharpTokenSets
{
	private static class ReturnStatementInsertHandler implements InsertHandler<LookupElement>
	{
		private final CSharpSimpleLikeMethodAsElement myPseudoMethod;

		public ReturnStatementInsertHandler(CSharpSimpleLikeMethodAsElement pseudoMethod)
		{
			myPseudoMethod = pseudoMethod;
		}

		@Override
		@RequiredReadAction
		public void handleInsert(InsertionContext insertionContext, LookupElement item)
		{
			Editor editor = insertionContext.getEditor();
			int offset = editor.getCaretModel().getOffset();
			boolean isVoidReturnType = DotNetTypeRefUtil.isVmQNameEqual(myPseudoMethod.getReturnTypeRef(), DotNetTypes.System.Void);

			if(isVoidReturnType)
			{
				if(insertionContext.getCompletionChar() == '\n')
				{
					TailType.insertChar(editor, offset, ';');
					insertionContext.getEditor().getCaretModel().moveToOffset(offset + 1);
				}
			}
			else
			{
				if(insertionContext.getCompletionChar() == '\n')
				{
					TailType.insertChar(editor, offset, ' ');

					insertionContext.getEditor().getCaretModel().moveToOffset(offset + 1);
				}

				AutoPopupController.getInstance(editor.getProject()).autoPopupMemberLookup(editor, null);
			}
		}
	}

	private static final TokenSet ourParStatementKeywords = TokenSet.create(IF_KEYWORD, FOR_KEYWORD, FOREACH_KEYWORD, FOREACH_KEYWORD, FIXED_KEYWORD, UNCHECKED_KEYWORD, CHECKED_KEYWORD,
			SWITCH_KEYWORD, USING_KEYWORD, WHILE_KEYWORD, DO_KEYWORD, UNSAFE_KEYWORD, TRY_KEYWORD, LOCK_KEYWORD);

	private static final TokenSet ourCaseAndDefaultKeywords = TokenSet.create(CASE_KEYWORD, DEFAULT_KEYWORD);

	private static final TokenSet ourContinueAndBreakKeywords = TokenSet.create(BREAK_KEYWORD, CONTINUE_KEYWORD, GOTO_KEYWORD);

	private static final TokenSet ourCatchFinallyKeywords = TokenSet.create(CATCH_KEYWORD, FINALLY_KEYWORD);


	static void extend(CompletionContributor contributor)
	{
		contributor.extend(CompletionType.BASIC, CSharpPatterns.statementStart().inside(or(psiElement().inside(CSharpForeachStatementImpl.class), psiElement().inside(CSharpForStatementImpl.class),
				psiElement().inside(CSharpWhileStatementImpl.class), psiElement().inside(CSharpDoWhileStatementImpl.class))), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull final CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				CSharpCompletionUtil.tokenSetToLookup(result, ourContinueAndBreakKeywords, (t, v) ->
				{
					t = t.withInsertHandler(CSharpWeightInsertHandler.INSTANCE);
					return t;
				}, null);
			}
		});

		contributor.extend(CompletionType.BASIC, CSharpPatterns.statementStart().inside(psiElement().inside(CSharpSimpleLikeMethodAsElement.class)).andNot(psiElement().inside
				(CSharpFinallyStatementImpl.class)), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull final CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				final CSharpSimpleLikeMethodAsElement pseudoMethod = PsiTreeUtil.getParentOfType(parameters.getPosition(), CSharpSimpleLikeMethodAsElement.class);
				assert pseudoMethod != null;
				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.RETURN_KEYWORD, (t, v) ->
				{
					t = t.withInsertHandler(new ReturnStatementInsertHandler(pseudoMethod));
					return t;
				}, null);
			}
		});

		contributor.extend(CompletionType.BASIC, CSharpPatterns.statementStart().inside(CSharpLabeledStatementImpl.class), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull final CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				CSharpCompletionUtil.tokenSetToLookup(result, ourContinueAndBreakKeywords, (t, v) ->
				{
					t = t.withInsertHandler((insertionContext, item) ->
					{
						int offset = insertionContext.getEditor().getCaretModel().getOffset();
						insertionContext.getDocument().insertString(offset, " ;");

						insertionContext.getEditor().getCaretModel().moveToOffset(offset + 1);
						Editor editor = parameters.getEditor();
						AutoPopupController.getInstance(editor.getProject()).autoPopupMemberLookup(editor, null);
					});
					return t;
				}, null);
			}
		});

		contributor.extend(CompletionType.BASIC, CSharpPatterns.statementStart().withSuperParent(6, CSharpSwitchStatementImpl.class), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.BREAK_KEYWORD, null, null);
				CSharpCompletionUtil.tokenSetToLookup(result, ourCaseAndDefaultKeywords, (t, v) ->
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
				}, null);
			}
		});

		contributor.extend(CompletionType.BASIC, CSharpPatterns.statementStart(), new CompletionProvider()
		{
			@Override
			@RequiredReadAction
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				CSharpCompletionUtil.tokenSetToLookup(result, ourParStatementKeywords, (t, v) ->
				{
					t = t.withInsertHandler(buildInsertHandler(v));
					return t;
				}, null);
				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.THROW_KEYWORD, CSharpCompletionUtil.ourSpaceInsert, null);
			}
		});

		contributor.extend(CompletionType.BASIC, CSharpPatterns.statementStart(), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				final PsiElement position = parameters.getPosition();
				CSharpCompletionUtil.elementToLookup(result, CSharpSoftTokens.YIELD_KEYWORD, (t, v) ->
				{
					t = t.withInsertHandler((insertionContext, item) ->
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
					});
					return t;
				}, elementType ->
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
				});
			}
		});

		contributor.extend(CompletionType.BASIC, CSharpPatterns.statementStart(), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.CONST_KEYWORD, CSharpCompletionUtil.ourSpaceInsert, null);
			}
		});

		contributor.extend(CompletionType.BASIC, psiElement().afterLeaf(psiElement().withElementType(CSharpSoftTokens.YIELD_KEYWORD)), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				final PsiElement position = parameters.getPosition();
				final CSharpSimpleLikeMethodAsElement methodAsElement = PsiTreeUtil.getParentOfType(position, CSharpSimpleLikeMethodAsElement.class);
				if(methodAsElement == null)
				{
					return;
				}
				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.BREAK_KEYWORD, null, null);
				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.RETURN_KEYWORD, (t, v) ->
				{
					t = t.withInsertHandler(new ReturnStatementInsertHandler(methodAsElement));
					return t;
				}, null);
			}
		});

		contributor.extend(CompletionType.BASIC, psiElement().afterLeaf(psiElement().withElementType(CSharpTokens.ELSE_KEYWORD)).withSuperParent(2, CSharpExpressionStatementImpl.class), new
				CompletionProvider()
				{
					@RequiredReadAction
					@Override
					public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
					{
						CSharpCompletionUtil.elementToLookup(result, CSharpTokens.IF_KEYWORD, (t, v) ->
						{
							t = t.withInsertHandler(buildInsertHandler(v));

							return t;
						}, null);
					}
				});

		contributor.extend(CompletionType.BASIC, CSharpPatterns.statementStart(), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				DotNetStatement statement = PsiTreeUtil.getParentOfType(parameters.getPosition(), DotNetStatement.class);
				if(statement == null)
				{
					return;
				}

				final PsiElement maybeTryStatement = UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(statement, true);
				if(maybeTryStatement instanceof CSharpTryStatementImpl)
				{
					CSharpCompletionUtil.tokenSetToLookup(result, ourCatchFinallyKeywords, (t, v) ->
					{
						t = t.withInsertHandler(buildInsertHandler(v));
						return t;
					}, elementType ->
					{
						CSharpTryStatementImpl st = (CSharpTryStatementImpl) maybeTryStatement;
						return st.getFinallyStatement() == null;
					});
				}
				else if(maybeTryStatement instanceof CSharpIfStatementImpl)
				{
					CSharpCompletionUtil.elementToLookup(result, CSharpTokens.ELSE_KEYWORD, null, null);
				}
			}
		});

		contributor.extend(CompletionType.BASIC, psiElement().afterLeaf(psiElement().withElementType(CSharpTokens.RPAR).withParent(CSharpCatchStatementImpl.class)), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				if(CSharpModuleUtil.findLanguageVersion(parameters.getPosition()).isAtLeast(CSharpLanguageVersion._6_0))
				{
					CSharpCompletionUtil.elementToLookup(result, CSharpSoftTokens.WHEN_KEYWORD, (t, v) ->
					{
						t = t.withInsertHandler(buildInsertHandler(v));
						return t;
					}, null);
				}
			}
		});
	}

	@Nonnull
	public static InsertHandler<LookupElement> buildInsertHandler(final IElementType elementType)
	{
		char open = '(';
		char close = ')';

		if(elementType == DO_KEYWORD || elementType == TRY_KEYWORD || elementType == CATCH_KEYWORD || elementType == UNSAFE_KEYWORD || elementType == FINALLY_KEYWORD)
		{
			open = '{';
			close = '}';
		}

		return new ExpressionOrStatementInsertHandler<>(open, close);
	}
}
