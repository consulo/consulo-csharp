package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
	@Override
	public boolean executeImpl(@NotNull PsiElement element, ResolveState state)
	{
		PsiNamedElement namedElementOf = getNamedElementOf(element);
		if(namedElementOf == null)
		{
			return true;
		}
		String name = namedElementOf.getName();
		if(name == null)
		{
			return true;
		}

		CSharpResolveSelector selector = state.get(CSharpResolveUtil.SELECTOR);
		if(!(selector instanceof CSharpNamedResolveSelector))
		{
			return true;
		}

		if(((CSharpNamedResolveSelector) selector).isNameEqual(name))
		{
			addElement(namedElementOf);
			return false;
		}
		return true;
	}

	@Nullable
	public PsiNamedElement getNamedElementOf(@NotNull PsiElement element)
	{
		return null;
	}
}
