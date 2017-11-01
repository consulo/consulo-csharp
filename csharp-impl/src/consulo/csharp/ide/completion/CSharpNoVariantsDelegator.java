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

import java.util.Collections;
import java.util.Set;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResult;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.codeInsight.completion.impl.BetterPrefixMatcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.intellij.util.Processor;
import com.intellij.util.indexing.IdFilter;
import consulo.annotations.RequiredDispatchThread;
import consulo.annotations.RequiredReadAction;
import consulo.annotations.RequiredWriteAction;
import consulo.csharp.ide.codeInsight.actions.AddUsingAction;
import consulo.csharp.ide.completion.item.CSharpTypeLikeLookupElement;
import consulo.csharp.ide.completion.util.LtGtInsertHandler;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpUsingListChild;
import consulo.csharp.lang.psi.impl.source.CSharpPsiUtilImpl;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.libraryAnalyzer.NamespaceReference;
import consulo.dotnet.psi.DotNetAttributeUtil;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.resolve.DotNetShortNameSearcher;
import consulo.dotnet.resolve.GlobalSearchScopeFilter;
import consulo.ide.IconDescriptorUpdaters;
import consulo.internal.dotnet.msil.decompiler.util.MsilHelper;

/**
 * @author VISTALL
 * @see com.intellij.codeInsight.completion.JavaNoVariantsDelegator
 * @since 23.08.2015
 */
public class CSharpNoVariantsDelegator extends CompletionContributor
{
	public static class ResultTracker implements Consumer<CompletionResult>
	{
		private final CompletionResultSet myResult;
		public boolean containsOnlyNamespaces = true;
		public BetterPrefixMatcher betterMatcher;

		public ResultTracker(CompletionResultSet result)
		{
			myResult = result;
			betterMatcher = new BetterPrefixMatcher.AutoRestarting(result);
		}

		@Override
		public void consume(CompletionResult plainResult)
		{
			myResult.passResult(plainResult);

			LookupElement element = plainResult.getLookupElement();
			if(containsOnlyNamespaces && !(CompletionUtil.getTargetElement(element) instanceof DotNetNamespaceAsElement))
			{
				containsOnlyNamespaces = false;
			}

			betterMatcher = betterMatcher.improve(plainResult);
		}
	}

