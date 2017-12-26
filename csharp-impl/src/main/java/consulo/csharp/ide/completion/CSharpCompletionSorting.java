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

import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionSorter;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementWeigher;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.SyntaxTraverser;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.completion.expected.ExpectedTypeInfo;
import consulo.csharp.ide.completion.expected.ExpectedTypeVisitor;
import consulo.csharp.ide.completion.weigher.CSharpRecursiveGuardWeigher;
import consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import consulo.csharp.lang.psi.CSharpEnumConstantDeclaration;
import consulo.csharp.lang.psi.CSharpEventDeclaration;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpLambdaParameter;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.impl.source.CSharpForeachStatementImpl;
import consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;

/**
 * @author VISTALL
 * @since 20.03.2016
 */
public class CSharpCompletionSorting
{
	public static class KindSorter extends LookupElementWeigher
	{
		private static Key<Type> ourForceType = Key.create("csharpOurForceType");

		public enum Type
		{
			top1,
			top2,
			delegate,
			localVariableOrParameter,
			keyword,
			constants,
			member,
			parameterInCall,
			overrideMember,
			preprocessorKeywords,
			any,
			hiddenKeywords,
			namespace,
		}

		protected KindSorter()
		{
			super("csharpKindSorter");
		}

		@Nullable
		@Override
		@RequiredReadAction
		public Type weigh(@NotNull LookupElement element)
		{
			Type type = element.getUserData(ourForceType);
			if(type != null)
			{
				return type;
			}

			IElementType keywordElementType = element.getUserData(CSharpCompletionUtil.KEYWORD_ELEMENT_TYPE);

			String lookupString = element.getLookupString();
			if(lookupString.startsWith("__") && keywordElementType != null)
			{
				return Type.hiddenKeywords;
			}
			else if(keywordElementType != null)
			{
				return Type.keyword;
			}
			else if(lookupString.startsWith("#"))
			{
				return Type.preprocessorKeywords;
			}

			PsiElement psiElement = element.getPsiElement();
			if(psiElement instanceof CSharpLocalVariable || psiElement instanceof DotNetParameter || psiElement instanceof CSharpLambdaParameter)
			{
				return Type.localVariableOrParameter;
			}

			if(psiElement instanceof CSharpFieldDeclaration && ((CSharpFieldDeclaration) psiElement).isConstant() || psiElement instanceof CSharpEnumConstantDeclaration)
			{
				return Type.constants;
			}

			if(psiElement instanceof CSharpPropertyDeclaration || psiElement instanceof CSharpEventDeclaration || psiElement instanceof CSharpFieldDeclaration || psiElement instanceof
					CSharpMethodDeclaration && !((CSharpMethodDeclaration) psiElement).isDelegate())
			{
				return Type.member;
			}

			if(psiElement instanceof DotNetNamespaceAsElement)
			{
				return Type.namespace;
			}
			return Type.any;
		}
	}

	private static class ExpectedNameSorter extends LookupElementWeigher
	{
		private Set<String> myExpectedNames;

		@RequiredReadAction
		private ExpectedNameSorter(List<ExpectedTypeInfo> expectedTypeInfos)
		{
			super("csharpByExpectedNameSorter");

			myExpectedNames = new THashSet<>();
			for(ExpectedTypeInfo expectedTypeInfo : expectedTypeInfos)
			{
				PsiElement typeProvider = expectedTypeInfo.getTypeProvider();
				if(typeProvider instanceof PsiNamedElement)
				{
					myExpectedNames.add(((PsiNamedElement) typeProvider).getName());
				}
			}
		}

		@Nullable
		@Override
		@RequiredReadAction
		public Comparable weigh(@NotNull LookupElement element)
		{
			if(myExpectedNames.isEmpty())
			{
				return null;
			}

			String targetName = getName(element);
			if(targetName != null)
			{
				int max = 0;
				final List<String> wordsNoDigits = NameUtil.nameToWordsLowerCase(truncDigits(targetName));
				for(String expectedName : myExpectedNames)
				{
					final THashSet<String> set = new THashSet<>(NameUtil.nameToWordsLowerCase(truncDigits(expectedName)));
					set.retainAll(wordsNoDigits);
					max = Math.max(max, set.size());
				}
				return -max;
			}
			return 0;
		}
	}

	private static class PreferShorter extends LookupElementWeigher
	{
		private final List<ExpectedTypeInfo> myExpectedTypes;

		public PreferShorter(List<ExpectedTypeInfo> expectedTypes)
		{
			super("csharpShorter");
			myExpectedTypes = expectedTypes;
		}

		@NotNull
		@Override
		@RequiredReadAction
		public Comparable weigh(@NotNull LookupElement element)
		{
			final String name = getName(element);

			if(name != null && getNameEndMatchingDegree(name, myExpectedTypes) != 0)
			{
				return NameUtil.nameToWords(name).length - 1000;
			}
			return 0;
		}

		@RequiredReadAction
		private static int getNameEndMatchingDegree(final String name, List<ExpectedTypeInfo> expectedInfos)
		{
			int res = 0;
			if(name != null && expectedInfos != null)
			{
				final List<String> words = NameUtil.nameToWordsLowerCase(name);
				final List<String> wordsNoDigits = NameUtil.nameToWordsLowerCase(truncDigits(name));
				int max1 = calcMatch(words, 0, expectedInfos);
				max1 = calcMatch(wordsNoDigits, max1, expectedInfos);
				res = max1;
			}

			return res;
		}
	}

	private static class NotImportedSorter extends LookupElementWeigher
	{
		public NotImportedSorter()
		{
			super("notImportedSorter");
		}

