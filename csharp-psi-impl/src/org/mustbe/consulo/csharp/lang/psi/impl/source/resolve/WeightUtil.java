package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 24.10.14
 */
public class WeightUtil
{
	public static final int MAX_WEIGHT = Integer.MAX_VALUE;

	public static final Comparator<Pair<MethodCalcResult, PsiElement>> ourComparator = new Comparator<Pair<MethodCalcResult, PsiElement>>()
	{
		@Override
		public int compare(Pair<MethodCalcResult, PsiElement> o1, Pair<MethodCalcResult, PsiElement> o2)
		{
			return o2.getFirst().getWeight() - o1.getFirst().getWeight();
		}
	};

	@NotNull
	public static ResolveResult[] sortAndReturn(List<Pair<MethodCalcResult, PsiElement>> list)
	{
		if(list.isEmpty())
		{
			return ResolveResult.EMPTY_ARRAY;
		}
		ContainerUtil.sort(list, ourComparator);

		ResolveResult[] resolveResults = new ResolveResult[list.size()];
		int i = 0;
		for(Pair<MethodCalcResult, PsiElement> pair : list)
		{
			resolveResults[i++] = new MethodResolveResult(pair.getSecond(), pair.getFirst());
		}
		return resolveResults;
	}
}
