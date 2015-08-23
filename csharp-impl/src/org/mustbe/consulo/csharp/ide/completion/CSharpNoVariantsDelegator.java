package org.mustbe.consulo.csharp.ide.completion;

import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResult;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.codeInsight.completion.impl.BetterPrefixMatcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.intellij.util.Consumer;

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
			if(containsOnlyNamespaces && !(CompletionUtil.getTargetElement(element) instanceof
					DotNetNamespaceAsElement))
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
				CSharpTypeReferenceCompletionContributor.addTypesForUsing(parameters, result.withPrefixMatcher(tracker
						.betterMatcher), holder);
			}
		}
	}

	private static void delegate(CompletionParameters parameters,
			final CompletionResultSet result,
			final InheritorsHolder inheritorsHolder)
	{
		if(parameters.getCompletionType() == CompletionType.BASIC)
		{
			PsiElement position = parameters.getPosition();
			if(parameters.getInvocationCount() <= 1 &&
					CSharpCompletionUtil.mayStartClassName(result) &&
					CSharpCompletionUtil.isClassNamePossible(parameters))
			{
				CSharpTypeReferenceCompletionContributor.addTypesForUsing(parameters, result, inheritorsHolder);
				return;
			}

			//suggestChainedCalls(parameters, result, position);
		}

		if(parameters.getCompletionType() == CompletionType.SMART && parameters.getInvocationCount() == 2)
		{
			result.runRemainingContributors(parameters.withInvocationCount(3), true);
		}
	}
}
