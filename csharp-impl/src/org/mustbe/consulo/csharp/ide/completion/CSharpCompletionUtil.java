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

import java.util.Locale;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPsiUtilImpl;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.NotNullPairFunction;
import com.intellij.util.containers.ConcurrentFactoryMap;

/**
 * @author VISTALL
 * @since 12.06.14
 */
public class CSharpCompletionUtil
{
	public static final double MIN_PRIORITY = 5;

	public static final double NORMAL_PRIORITY = 5;

	public static final double MAX_PRIORITY = 10;

	private static Map<IElementType, String> ourCache = new ConcurrentFactoryMap<IElementType, String>()
	{
		@Nullable
		@Override
		protected String create(IElementType elementType)
		{
			if(elementType == CSharpTokens.NULL_LITERAL)
			{
				return "null";
			}
			return elementType.toString().replace("_KEYWORD", "").toLowerCase(Locale.US);
		}
	};

	public static final Key<IElementType> KEYWORD_ELEMENT_TYPE = Key.create("csharp.keyword.element.type");

	public static boolean mayStartClassName(CompletionResultSet result)
	{
		return StringUtil.isNotEmpty(result.getPrefixMatcher().getPrefix());
	}

	public static boolean isClassNamePossible(CompletionParameters parameters)
	{
		PsiElement position = parameters.getPosition();
		if(position.getNode().getElementType() == CSharpTokens.IDENTIFIER)
		{
			PsiElement parent = position.getParent();
			if(parent instanceof CSharpReferenceExpression)
			{
				if(((CSharpReferenceExpression) parent).getQualifier() != null)
				{
					return false;
				}

				CSharpReferenceExpression.ResolveToKind kind = ((CSharpReferenceExpression) parent).kind();
				if(kind == CSharpReferenceExpression.ResolveToKind.TYPE_LIKE ||
						kind == CSharpReferenceExpression.ResolveToKind.CONSTRUCTOR ||
						kind == CSharpReferenceExpression.ResolveToKind.EXPRESSION_OR_TYPE_LIKE ||
						kind == CSharpReferenceExpression.ResolveToKind.ANY_MEMBER)
				{
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isTypeLikeElementWithNamespace(@NotNull PsiElement element)
	{
		return CSharpPsiUtilImpl.isTypeLikeElement(element) || element instanceof DotNetNamespaceAsElement;
	}

	@NotNull
	public static String textOfKeyword(IElementType elementType)
	{
		String firstElement = ourCache.get(elementType);
		assert firstElement != null;
		return firstElement;
	}

	public static void tokenSetToLookup(@NotNull CompletionResultSet resultSet,
			@NotNull TokenSet tokenSet,
			@Nullable NotNullPairFunction<LookupElementBuilder, IElementType, LookupElement> decorator,
			@Nullable Condition<IElementType> condition)
	{
		for(IElementType elementType : tokenSet.getTypes())
		{
			elementToLookup(resultSet, elementType, decorator, condition);
		}
	}

	public static void elementToLookup(@NotNull CompletionResultSet resultSet,
			@NotNull IElementType elementType,
			@Nullable NotNullPairFunction<LookupElementBuilder, IElementType, LookupElement> decorator,
			@Nullable Condition<IElementType> condition)
	{
		if(condition != null && !condition.value(elementType))
		{
			return;
		}

		String keyword = ourCache.get(elementType);
		LookupElementBuilder builder = LookupElementBuilder.create(elementType, keyword);
		builder = builder.bold();
		LookupElement item = builder;
		if(decorator != null)
		{
			item = decorator.fun(builder, elementType);
		}

		item.putUserData(KEYWORD_ELEMENT_TYPE, elementType);
		resultSet.addElement(item);
	}
}
