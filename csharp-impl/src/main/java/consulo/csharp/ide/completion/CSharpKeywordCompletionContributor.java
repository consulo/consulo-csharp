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

import consulo.language.editor.completion.CompletionContributor;
import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.completion.CompletionType;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.PsiElement;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.psi.PsiUtilCore;
import consulo.language.util.ProcessingContext;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.editor.completion.CompletionProvider;
import consulo.csharp.ide.completion.patterns.CSharpPatterns;
import consulo.csharp.ide.completion.util.SpaceInsertHandler;
import consulo.csharp.lang.impl.psi.CSharpPreprocesorTokens;
import consulo.csharp.lang.impl.psi.CSharpPreprocessorElements;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.impl.psi.source.CSharpConstructorSuperCallImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpModuleUtil;
import consulo.dotnet.psi.*;
import consulo.language.editor.completion.CompletionUtilCore;
import consulo.language.editor.completion.lookup.ParenthesesInsertHandler;
import consulo.util.collection.ArrayUtil;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static consulo.language.pattern.StandardPatterns.psiElement;

/**
 * @author VISTALL
 * @since 07.01.14.
 */
class CSharpKeywordCompletionContributor
{
	private static final Map<String, Boolean> ourPreprocessorDirectives = new HashMap<>();

	static
	{
		ourPreprocessorDirectives.put("region", Boolean.TRUE);
		ourPreprocessorDirectives.put("endregion", Boolean.FALSE);
		ourPreprocessorDirectives.put("if", Boolean.TRUE);
		ourPreprocessorDirectives.put("elif", Boolean.TRUE);
		ourPreprocessorDirectives.put("else", Boolean.TRUE);
		ourPreprocessorDirectives.put("endif", Boolean.FALSE);
		ourPreprocessorDirectives.put("pragma", Boolean.TRUE);
		ourPreprocessorDirectives.put("nullable", Boolean.TRUE);
		ourPreprocessorDirectives.put("warning", Boolean.TRUE);
	}

	private static final TokenSet THIS_OR_BASE = TokenSet.create(CSharpTokens.THIS_KEYWORD, CSharpTokens.BASE_KEYWORD);

	static void extend(CompletionContributor contributor)
	{
		contributor.extend(CompletionType.BASIC, psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpression.class).withSuperParent(2, CSharpConstructorSuperCallImpl.class),
				(parameters, context, result) ->
		{
			CSharpTypeDeclaration typeDeclaration = PsiTreeUtil.getParentOfType(parameters.getPosition(), CSharpTypeDeclaration.class);
			if(typeDeclaration == null)
			{
				return;
			}

			CSharpCompletionUtil.tokenSetToLookup(result, THIS_OR_BASE, (b, elementType) -> b.withInsertHandler(ParenthesesInsertHandler.getInstance(true)), null);
		});

		contributor.extend(CompletionType.BASIC, psiElement(CSharpPreprocesorTokens.ILLEGAL_KEYWORD), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				PsiElement position = parameters.getPosition();
				String trim = position.getText().replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "").trim();
				if(!trim.equals("#"))
				{
					return;
				}

