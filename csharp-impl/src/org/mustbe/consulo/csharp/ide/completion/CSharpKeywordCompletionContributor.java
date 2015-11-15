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

import static com.intellij.patterns.StandardPatterns.psiElement;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.completion.util.SpaceInsertHandler;
import org.mustbe.consulo.csharp.lang.psi.*;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleUtil;
import org.mustbe.consulo.dotnet.DotNetRunUtil;
import org.mustbe.consulo.dotnet.psi.*;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.NotNullPairFunction;
import com.intellij.util.ProcessingContext;

/**
 * @author VISTALL
 * @since 07.01.14.
 */
public class CSharpKeywordCompletionContributor extends CompletionContributor
{
	private static final TokenSet ourExpressionLiterals = TokenSet.create(CSharpTokens.NULL_LITERAL, CSharpTokens.FALSE_KEYWORD, CSharpTokens.TRUE_KEYWORD, CSharpTokens.DEFAULT_KEYWORD,
			CSharpTokens.TYPEOF_KEYWORD, CSharpTokens.SIZEOF_KEYWORD, CSharpTokens.THIS_KEYWORD, CSharpTokens.BASE_KEYWORD, CSharpSoftTokens.AWAIT_KEYWORD, CSharpTokens.NEW_KEYWORD,
			CSharpTokens.__MAKEREF_KEYWORD, CSharpTokens.__REFTYPE_KEYWORD, CSharpTokens.__REFVALUE_KEYWORD, CSharpSoftTokens.NAMEOF_KEYWORD);

	public CSharpKeywordCompletionContributor()
	{
		extend(CompletionType.BASIC, psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpressionEx.class), new CompletionProvider<CompletionParameters>()
		{
			@RequiredReadAction
			@Override
			protected void addCompletions(@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				final CSharpReferenceExpressionEx parent = (CSharpReferenceExpressionEx) parameters.getPosition().getParent();
				if(parent.getQualifier() == null && (parent.kind() == CSharpReferenceExpression.ResolveToKind.ANY_MEMBER || parent.kind() == CSharpReferenceExpression.ResolveToKind
						.EXPRESSION_OR_TYPE_LIKE))
				{
					CSharpCompletionUtil.tokenSetToLookup(result, ourExpressionLiterals, new NotNullPairFunction<LookupElementBuilder, IElementType, LookupElement>()
							{
								@NotNull
								@Override
								public LookupElement fun(LookupElementBuilder t, IElementType elementType)
								{
									if(elementType == CSharpTokens.DEFAULT_KEYWORD ||
											elementType == CSharpTokens.TYPEOF_KEYWORD ||
											elementType == CSharpSoftTokens.NAMEOF_KEYWORD ||
											elementType == CSharpTokens.__MAKEREF_KEYWORD ||
											elementType == CSharpTokens.__REFTYPE_KEYWORD ||
											elementType == CSharpTokens.__REFVALUE_KEYWORD ||
											elementType == CSharpTokens.SIZEOF_KEYWORD)
									{
										t = t.withTailText("(...)", true);
										t = t.withInsertHandler(ParenthesesInsertHandler.getInstance(true));
									}
									else if(elementType == CSharpTokens.NEW_KEYWORD)
									{
										t = t.withInsertHandler(SpaceInsertHandler.INSTANCE);
									}
									else if(elementType == CSharpSoftTokens.AWAIT_KEYWORD)
									{
										t = t.withInsertHandler(new InsertHandler<LookupElement>()
										{
											@Override
											@RequiredDispatchThread
											public void handleInsert(InsertionContext context, LookupElement item)
											{
												CSharpSimpleLikeMethodAsElement methodAsElement = PsiTreeUtil.getParentOfType(parameters.getOriginalPosition(), CSharpSimpleLikeMethodAsElement.class);
												if(methodAsElement != null && ! methodAsElement.hasModifier(CSharpModifier.ASYNC))
												{
													methodAsElement.addModifier(CSharpModifier.ASYNC);
													PsiDocumentManager.getInstance(context.getProject()).doPostponedOperationsAndUnblockDocument(context.getDocument());
												}

												SpaceInsertHandler.INSTANCE.handleInsert(context, item);
											}
										});
									}
									return t;
								}
							}, new Condition<IElementType>()
							{
								@Override
								@RequiredReadAction
								public boolean value(IElementType elementType)
								{
									if(elementType == CSharpTokens.BASE_KEYWORD || elementType == CSharpTokens.THIS_KEYWORD)
									{
										DotNetModifierListOwner owner = (DotNetModifierListOwner) PsiTreeUtil.getParentOfType(parent, DotNetQualifiedElement.class);
										if(owner == null || owner.hasModifier(DotNetModifier.STATIC))
										{
											return false;
										}
										return true;
									}
									if(elementType == CSharpSoftTokens.AWAIT_KEYWORD)
									{
										CSharpSimpleLikeMethodAsElement methodAsElement = PsiTreeUtil.getParentOfType(parameters.getOriginalPosition(), CSharpSimpleLikeMethodAsElement.class);

										if(methodAsElement instanceof DotNetMethodDeclaration && DotNetRunUtil.isEntryPoint((DotNetMethodDeclaration) methodAsElement))
										{
											return false;
										}
										if(methodAsElement == null)
										{
											return false;
										}

										return CSharpModuleUtil.findLanguageVersion(parent).isAtLeast(CSharpLanguageVersion._4_0);
									}
									if(elementType == CSharpSoftTokens.NAMEOF_KEYWORD)
									{
										return CSharpModuleUtil.findLanguageVersion(parent).isAtLeast(CSharpLanguageVersion._6_0);
									}
									return true;
								}
							}
					);
				}
			}
		});

