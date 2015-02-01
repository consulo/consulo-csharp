package org.mustbe.consulo.csharp.ide.refactoring.util;

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;

/**
 * @author: Fedor.Korotkov
 */
public class ComponentNameScopeProcessor implements PsiScopeProcessor
{
	private final Set<PsiNamedElement> result;

	public ComponentNameScopeProcessor(Set<PsiNamedElement> result)
	{
		this.result = result;
	}

	@Override
	public boolean execute(@NotNull PsiElement element, ResolveState state)
	{
		if(element instanceof PsiNamedElement)
		{
			result.add((PsiNamedElement) element);
		}
		return true;
	}

	@Override
	public <T> T getHint(@NotNull Key<T> hintKey)
	{
		return null;
	}

	@Override
	public void handleEvent(Event event, @Nullable Object associated)
	{
	}
}
