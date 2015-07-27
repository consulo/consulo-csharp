package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpNamedResolveSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 09.10.14
 */
public class SimpleNamedScopeProcessor extends StubScopeProcessor
{
	private Processor<ResolveResult> myCompletionProcessor;
	private boolean myCompletion;

	public SimpleNamedScopeProcessor(@NotNull Processor<ResolveResult> completionProcessor, boolean completion, ExecuteTarget... targets)
	{
		myCompletionProcessor = completionProcessor;
		myCompletion = completion;
		putUserData(ExecuteTargetUtil.EXECUTE_TARGETS, ExecuteTargetUtil.of(targets));
	}

	@RequiredReadAction
	@Override
	public boolean execute(@NotNull PsiElement element, ResolveState state)
	{
		if(!(element instanceof PsiNamedElement) || !ExecuteTargetUtil.isMyElement(this, element))
		{
			return true;
		}

		String name = ((PsiNamedElement) element).getName();
		if(name == null)
		{
			return true;
		}

		if(myCompletion)
		{
			return myCompletionProcessor.process(new PsiElementResolveResult(element, true));
		}
		else
		{
			CSharpResolveSelector selector = state.get(CSharpResolveUtil.SELECTOR);
			if(!(selector instanceof CSharpNamedResolveSelector))
			{
				return true;
			}

			if(((CSharpNamedResolveSelector) selector).isNameEqual(name))
			{
				myCompletionProcessor.process(new PsiElementResolveResult(element, true));
				return false;
			}
		}
		return true;
	}
}
