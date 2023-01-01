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

package consulo.csharp.impl.ide.completion;

import consulo.application.util.ConcurrentFactoryMap;
import consulo.csharp.impl.ide.completion.util.SpaceInsertHandler;
import consulo.csharp.lang.impl.psi.source.CSharpConstructorSuperCallImpl;
import consulo.csharp.lang.impl.psi.source.CSharpPsiUtilImpl;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.PsiElement;
import consulo.util.dataholder.Key;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * @author VISTALL
 * @since 12.06.14
 */
public class CSharpCompletionUtil
{
	public static BiFunction<LookupElementBuilder, IElementType, LookupElement> ourSpaceInsert = (b, t) -> b.withInsertHandler(SpaceInsertHandler.INSTANCE);

	private static Map<IElementType, String> ourCache = ConcurrentFactoryMap.createMap(elementType ->
	{
		if(elementType == CSharpTokens.NULL_LITERAL)
		{
			return "null";
		}
		return elementType.toString().replace("_KEYWORD", "").toLowerCase(Locale.US);
	});

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

				if(parent.getParent() instanceof CSharpConstructorSuperCallImpl)
				{
					return false;
				}

				CSharpReferenceExpression.ResolveToKind kind = ((CSharpReferenceExpression) parent).kind();
				if(kind == CSharpReferenceExpression.ResolveToKind.TYPE_LIKE || kind == CSharpReferenceExpression.ResolveToKind.CONSTRUCTOR || kind == CSharpReferenceExpression.ResolveToKind
						.EXPRESSION_OR_TYPE_LIKE || kind == CSharpReferenceExpression.ResolveToKind.ANY_MEMBER)
				{
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isTypeLikeElementWithNamespace(@Nonnull PsiElement element)
	{
		return CSharpPsiUtilImpl.isTypeLikeElement(element) || element instanceof DotNetNamespaceAsElement;
	}

	@Nonnull
	public static String textOfKeyword(IElementType elementType)
	{
		String firstElement = ourCache.get(elementType);
		assert firstElement != null;
		return firstElement;
	}

	public static void tokenSetToLookup(@Nonnull CompletionResultSet resultSet,
			@Nonnull TokenSet tokenSet,
			@Nullable BiFunction<LookupElementBuilder, IElementType, LookupElement> decorator,
			@Nullable Predicate<IElementType> condition)
	{
		for(IElementType elementType : tokenSet.getTypes())
		{
			elementToLookup(resultSet, elementType, decorator, condition);
		}
	}

	public static void elementToLookup(@Nonnull CompletionResultSet resultSet,
			@Nonnull IElementType elementType,
			@Nullable BiFunction<LookupElementBuilder, IElementType, LookupElement> decorator,
			@Nullable Predicate<IElementType> condition)
	{
		if(condition != null && !condition.test(elementType))
		{
			return;
		}

		String keyword = ourCache.get(elementType);
		LookupElementBuilder builder = LookupElementBuilder.create(elementType, keyword);
		builder = builder.bold();
		LookupElement item = builder;
		if(decorator != null)
		{
			item = decorator.apply(builder, elementType);
		}

		item.putUserData(KEYWORD_ELEMENT_TYPE, elementType);
		resultSet.addElement(item);
	}
}
