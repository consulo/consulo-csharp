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

package consulo.csharp.ide.refactoring.util;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.ide.codeStyle.CSharpCodeGenerationSettings;
import consulo.csharp.lang.impl.ide.refactoring.CSharpRefactoringUtil;
import consulo.csharp.lang.impl.lexer.CSharpLexer;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.CSharpForeachStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpIndexAccessExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpMethodCallExpressionImpl;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Fedor.Korotkov
 * <p>
 * from google-dart
 */
public class CSharpNameSuggesterUtil
{
	private CSharpNameSuggesterUtil()
	{
	}

	private static String deleteNonLetterFromString(@Nonnull final String string)
	{
		Pattern pattern = Pattern.compile("[^a-zA-Z_]+");
		Matcher matcher = pattern.matcher(string);
		return matcher.replaceAll("_");
	}

	@Nonnull
	@RequiredReadAction
	public static Collection<String> getSuggestedVariableNames(final DotNetVariable variable)
	{
		PsiElement parent = variable.getParent();

		Collection<String> suggestedNames = null;
		if(parent instanceof CSharpForeachStatementImpl)
		{
			DotNetExpression iterableExpression = ((CSharpForeachStatementImpl) parent).getIterableExpression();
			if(iterableExpression != null)
			{
				suggestedNames = getSuggestedNames(iterableExpression, List.of(), true, variable);
			}
		}

		if(suggestedNames == null)
		{
			if(variable.toTypeRef(false) == DotNetTypeRef.AUTO_TYPE)
			{
				suggestedNames = ContainerUtil.newArrayList("v");
			}
			else
			{
				suggestedNames = getSuggestedNames(variable.toTypeRef(true), variable);
			}
		}

		DotNetExpression initializer = variable.getInitializer();
		if(initializer != null)
		{
			suggestedNames.addAll(getSuggestedNames(initializer, suggestedNames, false, variable));
		}

		if(variable instanceof CSharpPropertyDeclaration)
		{
			suggestedNames = ContainerUtil.map(suggestedNames, StringUtil::capitalize);
		}

		CSharpCodeGenerationSettings settings = CSharpCodeGenerationSettings.getInstance(variable.getProject());

		boolean isStatic = variable.hasModifier(DotNetModifier.STATIC);
		final String prefix;
		final String suffix;
		if(variable instanceof CSharpPropertyDeclaration)
		{
			prefix = isStatic ? settings.STATIC_PROPERTY_PREFIX : settings.PROPERTY_PREFIX;
			suffix = isStatic ? settings.STATIC_PROPERTY_SUFFIX : settings.PROPERTY_PREFIX;
		}
		else if(variable instanceof CSharpFieldDeclaration)
		{
			prefix = isStatic ? settings.STATIC_FIELD_PREFIX : settings.FIELD_PREFIX;
			suffix = isStatic ? settings.STATIC_FIELD_SUFFIX : settings.FIELD_SUFFIX;
		}
		else
		{
			prefix = null;
			suffix = null;
		}

		return ContainerUtil.map(suggestedNames, name ->
		{
			if(prefix == null && suffix == null)
			{
				return name;
			}
			if(StringUtil.isEmpty(prefix))
			{
				return name + StringUtil.notNullize(suffix);
			}
			return StringUtil.notNullize(prefix) + StringUtil.capitalize(name) + StringUtil.notNullize(suffix);
		});
	}

	@Nonnull
	@RequiredReadAction
	public static Set<String> getSuggestedNames(@Nonnull DotNetTypeRef typeRef, @Nonnull PsiElement scope)
	{
		return getSuggestedNames(typeRef, scope, Collections.emptySet());
	}

	@Nonnull
	@RequiredReadAction
	public static Set<String> getSuggestedNames(@Nonnull DotNetTypeRef typeRef, @Nonnull PsiElement scope, @Nonnull Set<String> alreadyUsedNames)
	{
		Collection<String> candidates = new LinkedHashSet<>();

		boolean enumerable = false;
		// string is enumerable of chars, and it will provide name 'chars' skip it
		if(!DotNetTypeRefUtil.isVmQNameEqual(typeRef, DotNetTypes.System.String))
		{
			DotNetTypeResolveResult resolveResult = CSharpTypeUtil.findTypeRefFromExtends(typeRef, new CSharpTypeRefByQName(scope, DotNetTypes.System.Collections.Generic.IEnumerable$1));
			if(resolveResult != null)
			{
				PsiElement element = resolveResult.getElement();
				if(element instanceof DotNetGenericParameterListOwner)
				{
					DotNetGenericParameter genericParameter = ((DotNetGenericParameterListOwner) element).getGenericParameters()[0];
					DotNetTypeRef insideTypeRef = resolveResult.getGenericExtractor().extract(genericParameter);
					if(insideTypeRef != null)
					{
						candidates.addAll(generateNames(StringUtil.pluralize(CSharpTypeRefPresentationUtil.buildShortText(insideTypeRef))));
						enumerable = true;
					}
				}
			}
		}

		if(!enumerable)
		{
			candidates.addAll(generateNames(CSharpTypeRefPresentationUtil.buildShortText(typeRef)));
		}

		final Set<String> usedNames = CSharpRefactoringUtil.collectUsedNames(scope, scope);

		usedNames.addAll(alreadyUsedNames);

		filterKeywords(candidates);

		final List<String> result = new ArrayList<>();

		for(String candidate : candidates)
		{
			int index = 0;
			String suffix = "";
			while(usedNames.contains(candidate + suffix))
			{
				suffix = Integer.toString(++index);
			}
			result.add(candidate + suffix);
		}

		if(result.isEmpty())
		{
			result.add("o"); // never empty
		}

		return new TreeSet<>(result);
	}

