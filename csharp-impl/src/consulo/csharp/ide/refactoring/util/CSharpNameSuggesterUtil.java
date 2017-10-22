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

import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.codeStyle.CSharpCodeGenerationSettings;
import consulo.csharp.lang.lexer.CSharpLexer;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTokenSets;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.impl.source.CSharpForeachStatementImpl;
import consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;

/**
 * @author Fedor.Korotkov
 *         <p>
 *         from google-dart
 */
public class CSharpNameSuggesterUtil
{
	private CSharpNameSuggesterUtil()
	{
	}

	private static String deleteNonLetterFromString(@NotNull final String string)
	{
		Pattern pattern = Pattern.compile("[^a-zA-Z_]+");
		Matcher matcher = pattern.matcher(string);
		return matcher.replaceAll("_");
	}

	@NotNull
	@RequiredReadAction
	public static Collection<String> getSuggestedVariableNames(final DotNetVariable variable)
	{
		PsiElement parent = variable.getParent();

		Collection<String> suggestedNames = getSuggestedNames(variable.toTypeRef(true), variable);
		if(parent instanceof CSharpForeachStatementImpl)
		{
			DotNetExpression iterableExpression = ((CSharpForeachStatementImpl) parent).getIterableExpression();
			if(iterableExpression != null)
			{
				suggestedNames = getSuggestedNames(iterableExpression, suggestedNames, variable);
			}
		}

		DotNetExpression initializer = variable.getInitializer();
		if(initializer != null)
		{
			suggestedNames.addAll(getSuggestedNames(initializer, suggestedNames, variable));
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

	@NotNull
	@RequiredReadAction
	public static Set<String> getSuggestedNames(final DotNetTypeRef typeRef, PsiElement scope)
	{
		Collection<String> candidates = new LinkedHashSet<>();

		DotNetTypeResolveResult resolveResult = CSharpTypeUtil.findTypeRefFromExtends(typeRef, new CSharpTypeRefByQName(scope, DotNetTypes.System.Collections.Generic.IEnumerable$1), scope);
		if(resolveResult != null)
		{
			PsiElement element = resolveResult.getElement();
			if(element instanceof DotNetGenericParameterListOwner)
			{
				DotNetGenericParameter genericParameter = ((DotNetGenericParameterListOwner) element).getGenericParameters()[0];
				DotNetTypeRef insideTypeRef = resolveResult.getGenericExtractor().extract(genericParameter);
				if(insideTypeRef != null)
				{
					candidates.addAll(generateNames(StringUtil.pluralize(CSharpTypeRefPresentationUtil.buildShortText(insideTypeRef, scope))));
				}
			}
		}

		candidates.addAll(generateNames(CSharpTypeRefPresentationUtil.buildShortText(typeRef, scope)));

		final Set<String> usedNames = CSharpRefactoringUtil.collectUsedNames(scope, scope);

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

		for(ListIterator<String> iterator = result.listIterator(); iterator.hasNext(); )
		{
			String next = iterator.next();

			if(isKeyword(next))
			{
				iterator.set(String.valueOf(next.charAt(0)));
			}
		}
		return new TreeSet<>(result);
	}

	@NotNull
	@RequiredReadAction
	public static Collection<String> getSuggestedNames(final DotNetExpression expression)
	{
		return getSuggestedNames(expression, null, null);
	}

	@NotNull
	@RequiredReadAction
	private static Set<String> getSuggestedNames(@NotNull DotNetExpression expression, @Nullable Collection<String> additionalUsedNames, @Nullable PsiElement toSkip)
	{
		Set<String> candidates = new LinkedHashSet<>();

		String text = expression.getText();
		if(expression.getParent() instanceof CSharpForeachStatementImpl)
		{
			text = StringUtil.unpluralize(expression.getText());
		}
		else if(expression instanceof CSharpReferenceExpression)
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

		if(text != null)
		{
			candidates.addAll(generateNames(text));
		}

		final Set<String> usedNames = CSharpRefactoringUtil.collectUsedNames(expression, toSkip);
		if(additionalUsedNames != null && !additionalUsedNames.isEmpty())
		{
			usedNames.addAll(additionalUsedNames);
		}
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

		for(ListIterator<String> iterator = result.listIterator(); iterator.hasNext(); )
		{
			String next = iterator.next();

			if(isKeyword(next))
			{
				iterator.set(String.valueOf(next.charAt(0)));
			}
		}
		return new THashSet<>(result);
	}

	public static boolean isKeyword(String text)
	{
		return wantOnlyThisTokens(CSharpTokenSets.KEYWORDS, text);
	}

	public static boolean isIdentifier(String text)
	{
		return wantOnlyThisTokens(TokenSet.create(CSharpTokens.IDENTIFIER), text);
	}

	private static boolean wantOnlyThisTokens(@NotNull TokenSet tokenSet, @NotNull CharSequence text)
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

	@NotNull
	public static Collection<String> generateNames(@NotNull String name)
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
