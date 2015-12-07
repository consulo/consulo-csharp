package org.mustbe.consulo.csharp.ide.completion;

import gnu.trove.THashSet;

import java.util.Collections;
import java.util.Set;

import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.AddUsingAction;
import org.mustbe.consulo.csharp.ide.completion.item.ReplaceableTypeLikeLookupElement;
import org.mustbe.consulo.csharp.ide.completion.util.LtGtInsertHandler;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingList;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.libraryAnalyzer.NamespaceReference;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetShortNameSearcher;
import org.mustbe.consulo.dotnet.resolve.GlobalSearchScopeFilter;
import org.mustbe.dotnet.msil.decompiler.util.MsilHelper;
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
import com.intellij.ide.IconDescriptorUpdaters;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.intellij.util.Function;
import com.intellij.util.Processor;
import com.intellij.util.indexing.IdFilter;

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
			betterMatcher = new BetterPrefixMatcher(result);
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
				if(o instanceof PsiElement && CSharpCompletionUtil.isTypeLikeElement((PsiElement) o))
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
			if(parameters.getCompletionType() == CompletionType.BASIC &&
					parameters.getInvocationCount() <= 1 &&
					CSharpCompletionUtil.mayStartClassName(result) &&
					CSharpCompletionUtil.isClassNamePossible(parameters))
			{
				addTypesForUsing(parameters, result.withPrefixMatcher(tracker.betterMatcher), holder);
			}
		}
	}

	private static void delegate(CompletionParameters parameters, final CompletionResultSet result, final InheritorsHolder inheritorsHolder)
	{
		if(parameters.getCompletionType() == CompletionType.BASIC)
		{
			PsiElement position = parameters.getPosition();
			if(parameters.getInvocationCount() <= 1 &&
					CSharpCompletionUtil.mayStartClassName(result) &&
					CSharpCompletionUtil.isClassNamePossible(parameters))
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

	public static void addTypesForUsing(final CompletionParameters parameters, final CompletionResultSet result, final InheritorsHolder inheritorsHolder)
	{
		final PrefixMatcher matcher = result.getPrefixMatcher();
		CSharpReferenceExpression parent = (CSharpReferenceExpression) parameters.getPosition().getParent();

		final Project project = parent.getProject();
		final GlobalSearchScope resolveScope = parent.getResolveScope();

		final DotNetShortNameSearcher shortNameSearcher = DotNetShortNameSearcher.getInstance(project);
		final IdFilter projectIdFilter = new GlobalSearchScopeFilter(resolveScope);

		final boolean insideUsingList = PsiTreeUtil.getParentOfType(parent, CSharpUsingList.class) != null;

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

		int i = 0;
		for(String key : names)
		{
			if(i++ % 512 == 0)
			{
				ProgressManager.checkCanceled();
			}

			shortNameSearcher.collectTypes(key, resolveScope, projectIdFilter, new Processor<DotNetTypeDeclaration>()
			{
				@Override
				@RequiredReadAction
				public boolean process(DotNetTypeDeclaration typeDeclaration)
				{
					if(inheritorsHolder.alreadyProcessed(typeDeclaration))
					{
						return true;
					}

					consumeType(parameters, result, insideUsingList, typeDeclaration);

					return true;
				}
			});
		}
	}

	@RequiredReadAction
	private static void consumeType(final CompletionParameters completionParameters, Consumer<LookupElement> consumer, boolean insideUsingList, DotNetTypeDeclaration someType)
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
			presentationText += "<" + StringUtil.join(genericParameters, new Function<DotNetGenericParameter, String>()
			{
				@Override
				public String fun(DotNetGenericParameter parameter)
				{
					return parameter.getName();
				}
			}, ", ");
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
				public void handleInsert(InsertionContext context, LookupElement item)
				{
					if(ltGtInsertHandler != null)
					{
						ltGtInsertHandler.handleInsert(context, item);
					}

					new AddUsingAction(completionParameters.getEditor(), context.getFile(), Collections.<NamespaceReference>singleton(new NamespaceReference(parentQName, null))).execute();
				}
			});
		}

		consumer.consume(new ReplaceableTypeLikeLookupElement(builder));
	}
}
