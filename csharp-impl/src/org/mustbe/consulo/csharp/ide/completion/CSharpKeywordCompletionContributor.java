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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpEventDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceExpression;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceType;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.lookup.LookupElementRenderer;
import com.intellij.openapi.util.Condition;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ConcurrentFactoryMap;
import lombok.val;

/**
 * @author VISTALL
 * @since 07.01.14.
 */
public class CSharpKeywordCompletionContributor extends CompletionContributor
{
	private static Map<IElementType, String[]> ourCache = new ConcurrentFactoryMap<IElementType, String[]>()
	{
		@Nullable
		@Override
		protected String[] create(IElementType elementType)
		{
			if(elementType == CSharpTokens.BOOL_LITERAL)
			{
				return new String[]{
						"true",
						"false"
				};
			}
			return new String[]{elementType.toString().replace("_KEYWORD", "").toLowerCase()};
		}
	};

	public CSharpKeywordCompletionContributor()
	{
		extend(CompletionType.BASIC, StandardPatterns.psiElement(), new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext,
					@NotNull CompletionResultSet completionResultSet)
			{
				PsiElement position = completionParameters.getPosition();
				if(position.getParent() instanceof DotNetReferenceExpression && position.getParent().getParent() instanceof DotNetReferenceType)
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
							CSharpTokenSets.MODIFIERS.contains(prevSibling.getNode().getElementType()))
					{
						val tokeVal = TokenSet.orSet(CSharpTokenSets.MODIFIERS, CSharpTokenSets.TYPE_DECLARATION_START);

						tokenSetToLookup(completionResultSet, tokeVal, createCondForFilterModifierOrTypeStart(parent1));
					}
				}
			}
		});

		extend(CompletionType.BASIC, StandardPatterns.psiElement(CSharpTokens.IDENTIFIER).withParent(DotNetReferenceExpression.class),
				new CompletionProvider<CompletionParameters>()
		{

			@Override
			protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext,
					@NotNull CompletionResultSet completionResultSet)
			{
				DotNetReferenceExpression parent = (DotNetReferenceExpression) completionParameters.getPosition().getParent();

				if(parent.getQualifier() == null)
				{
					tokenSetToLookup(completionResultSet, CSharpTokenSets.NATIVE_TYPES, null);
				}
			}
		});

		extend(CompletionType.BASIC, StandardPatterns.psiElement(), new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext,
					@NotNull CompletionResultSet resultSet)
			{
				PsiElement originalPosition = completionParameters.getOriginalPosition();
				if(originalPosition == null)
				{
					return;
				}

				// ; Expected
				PsiElement prevSibling = originalPosition.getPrevSibling();
				if(prevSibling == null)
				{
					return;
				}

				prevSibling = prevSibling instanceof CSharpEventDeclaration ? prevSibling : prevSibling.getPrevSibling();

				String text = null;
				if(prevSibling instanceof CSharpFieldDeclaration)
				{
					text = "{ get; set; }";
				}
				else if(prevSibling instanceof CSharpEventDeclaration)
				{
					text = "{ add; remove; }";
				}
				else
				{
					return;
				}
				LookupElementBuilder builder = LookupElementBuilder.create(text);
				builder = builder.withRenderer(new LookupElementRenderer<LookupElement>()
				{
					@Override
					public void renderElement(LookupElement lookupElement, LookupElementPresentation lookupElementPresentation)
					{
						String lookupString = lookupElement.getLookupString();
						for(char o : lookupString.toCharArray())
						{
							boolean grey = o == '{' || o == '}' || o == ';';

							lookupElementPresentation.appendTailText(String.valueOf(o), grey);
						}
					}
				});
				resultSet.addElement(builder.withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE));
			}
		});
	}

	private static void tokenSetToLookup(CompletionResultSet resultSet, TokenSet tokenSet, Condition<IElementType> condition)
	{
		for(IElementType iElementType : tokenSet.getTypes())
		{
			if(condition != null && !condition.value(iElementType))
			{
				continue;
			}

			for(String keyword : ourCache.get(iElementType))
			{
				LookupElementBuilder builder = LookupElementBuilder.create(keyword);
				builder = builder.bold();
				resultSet.addElement(builder);
			}
		}
	}

	private static Condition<IElementType> createCondForFilterModifierOrTypeStart(PsiElement parent1)
	{
		val elementTypes = new ArrayList<IElementType>();

		PsiElement prevSibling = PsiTreeUtil.prevVisibleLeaf(parent1);
		while(prevSibling != null)
		{
			IElementType elementType = prevSibling.getNode().getElementType();
			if(CSharpTokenSets.MODIFIERS.contains(elementType))
			{
				elementTypes.add(elementType);

				prevSibling = PsiTreeUtil.prevVisibleLeaf(prevSibling);
			}
			else
			{
				break;
			}
		}

		PsiElement nextSibling = PsiTreeUtil.nextVisibleLeaf(parent1);
		while(nextSibling != null)
		{
			IElementType elementType = nextSibling.getNode().getElementType();
			if(CSharpTokenSets.MODIFIERS.contains(elementType))
			{
				elementTypes.add(elementType);

				nextSibling = PsiTreeUtil.nextVisibleLeaf(nextSibling);
			}
			else if(CSharpTokenSets.TYPE_DECLARATION_START.contains(elementType))
			{
				elementTypes.addAll(Arrays.asList(CSharpTokenSets.TYPE_DECLARATION_START.getTypes()));
				break;
			}
			else
			{
				break;
			}
		}

		return new Condition<IElementType>()
		{
			@Override
			public boolean value(IElementType elementType)
			{
				return !elementTypes.contains(elementType);
			}
		};
	}
}
