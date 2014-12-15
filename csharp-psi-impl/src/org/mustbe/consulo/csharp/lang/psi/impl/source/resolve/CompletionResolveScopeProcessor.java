package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpVisibilityUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpResolveContextUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.CommonProcessors;

/**
 * @author VISTALL
 * @since 23.10.14
 */
public class CompletionResolveScopeProcessor extends AbstractScopeProcessor
{
	private final GlobalSearchScope myScope;
	@NotNull
	private final PsiElement myPlace;

	public CompletionResolveScopeProcessor(@NotNull PsiElement place, ResolveResult[] elements, ExecuteTarget[] targets)
	{
		myPlace = place;
		Collections.addAll(myElements, elements);
		myScope = place.getResolveScope();
		putUserData(ExecuteTargetUtil.EXECUTE_TARGETS, ExecuteTargetUtil.of(targets));
	}

	@Override
	public boolean execute(@NotNull PsiElement element, ResolveState state)
	{
		DotNetGenericExtractor extractor = state.get(CSharpResolveUtil.EXTRACTOR);
		assert extractor != null;

		CommonProcessors.CollectProcessor<PsiElement> collectProcessor = new CommonProcessors.CollectProcessor<PsiElement>();
		CSharpResolveContextUtil.createContext(extractor, myScope, element).processElements(collectProcessor, true);

		Collection<PsiElement> results = collectProcessor.getResults();

		List<PsiElement> mergedElements = CSharpResolveUtil.mergeGroupsToIterable(results);
		PsiElement[] psiElements = OverrideUtil.filterOverrideElements(myPlace, mergedElements, CommonProcessors.alwaysTrue());

		for(PsiElement psiElement : CSharpResolveUtil.mergeGroupsToIterable(psiElements))
		{
			addElement(psiElement);
		}
		return true;
	}

	@Override
	public void addElement(@NotNull PsiElement element)
	{
		if(element instanceof DotNetModifierListOwner && !CSharpVisibilityUtil.isVisibleForCompletion((DotNetModifierListOwner) element, myPlace))
		{
			return;
		}
		super.addElement(element);
	}
}