		@NotNull
		@Override
		@RequiredReadAction
		public Comparable weigh(@NotNull LookupElement element)
		{
			Boolean data = element.getUserData(CSharpNoVariantsDelegator.NOT_IMPORTED);
			return data != Boolean.TRUE;
		}
	}

	@RequiredReadAction
	private static int calcMatch(final List<String> words, int max, List<ExpectedTypeInfo> myExpectedInfos)
	{
		for(ExpectedTypeInfo expectedInfo : myExpectedInfos)
		{
			String expectedName = getName(expectedInfo.getTypeProvider());
			if(expectedName == null)
			{
				continue;
			}
			max = calcMatch(expectedName, words, max);
			max = calcMatch(truncDigits(expectedName), words, max);
		}
		return max;
	}

	private static int calcMatch(final String expectedName, final List<String> words, int max)
	{
		if(expectedName == null)
		{
			return max;
		}

		String[] expectedWords = NameUtil.nameToWords(expectedName);
		int limit = Math.min(words.size(), expectedWords.length);
		for(int i = 0; i < limit; i++)
		{
			String word = words.get(words.size() - i - 1);
			String expectedWord = expectedWords[expectedWords.length - i - 1];
			if(word.equalsIgnoreCase(expectedWord))
			{
				max = Math.max(max, i + 1);
			}
			else
			{
				break;
			}
		}
		return max;
	}

	private static String truncDigits(String name)
	{
		int count = name.length() - 1;
		while(count >= 0)
		{
			char c = name.charAt(count);
			if(!Character.isDigit(c))
			{
				break;
			}
			count--;
		}
		return name.substring(0, count + 1);
	}

	@Nullable
	@RequiredReadAction
	private static String getName(LookupElement element)
	{
		PsiElement psiElement = element.getPsiElement();
		return getName(psiElement);
	}

	@Nullable
	@RequiredReadAction
	private static String getName(@Nullable PsiElement psiElement)
	{
		if(psiElement instanceof DotNetVariable || psiElement instanceof CSharpMethodDeclaration)
		{
			return ((PsiNamedElement) psiElement).getName();
		}
		return null;
	}

	public static void force(UserDataHolder holder, KindSorter.Type type)
	{
		holder.putUserData(KindSorter.ourForceType, type);
	}

	@Nullable
	public static KindSorter.Type getSort(UserDataHolder userDataHolder)
	{
		return userDataHolder.getUserData(KindSorter.ourForceType);
	}

	public static void copyForce(UserDataHolder from, UserDataHolder to)
	{
		KindSorter.Type data = from.getUserData(KindSorter.ourForceType);
		if(data != null)
		{
			to.putUserData(KindSorter.ourForceType, data);
		}
	}

	@Nullable
	@RequiredReadAction
	private static LookupElementWeigher recursiveSorter(CompletionParameters completionParameters, CompletionResultSet result)
	{
		PsiElement position = completionParameters.getPosition();


		Set<PsiElement> elements = new THashSet<>();

		PsiElement argumentListOwner = PsiTreeUtil.getContextOfType(position, CSharpCallArgumentListOwner.class, DotNetVariable.class);
		if(argumentListOwner instanceof CSharpMethodCallExpressionImpl)
		{
			ContainerUtil.addIfNotNull(elements, ((CSharpMethodCallExpressionImpl) argumentListOwner).resolveToCallable());
		}
		else if(argumentListOwner instanceof DotNetVariable)
		{
			elements.add(argumentListOwner);
		}

		List<CSharpForeachStatementImpl> foreachStatements = SyntaxTraverser.psiApi().parents(position).filter(CSharpForeachStatementImpl.class).addAllTo(new ArrayList<>());
		for(CSharpForeachStatementImpl foreachStatement : foreachStatements)
		{
			DotNetExpression iterableExpression = foreachStatement.getIterableExpression();
			if(iterableExpression instanceof CSharpReferenceExpression)
			{
				ContainerUtil.addIfNotNull(elements, ((CSharpReferenceExpression) iterableExpression).resolve());
			}
		}

		if(!elements.isEmpty())
		{
			return new CSharpRecursiveGuardWeigher(elements);
		}
		return null;
	}

	@RequiredReadAction
	public static CompletionResultSet modifyResultSet(CompletionParameters completionParameters, CompletionResultSet result)
	{
		CompletionSorter sorter = CompletionSorter.defaultSorter(completionParameters, result.getPrefixMatcher());
		List<LookupElementWeigher> afterStats = new ArrayList<>();

		afterStats.add(new KindSorter());
		ContainerUtil.addIfNotNull(afterStats, recursiveSorter(completionParameters, result));

		List<LookupElementWeigher> afterProximity = new ArrayList<>();

		List<ExpectedTypeInfo> expectedTypeRefs = ExpectedTypeVisitor.findExpectedTypeRefs(completionParameters.getPosition());
		if(expectedTypeRefs.isEmpty())
		{
			expectedTypeRefs = ExpectedTypeVisitor.findExpectedTypeRefs(completionParameters.getPosition().getParent());
		}

		if(!expectedTypeRefs.isEmpty())
		{
			afterProximity.add(new ExpectedNameSorter(expectedTypeRefs));
			afterProximity.add(new PreferShorter(expectedTypeRefs));
		}

		afterProximity.add(new NotImportedSorter());

		sorter = sorter.weighAfter("stats", afterStats.toArray(new LookupElementWeigher[afterStats.size()]));
		sorter = sorter.weighAfter("proximity", afterProximity.toArray(new LookupElementWeigher[afterProximity.size()]));
		result = result.withRelevanceSorter(sorter);
		return result;
	}
}