	@RequiredReadAction
	@Override
	@RequiredDispatchThread
	public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result)
	{
		final InheritorsHolder holder = new InheritorsHolder(result);
		ResultTracker tracker = new ResultTracker(result)
		{
			@Override
			public void consume(CompletionResult plainResult)
			{
				super.consume(plainResult);

				LookupElement element = plainResult.getLookupElement();
				Object o = element.getObject();
				if(o instanceof PsiElement && CSharpPsiUtilImpl.isTypeLikeElement((PsiElement) o))
				{
					holder.registerTypeLike((DotNetQualifiedElement) o);
				}
			}
		};
		result.runRemainingContributors(parameters, tracker);
		final boolean empty = tracker.containsOnlyNamespaces;

		if(!empty && parameters.getInvocationCount() == 0)
		{
			result.restartCompletionWhenNothingMatches();
		}

		if(empty)
		{
			delegate(parameters, CSharpCompletionSorting.modifyResultSet(parameters, result), holder);
		}
		else
		{
			if(parameters.getCompletionType() == CompletionType.BASIC && parameters.getInvocationCount() <= 1 && CSharpCompletionUtil.mayStartClassName(result) && CSharpCompletionUtil.isClassNamePossible(parameters))
			{
				addTypesForUsing(parameters, CSharpCompletionSorting.modifyResultSet(parameters, result.withPrefixMatcher(tracker.betterMatcher)), holder);
			}
		}
	}

	@RequiredReadAction
	private static void delegate(CompletionParameters parameters, final CompletionResultSet result, final InheritorsHolder inheritorsHolder)
	{
		if(parameters.getCompletionType() == CompletionType.BASIC)
		{
			PsiElement position = parameters.getPosition();
			if(parameters.getInvocationCount() <= 1 && CSharpCompletionUtil.mayStartClassName(result) && CSharpCompletionUtil.isClassNamePossible(parameters))
			{
				addTypesForUsing(parameters, result, inheritorsHolder);
				return;
			}

			//suggestChainedCalls(parameters, result, position);
		}

		if(parameters.getCompletionType() == CompletionType.SMART && parameters.getInvocationCount() == 2)
		{
			result.runRemainingContributors(parameters.withInvocationCount(3), true);
		}
	}

	@RequiredReadAction
	public static void addTypesForUsing(final CompletionParameters parameters, final CompletionResultSet result, final InheritorsHolder inheritorsHolder)
	{
		final PrefixMatcher matcher = result.getPrefixMatcher();
		final CSharpReferenceExpression parent = (CSharpReferenceExpression) parameters.getPosition().getParent();

		final Project project = parent.getProject();
		final GlobalSearchScope resolveScope = parent.getResolveScope();

		final DotNetShortNameSearcher shortNameSearcher = DotNetShortNameSearcher.getInstance(project);
		final IdFilter projectIdFilter = new GlobalSearchScopeFilter(resolveScope);

		final boolean insideUsing = PsiTreeUtil.getParentOfType(parent, CSharpUsingListChild.class) != null;

		final Set<String> names = new THashSet<String>(1000);
		shortNameSearcher.collectTypeNames(new Processor<String>()
		{
			private int count = 0;

			@Override
			public boolean process(String key)
			{
				if(count++ % 512 == 0)
				{
					ProgressManager.checkCanceled();
				}

				if(matcher.prefixMatches(key))
				{
					names.add(key);
				}
				return true;
			}
		}, resolveScope, projectIdFilter);

		final Set<DotNetTypeDeclaration> targets = new THashSet<DotNetTypeDeclaration>(names.size());
		int i = 0;
		for(String key : names)
		{
			if(i++ % 512 == 0)
			{
				ProgressManager.checkCanceled();
			}

			shortNameSearcher.collectTypes(key, resolveScope, projectIdFilter, typeDeclaration ->
			{
				ProgressManager.checkCanceled();

				if(inheritorsHolder.alreadyProcessed(typeDeclaration))
				{
					return true;
				}

				targets.add(typeDeclaration);

				return true;
			});
		}

		i = 0;
		for(DotNetTypeDeclaration target : targets)
		{
			if(i++ % 512 == 0)
			{
				ProgressManager.checkCanceled();
			}

			consumeType(parameters, parent, result, insideUsing, target);
		}
	}

	@RequiredReadAction
	private static void consumeType(final CompletionParameters completionParameters,
			CSharpReferenceExpression referenceExpression,
			Consumer<LookupElement> consumer,
			boolean insideUsingList,
			DotNetTypeDeclaration someType)
	{
		final String parentQName = someType.getPresentableParentQName();
		if(StringUtil.isEmpty(parentQName))
		{
			return;
		}

		String presentationText = MsilHelper.cutGenericMarker(someType.getName());

		int genericCount = 0;
		DotNetGenericParameter[] genericParameters = someType.getGenericParameters();
		if((genericCount = genericParameters.length) > 0)
		{
			presentationText += "<" + StringUtil.join(genericParameters, parameter -> parameter.getName(), ", ");
			presentationText += ">";
		}

		String lookupString = insideUsingList ? someType.getPresentableQName() : someType.getName();
		if(lookupString == null)
		{
			return;
		}
		lookupString = MsilHelper.cutGenericMarker(lookupString);

		DotNetQualifiedElement targetElementForLookup = someType;
		CSharpMethodDeclaration methodDeclaration = someType.getUserData(CSharpResolveUtil.DELEGATE_METHOD_TYPE);
		if(methodDeclaration != null)
		{
			targetElementForLookup = methodDeclaration;
		}
		LookupElementBuilder builder = LookupElementBuilder.create(targetElementForLookup, lookupString);
		builder = builder.withPresentableText(presentationText);
		builder = builder.withIcon(IconDescriptorUpdaters.getIcon(targetElementForLookup, Iconable.ICON_FLAG_VISIBILITY));

		builder = builder.withTypeText(parentQName, true);
		final InsertHandler<LookupElement> ltGtInsertHandler = genericCount == 0 ? null : LtGtInsertHandler.getInstance(genericCount > 0);
		if(insideUsingList)
		{
			builder = builder.withInsertHandler(ltGtInsertHandler);
		}
		else
		{
			builder = builder.withInsertHandler(new InsertHandler<LookupElement>()
			{
				@Override
				@RequiredWriteAction
				public void handleInsert(InsertionContext context, LookupElement item)
				{
					if(ltGtInsertHandler != null)
					{
						ltGtInsertHandler.handleInsert(context, item);
					}

					context.commitDocument();

					new AddUsingAction(completionParameters.getEditor(), context.getFile(), Collections.<NamespaceReference>singleton(new NamespaceReference(parentQName, null))).execute();
				}
			});
		}

		if(DotNetAttributeUtil.hasAttribute(someType, DotNetTypes.System.ObsoleteAttribute))
		{
			builder = builder.withStrikeoutness(true);
		}

		consumer.consume(CSharpTypeLikeLookupElement.create(builder, DotNetGenericExtractor.EMPTY, referenceExpression));
	}
}
