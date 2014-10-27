package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpNamedResolveSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveState;

/**
 * @author VISTALL
 * @since 09.10.14
 */
public class SimpleNamedScopeProcessor extends AbstractScopeProcessor
{
	private final boolean myCompletion;

	public SimpleNamedScopeProcessor(ExecuteTarget... targets)
	{
		this(false, targets);
	}

	public SimpleNamedScopeProcessor(boolean completion, ExecuteTarget... targets)
	{
		myCompletion = completion;
		putUserData(ExecuteTargetUtil.EXECUTE_TARGETS, ExecuteTargetUtil.of(targets));
	}

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
			addElement(element);
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
				addElement(element);
				return false;
			}
		}
		return true;
	}
}
