package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpResolveContextUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
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
	private final GlobalSearchScope myScope;

	public CompletionResolveScopeProcessor(GlobalSearchScope scope, ResolveResult[] elements, ExecuteTarget[] targets)
	{
		Collections.addAll(myElements, elements);
		myScope = scope;
		putUserData(ExecuteTargetUtil.EXECUTE_TARGETS, ExecuteTargetUtil.of(targets));
	}

	@Override
	public boolean executeImpl(@NotNull PsiElement element, ResolveState state)
	{
		DotNetGenericExtractor extractor = state.get(CSharpResolveUtil.EXTRACTOR);
		assert extractor != null;

		CSharpResolveContext context = CSharpResolveContextUtil.createContext(extractor, myScope, element);

		for(PsiElement psiElement : context.getElements())
		{
			if(psiElement instanceof CSharpElementGroup)
			{
				for(PsiElement it : ((CSharpElementGroup) psiElement).getElements())
				{
					addElement(it);
				}
			}
			else
			{
				addElement(psiElement);
			}
		}

		return true;
	}
}