				for(Map.Entry<String, Boolean> entry : ourPreprocessorDirectives.entrySet())
				{
					LookupElementBuilder element = LookupElementBuilder.create(entry.getKey()).withPresentableText("#" + entry.getKey());
					if(entry.getValue())
					{
						element = element.withInsertHandler(SpaceInsertHandler.INSTANCE);
					}
					element = element.bold();
					result.addElement(element);
				}
			}
		});

		contributor.extend(CompletionType.BASIC, CSharpPatterns.fieldStart(), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				CSharpFieldDeclaration fieldDeclaration = PsiTreeUtil.getParentOfType(parameters.getPosition(), CSharpFieldDeclaration.class);
				assert fieldDeclaration != null;
				if(fieldDeclaration.isConstant() || fieldDeclaration.hasModifier(CSharpModifier.STATIC))
				{
					return;
				}

				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.CONST_KEYWORD, CSharpCompletionUtil.ourSpaceInsert, null);
			}
		});

		contributor.extend(CompletionType.BASIC, psiElement().afterLeaf(psiElement().withElementType(CSharpTokens.USING_KEYWORD)).inside(CSharpUsingNamespaceStatement.class), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull final CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.STATIC_KEYWORD, null, new Predicate<IElementType>()
				{
					@Override
					@RequiredReadAction
					public boolean test(IElementType elementType)
					{
						return CSharpModuleUtil.findLanguageVersion(parameters.getPosition()).isAtLeast(CSharpLanguageVersion._6_0);
					}
				});
			}
		});

		contributor.extend(CompletionType.BASIC, psiElement().inside(DotNetGenericParameter.class), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull final CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				CSharpCompletionUtil.tokenSetToLookup(result, TokenSet.create(CSharpTokens.IN_KEYWORD, CSharpTokens.OUT_KEYWORD), null, new Predicate<IElementType>()
				{
					@Override
					@RequiredReadAction
					public boolean test(IElementType elementType)
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

		contributor.extend(CompletionType.BASIC, psiElement(), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				PsiElement position = parameters.getPosition();
				if(isCorrectPosition(position))
				{
					CSharpCompletionUtil.elementToLookup(result, CSharpSoftTokens.WHERE_KEYWORD, CSharpCompletionUtil.ourSpaceInsert, null);
				}
			}

			@RequiredReadAction
			private boolean isCorrectPosition(PsiElement position)
			{
				PsiElement prev = PsiTreeUtil.prevVisibleLeaf(position);
				if(prev == null)
				{
					return false;
				}

				if(PsiUtilCore.getElementType(prev) == CSharpTokens.RPAR)
				{
					DotNetGenericParameterListOwner listOwner = PsiTreeUtil.getParentOfType(prev, DotNetGenericParameterListOwner.class);
					if(listOwner == null || listOwner.getGenericParametersCount() == 0)
					{
						return false;
					}

					return true;
				}
				return false;
			}
		});

		contributor.extend(CompletionType.BASIC, CSharpPatterns.expressionStart().inside(CSharpUserType.class).inside(CSharpGenericConstraint.class).afterLeaf(psiElement(CSharpTokens.COLON)), new
				CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				TokenSet set = TokenSet.create(CSharpTokens.CLASS_KEYWORD, CSharpTokens.STRUCT_KEYWORD);
				CSharpCompletionUtil.tokenSetToLookup(result, set, null, null);
			}
		});

		contributor.extend(CompletionType.BASIC, psiElement(), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull final CompletionParameters parameters, ProcessingContext processingContext, @Nonnull CompletionResultSet completionResultSet)
			{
				final PsiElement position = parameters.getPosition();
				if(position.getParent() instanceof DotNetReferenceExpression && position.getParent().getParent() instanceof DotNetUserType)
				{
					if(((DotNetReferenceExpression) position.getParent()).getQualifier() != null)
					{
						return;
					}

					PsiElement parent1 = position.getParent().getParent();

					// dont allow inside statements
					DotNetStatement statementParent = PsiTreeUtil.getParentOfType(parent1, DotNetStatement.class);
					if(statementParent != null)
					{
						return;
					}

					PsiElement prevSibling = PsiTreeUtil.prevVisibleLeaf(parent1);
					if(prevSibling == null || prevSibling.getNode().getElementType() == CSharpTokens.LBRACE || prevSibling.getNode().getElementType() == CSharpTokens.RBRACE || prevSibling.getNode()
							.getElementType() == CSharpTokens.LPAR || prevSibling.getNode().getElementType() == CSharpTokens.COMMA || prevSibling.getNode().getElementType() == CSharpTokens.RBRACKET
							|| prevSibling.getNode().getElementType() == CSharpTokens.SEMICOLON || prevSibling.getNode().getElementType() == CSharpPreprocessorElements.PREPROCESSOR_DIRECTIVE ||
							CSharpTokenSets.MODIFIERS.contains(prevSibling.getNode().getElementType()))
					{
						TokenSet tokenVal = TokenSet.orSet(CSharpTokenSets.MODIFIERS, CSharpTokenSets.TYPE_DECLARATION_START, TokenSet.create(CSharpTokens.DELEGATE_KEYWORD, CSharpTokens
								.NAMESPACE_KEYWORD));

						CSharpCompletionUtil.tokenSetToLookup(completionResultSet, tokenVal, CSharpCompletionUtil.ourSpaceInsert, new Predicate<IElementType>()
						{
							@Override
							@RequiredReadAction
							public boolean test(IElementType elementType)
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

								if(elementType == CSharpTokens.REF_KEYWORD || elementType == CSharpTokens.OUT_KEYWORD || elementType == CSharpTokens.THIS_KEYWORD || elementType == CSharpTokens
										.PARAMS_KEYWORD)
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
						});
					}
				}
			}
		});
	}
}