		extend(CompletionType.BASIC, psiElement().afterLeaf(psiElement().withElementType(CSharpTokens.USING_KEYWORD)).inside(CSharpUsingNamespaceStatement.class),
				new CompletionProvider<CompletionParameters>()
		{
			@RequiredReadAction
			@Override
			protected void addCompletions(@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.STATIC_KEYWORD, null, new Condition<IElementType>()
				{
					@Override
					@RequiredReadAction
					public boolean value(IElementType elementType)
					{
						return CSharpModuleUtil.findLanguageVersion(parameters.getPosition()).isAtLeast(CSharpLanguageVersion._6_0);
					}
				});
			}
		});

		extend(CompletionType.BASIC, psiElement().inside(DotNetGenericParameter.class), new CompletionProvider<CompletionParameters>()
		{
			@RequiredReadAction
			@Override
			protected void addCompletions(@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpCompletionUtil.tokenSetToLookup(result, TokenSet.create(CSharpTokens.IN_KEYWORD, CSharpTokens.OUT_KEYWORD), null, new Condition<IElementType>()
				{
					@Override
					@RequiredReadAction
					public boolean value(IElementType elementType)
					{
						DotNetQualifiedElement qualifiedElement = PsiTreeUtil.getParentOfType(parameters.getPosition(), CSharpMethodDeclaration.class, DotNetTypeDeclaration.class);
						if(qualifiedElement instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) qualifiedElement).isDelegate())
						{
							return true;
						}
						if(qualifiedElement instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) qualifiedElement).isInterface())
						{
							return true;
						}
						return false;
					}
				});
			}
		});

		extend(CompletionType.BASIC, psiElement(), new CompletionProvider<CompletionParameters>()
		{
			@RequiredReadAction
			@Override
			protected void addCompletions(@NotNull final CompletionParameters parameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet)
			{
				final PsiElement position = parameters.getPosition();
				if(position.getParent() instanceof DotNetReferenceExpression && position.getParent().getParent() instanceof DotNetUserType)
				{
					PsiElement parent1 = position.getParent().getParent();

					// dont allow inside statements
					DotNetStatement statementParent = PsiTreeUtil.getParentOfType(parent1, DotNetStatement.class);
					if(statementParent != null)
					{
						return;
					}

					PsiElement prevSibling = PsiTreeUtil.prevVisibleLeaf(parent1);
					if(prevSibling == null ||
							prevSibling.getNode().getElementType() == CSharpTokens.LBRACE ||
							prevSibling.getNode().getElementType() == CSharpTokens.RBRACE ||
							prevSibling.getNode().getElementType() == CSharpTokens.LPAR ||
							prevSibling.getNode().getElementType() == CSharpTokens.COMMA ||
							prevSibling.getNode().getElementType() == CSharpTokens.RBRACKET ||
							prevSibling.getNode().getElementType() == CSharpTokens.SEMICOLON ||
							CSharpTokenSets.MODIFIERS.contains(prevSibling.getNode().getElementType()))
					{
						TokenSet tokenVal = TokenSet.orSet(CSharpTokenSets.MODIFIERS, CSharpTokenSets.TYPE_DECLARATION_START, TokenSet.create(CSharpTokens.DELEGATE_KEYWORD,
								CSharpTokens.NAMESPACE_KEYWORD));

						CSharpCompletionUtil.tokenSetToLookup(completionResultSet, tokenVal, new NotNullPairFunction<LookupElementBuilder, IElementType, LookupElement>()

								{
									@NotNull
									@Override
									public LookupElement fun(LookupElementBuilder t, IElementType v)
									{
										t = t.withInsertHandler(SpaceInsertHandler.INSTANCE);
										return t;
									}
								}, new Condition<IElementType>()
								{
									@Override
									@RequiredReadAction
									public boolean value(IElementType elementType)
									{
										if(elementType == CSharpTokens.IN_KEYWORD)
										{
											return false;
										}
										if(elementType == CSharpSoftTokens.ASYNC_KEYWORD)
										{
											CSharpSimpleLikeMethodAsElement methodAsElement = PsiTreeUtil.getParentOfType(parameters.getOriginalPosition(), CSharpSimpleLikeMethodAsElement.class);

											if(methodAsElement instanceof DotNetMethodDeclaration && DotNetRunUtil.isEntryPoint((DotNetMethodDeclaration) methodAsElement))
											{
												return false;
											}

											if(!CSharpModuleUtil.findLanguageVersion(position).isAtLeast(CSharpLanguageVersion._4_0))
											{
												return false;
											}
										}

										DotNetParameter parameter = PsiTreeUtil.getParentOfType(position, DotNetParameter.class);

										if(elementType == CSharpTokens.REF_KEYWORD ||
												elementType == CSharpTokens.OUT_KEYWORD ||
												elementType == CSharpTokens.THIS_KEYWORD ||
												elementType == CSharpTokens.PARAMS_KEYWORD)
										{
											if(parameter == null)
											{
												return false;
											}
											if(elementType == CSharpTokenSets.THIS_KEYWORD)
											{
												return parameter.getIndex() == 0;
											}
											else if(elementType == CSharpTokens.PARAMS_KEYWORD)
											{
												DotNetParameterListOwner owner = parameter.getOwner();
												return owner != null && ArrayUtil.getLastElement(owner.getParameters()) == parameter;
											}
											return true;
										}

										return parameter == null;
									}
								}
						);
					}
				}
			}
		});
	}


}
