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

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeInfo;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeRefProvider;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpressionEx;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceExpression;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import org.mustbe.consulo.dotnet.psi.DotNetUserType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.NotNullPairFunction;
import com.intellij.util.ProcessingContext;
import lombok.val;

/**
 * @author VISTALL
 * @since 07.01.14.
 */
public class CSharpKeywordCompletionContributor extends CompletionContributor
{
	private static final TokenSet ourExpressionLiterals = TokenSet.create(CSharpTokens.NULL_LITERAL, CSharpTokens.BOOL_LITERAL,
			CSharpTokens.DEFAULT_KEYWORD, CSharpTokens.TYPEOF_KEYWORD, CSharpTokens.SIZEOF_KEYWORD);

	public CSharpKeywordCompletionContributor()
	{
		extend(CompletionType.BASIC, psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpressionEx.class),
				new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				val parent = (CSharpReferenceExpressionEx) parameters.getPosition().getParent();
				if(parent.getQualifier() == null && parent.kind() == CSharpReferenceExpression.ResolveToKind.ANY_MEMBER)
				{
					CSharpCompletionUtil.tokenSetToLookup(result, ourExpressionLiterals, new NotNullPairFunction<LookupElementBuilder, IElementType,
							LookupElement>()
					{
						@NotNull
						@Override
						public LookupElement fun(LookupElementBuilder t, IElementType elementType)
						{
							if(elementType == CSharpTokens.DEFAULT_KEYWORD ||
									elementType == CSharpTokens.TYPEOF_KEYWORD ||
									elementType == CSharpTokens.SIZEOF_KEYWORD)
							{
								t = t.withTailText("(...)", true);
								t = t.withInsertHandler(ParenthesesInsertHandler.getInstance(true));
							}

							List<ExpectedTypeInfo> expectedTypeRefs = ExpectedTypeRefProvider.findExpectedTypeRefs(parent);
							if(!expectedTypeRefs.isEmpty())
							{
								if(elementType == CSharpTokens.NULL_LITERAL)
								{
									for(ExpectedTypeInfo expectedTypeRef : expectedTypeRefs)
									{
										DotNetTypeResolveResult typeResolveResult = expectedTypeRef.getTypeRef().resolve(parent);
										if(typeResolveResult.isNullable())
										{
											return PrioritizedLookupElement.withPriority(t, 2);
										}
									}
								}
								else
								{
									DotNetTypeRef typeRefForToken = typeRefFromTokeType(elementType);
									if(typeRefForToken == null)
									{
										return t;
									}
									for(ExpectedTypeInfo expectedTypeRef : expectedTypeRefs)
									{
										if(CSharpTypeUtil.isInheritable(expectedTypeRef.getTypeRef(), typeRefForToken, parent))
										{
											return PrioritizedLookupElement.withPriority(t, 2);
										}
									}
								}
							}
							return t;
						}
					}, null);
				}
			}
		});

		extend(CompletionType.BASIC, psiElement(), new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters completionParameters,
					ProcessingContext processingContext,
					@NotNull CompletionResultSet completionResultSet)
			{
				PsiElement position = completionParameters.getPosition();
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
							prevSibling.getNode().getElementType() == CSharpTokens.SEMICOLON ||
							CSharpTokenSets.MODIFIERS.contains(prevSibling.getNode().getElementType()))
					{
						val tokenVal = TokenSet.orSet(CSharpTokenSets.MODIFIERS, CSharpTokenSets.TYPE_DECLARATION_START);

						CSharpCompletionUtil.tokenSetToLookup(completionResultSet, tokenVal, null, null);
					}
				}
			}
		});
	}

	@Nullable
	private static DotNetTypeRef typeRefFromTokeType(@NotNull IElementType e)
	{
		if(e == CSharpTokens.BOOL_LITERAL)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.Boolean);
		}
		else if(e == CSharpTokens.TYPEOF_KEYWORD)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.Type);
		}
		else if(e == CSharpTokens.SIZEOF_KEYWORD)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.Int32);
		}
		return null;
	}
}