	@Nonnull
	@RequiredReadAction
	public static Collection<String> getSuggestedNames(final DotNetExpression expression)
	{
		return getSuggestedNames(expression, null, false, null);
	}

	@RequiredReadAction
	@Nonnull
	private static String getExpressionInnerText(@Nonnull DotNetExpression expression, @Nonnull Set<String> candidates)
	{
		String text = null;
		if(expression instanceof CSharpReferenceExpression)
		{
			PsiElement resolvedElement = ((CSharpReferenceExpression) expression).resolve();
			String name = null;
			if(resolvedElement instanceof PsiNamedElement)
			{
				name = ((PsiNamedElement) resolvedElement).getName();
			}

			if(name != null && !name.equals(StringUtil.decapitalize(name)))
			{
				candidates.add(StringUtil.decapitalize(name));
			}
		}
		else if(expression instanceof CSharpMethodCallExpressionImpl)
		{
			final PsiElement callee = ((CSharpMethodCallExpressionImpl) expression).getCallExpression();
			text = callee.getText();
		}
		else if(expression instanceof CSharpIndexAccessExpressionImpl)
		{
			text = StringUtil.unpluralize(((CSharpIndexAccessExpressionImpl) expression).getQualifier().getText());
		}

		if(text == null)
		{
			text = expression.getText();
		}
		
		return text;
	}

	@Nonnull
	@RequiredReadAction
	private static Set<String> getSuggestedNames(@Nonnull DotNetExpression expression, @Nullable Collection<String> additionalUsedNames, boolean unpluralize, @Nullable PsiElement toSkip)
	{
		Set<String> candidates = new LinkedHashSet<>();

		String text = getExpressionInnerText(expression, candidates);

		if(unpluralize)
		{
			String oldText = text;

			text = StringUtil.unpluralize(oldText);

			if(text == null)
			{
				text = oldText;
			}
		}

		candidates.addAll(generateNames(text));

		final Set<String> usedNames = CSharpRefactoringUtil.collectUsedNames(expression, toSkip);
		if(additionalUsedNames != null && !additionalUsedNames.isEmpty())
		{
			usedNames.addAll(additionalUsedNames);
		}

		filterKeywords(candidates);

		final List<String> result = new ArrayList<>();

		for(String candidate : candidates)
		{
			int index = 0;
			String suffix = "";
			while(usedNames.contains(candidate + suffix))
			{
				suffix = Integer.toString(++index);
			}
			result.add(candidate + suffix);
		}

		if(result.isEmpty())
		{
			result.add("o"); // never empty
		}

		return new HashSet<>(result);
	}

	private static void filterKeywords(Collection<String> candidates)
	{
		Set<String> keywords = new HashSet<>();
		for(String candidate : candidates)
		{
			if(isKeyword(candidate))
			{
				keywords.add(candidate);
			}
		}

		for(String keyword : keywords)
		{
			candidates.remove(keyword);

			if(keyword.equals("string"))
			{
				candidates.add("str");
			}
			else
			{
				candidates.add(String.valueOf(keyword.charAt(0)));
			}
		}
	}

	public static boolean isKeyword(String text)
	{
		return wantOnlyThisTokens(CSharpTokenSets.KEYWORDS, text);
	}

	public static boolean isIdentifier(String text)
	{
		return wantOnlyThisTokens(TokenSet.create(CSharpTokens.IDENTIFIER), text);
	}

	private static boolean wantOnlyThisTokens(@Nonnull TokenSet tokenSet, @Nonnull CharSequence text)
	{
		try
		{
			CSharpLexer lexer = new CSharpLexer();
			lexer.start(text);
			if(lexer.getTokenEnd() != text.length())
			{
				return false;
			}
			IElementType tokenType = lexer.getTokenType();
			return tokenSet.contains(tokenType);
		}
		catch(Exception ignored)
		{
			return false;
		}
	}

	@Nonnull
	public static Collection<String> generateNames(@Nonnull String name)
	{
		if(name.length() > 2 && name.charAt(0) == 'I' && Character.isUpperCase(name.charAt(1)))
		{
			name = name.substring(1, name.length());
		}

		name = StringUtil.decapitalize(deleteNonLetterFromString(StringUtil.unquoteString(name.replace('.', '_'))));
		if(name.startsWith("get"))
		{
			name = name.substring(3);
		}
		else if(name.startsWith("is"))
		{
			name = name.substring(2);
		}
		while(name.startsWith("_"))
		{
			name = name.substring(1);
		}
		while(name.endsWith("_"))
		{
			name = name.substring(0, name.length() - 1);
		}

		final int length = name.length();
		final Collection<String> possibleNames = new LinkedHashSet<>();
		for(int i = 0; i < length; i++)
		{
			if(Character.isLetter(name.charAt(i)) && (i == 0 || name.charAt(i - 1) == '_' || (Character.isLowerCase(name.charAt(i - 1)) && Character.isUpperCase(name.charAt(i)))))
			{
				final String candidate = StringUtil.decapitalize(name.substring(i));
				if(candidate.length() < 25)
				{
					possibleNames.add(candidate);
				}
			}
		}
		// prefer shorter names
		ArrayList<String> reversed = new ArrayList<>(possibleNames);
		Collections.reverse(reversed);
		return ContainerUtil.map(reversed, name1 ->
		{
			if(name1.indexOf('_') == -1)
			{
				return name1;
			}
			name1 = StringUtil.capitalizeWords(name1, "_", true, true);
			return StringUtil.decapitalize(name1.replaceAll("_", ""));
		});
	}
}
