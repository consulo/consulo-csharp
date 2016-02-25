package org.mustbe.consulo.csharp.ide.refactoring.util;

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
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.lexer.CSharpLexer;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpForeachStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefWithInnerTypeRef;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author Fedor.Korotkov
 *         <p/>
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
	public static Set<String> getSuggestedVariableNames(final DotNetVariable variable)
	{
		PsiElement parent = variable.getParent();

		Set<String> suggestedNames = getSuggestedNames(variable.toTypeRef(true), variable);
		if(parent instanceof CSharpForeachStatementImpl)
		{
			DotNetExpression iterableExpression = ((CSharpForeachStatementImpl) parent).getIterableExpression();
			if(iterableExpression != null)
			{
				suggestedNames = getSuggestedNames(iterableExpression, suggestedNames, variable);
			}
		}
		return suggestedNames;
	}

	@NotNull
	public static Set<String> getSuggestedNames(final DotNetTypeRef typeRef, PsiElement scope)
	{
		Collection<String> candidates = new LinkedHashSet<String>();

		if(typeRef instanceof DotNetTypeRefWithInnerTypeRef)
		{
			candidates.addAll(generateNames(((DotNetTypeRefWithInnerTypeRef) typeRef).getInnerTypeRef().getPresentableText()));
		}
		candidates.addAll(generateNames(typeRef.getPresentableText()));

		final Set<String> usedNames = CSharpRefactoringUtil.collectUsedNames(scope, scope);

		final List<String> result = new ArrayList<String>();

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
		return new TreeSet<String>(result);
	}

	@NotNull
	@RequiredReadAction
	public static Collection<String> getSuggestedNames(final DotNetExpression expression)
	{
		return getSuggestedNames(expression, null, null);
	}

	@NotNull
	@RequiredReadAction
	public static Set<String> getSuggestedNames(@NotNull DotNetExpression expression, @Nullable Collection<String> additionalUsedNames, @Nullable PsiElement toSkip)
	{
		Set<String> candidates = new LinkedHashSet<String>();

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
		final List<String> result = new ArrayList<String>();

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
		return new THashSet<String>(result);
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
		final Collection<String> possibleNames = new LinkedHashSet<String>();
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
		ArrayList<String> reversed = new ArrayList<String>(possibleNames);
		Collections.reverse(reversed);
		return ContainerUtil.map(reversed, new Function<String, String>()
		{
			@Override
			public String fun(String name)
			{
				if(name.indexOf('_') == -1)
				{
					return name;
				}
				name = StringUtil.capitalizeWords(name, "_", true, true);
				return StringUtil.decapitalize(name.replaceAll("_", ""));
			}
		});
	}
}
