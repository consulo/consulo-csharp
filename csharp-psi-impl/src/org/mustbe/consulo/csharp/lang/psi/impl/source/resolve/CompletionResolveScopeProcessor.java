package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpContextUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpVisibilityUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.search.GlobalSearchScope;

/**
 * @author VISTALL
 * @since 23.10.14
 */
public class CompletionResolveScopeProcessor extends AbstractScopeProcessor
{
	@NotNull
	private final GlobalSearchScope myScope;
	@NotNull
	private final PsiElement myPlace;
	@NotNull
	private CSharpContextUtil.ContextType myContextType;

	public CompletionResolveScopeProcessor(@NotNull PsiElement place, @NotNull ResolveResult[] elements, @NotNull ExecuteTarget[] targets)
	{
		myPlace = place;
		Collections.addAll(myElements, elements);
		myScope = place.getResolveScope();
		myContextType = place instanceof CSharpReferenceExpression ? CSharpContextUtil.getParentContextTypeForReference((CSharpReferenceExpression)
				place) : CSharpContextUtil.ContextType.ANY;
		putUserData(ExecuteTargetUtil.EXECUTE_TARGETS, ExecuteTargetUtil.of(targets));
	}

	@Override
	public boolean execute(@NotNull PsiElement element, ResolveState state)
	{
		DotNetGenericExtractor extractor = state.get(CSharpResolveUtil.EXTRACTOR);
		assert extractor != null;

		for(PsiElement psiElement : OverrideUtil.getAllMembers(element, myScope, extractor))
		{
			if(!ExecuteTargetUtil.isMyElement(this, psiElement))
			{
				continue;
			}
			addElement(psiElement);
		}
		return true;
	}

	@Override
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
		super.addElement(element);
	}
}
