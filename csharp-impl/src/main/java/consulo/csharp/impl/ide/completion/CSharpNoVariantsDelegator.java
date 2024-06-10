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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.application.progress.ProgressManager;
import consulo.application.util.function.Processor;
import consulo.application.util.matcher.PrefixMatcher;
import consulo.component.util.Iconable;
import consulo.csharp.impl.ide.CSharpLookupElementBuilder;
import consulo.csharp.impl.ide.codeInsight.actions.AddUsingAction;
import consulo.csharp.impl.ide.completion.item.CSharpTypeLikeLookupElement;
import consulo.csharp.impl.ide.completion.util.LtGtInsertHandler;
import consulo.csharp.impl.libraryAnalyzer.NamespaceReference;
import consulo.csharp.lang.impl.psi.source.CSharpPsiUtilImpl;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpUsingListChild;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetAttributeUtil;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.impl.resolve.GlobalSearchScopeFilter;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.psi.resolve.DotNetShortNameSearcher;
import consulo.ide.impl.idea.codeInsight.completion.impl.BetterPrefixMatcher;
import consulo.internal.dotnet.msil.decompiler.util.MsilHelper;
import consulo.language.editor.completion.*;
import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.IdFilter;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.lang.StringUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @see com.intellij.codeInsight.completion.JavaNoVariantsDelegator
 * @since 23.08.2015
 */
class CSharpNoVariantsDelegator
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
		public void accept(CompletionResult plainResult)
		{
			myResult.passResult(plainResult);

			LookupElement element = plainResult.getLookupElement();
			if(containsOnlyNamespaces && !(CompletionUtilCore.getTargetElement(element) instanceof DotNetNamespaceAsElement))
			{
				containsOnlyNamespaces = false;
			}

			betterMatcher = betterMatcher.improve(plainResult);
		}
	}

	@RequiredReadAction
	@RequiredUIAccess
	public static void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result)
	{
		final InheritorsHolder holder = new InheritorsHolder(result);
		ResultTracker tracker = new ResultTracker(result)
		{
			@Override
			public void accept(CompletionResult plainResult)
			{
				super.accept(plainResult);

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
			delegate(parameters, result, holder);
		}
		else
		{
			if(parameters.getCompletionType() == CompletionType.BASIC && parameters.getInvocationCount() <= 1 && CSharpCompletionUtil.mayStartClassName(result) && CSharpCompletionUtil
					.isClassNamePossible(parameters))
			{
				addTypesForUsing(parameters, result.withPrefixMatcher(tracker.betterMatcher), holder);
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

		final Set<String> names = new HashSet<String>(1000);
		shortNameSearcher.collectTypeNames(new Processor<>()
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

		final Set<DotNetTypeDeclaration> targets = new HashSet<DotNetTypeDeclaration>(names.size());
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

		int genericCount;
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
		builder = builder.withIcon(IconDescriptorUpdaters.getIcon(targetElementForLookup, 0));

		builder = builder.withTypeText(parentQName, true);
		final InsertHandler<LookupElement> ltGtInsertHandler = genericCount == 0 ? null : LtGtInsertHandler.getInstance(genericCount > 0);
		if(insideUsingList)
		{
			builder = builder.withInsertHandler(ltGtInsertHandler);
		}
		else
		{
			builder = builder.withInsertHandler(new InsertHandler<>()
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

		CSharpTypeLikeLookupElement element = CSharpTypeLikeLookupElement.create(builder, DotNetGenericExtractor.EMPTY, referenceExpression);
		CSharpCompletionSorting.force(element, CSharpCompletionSorting.KindSorter.Type.notImporterSymbol);

		element.putCopyableUserData(CSharpLookupElementBuilder.OBSOLETE_FLAG, Boolean.TRUE);

		consumer.accept(element);
	}
}
