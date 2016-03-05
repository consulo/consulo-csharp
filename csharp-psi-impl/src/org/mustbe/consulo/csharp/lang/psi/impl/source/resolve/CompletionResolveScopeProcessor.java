package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpContextUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpVisibilityUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 23.10.14
 */
public class CompletionResolveScopeProcessor extends StubScopeProcessor
{
	@NotNull
	private final GlobalSearchScope myScope;
	@NotNull
	private final PsiElement myPlace;
	@NotNull
	private CSharpContextUtil.ContextType myContextType;
	@NotNull
	private Processor<ResolveResult> myProcessor;

	public CompletionResolveScopeProcessor(@NotNull CSharpResolveOptions options, @NotNull Processor<ResolveResult> processor, @NotNull ExecuteTarget[] targets)
	{
		myProcessor = processor;
		myPlace = options.getElement();

		myScope = myPlace.getResolveScope();
		CSharpContextUtil.ContextType completionContextType = options.getCompletionContextType();
		if(completionContextType != null)
		{
			myContextType = completionContextType;
		}
		else
		{
			myContextType = myPlace instanceof CSharpReferenceExpression ? CSharpContextUtil.getParentContextTypeForReference(
					(CSharpReferenceExpression) myPlace) : CSharpContextUtil.ContextType.ANY;
		}
		putUserData(ExecuteTargetUtil.EXECUTE_TARGETS, ExecuteTargetUtil.of(targets));
	}

	@Override
	public void pushResultExternally(@NotNull ResolveResult resolveResult)
	{
		myProcessor.process(resolveResult);
	}

	@Override
	@RequiredReadAction
	public boolean execute(@NotNull PsiElement element, ResolveState state)
	{
		DotNetGenericExtractor extractor = state.get(CSharpResolveUtil.EXTRACTOR);
		assert extractor != null;

		for(PsiElement psiElement : OverrideUtil.getAllMembers(element, myScope, extractor, true, false))
		{
			ProgressManager.checkCanceled();

			if(!ExecuteTargetUtil.isMyElement(this, psiElement))
			{
				continue;
			}

			addElement(psiElement);
		}
		return true;
	}

	@RequiredReadAction
	public void addElement(@NotNull PsiElement element)
	{
		if(element instanceof DotNetModifierListOwner && !CSharpVisibilityUtil.isVisible((DotNetModifierListOwner) element, myPlace))
		{
			return;
		}

		if(myContextType != CSharpContextUtil.ContextType.ANY)
		{
			CSharpContextUtil.ContextType contextForResolved = CSharpContextUtil.getContextForResolved(element);
			switch(myContextType)
			{
				case INSTANCE:
					if(contextForResolved == CSharpContextUtil.ContextType.STATIC)
					{
						return;
					}
					break;
				case STATIC:
					if(contextForResolved.isAllowInstance())
					{
						return;
					}
					break;
			}
		}
		myProcessor.process(new CSharpResolveResult(element));
	}
}
