package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.ResolveResult;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 24.10.14
 */
public class WeightUtil
{
	public static final int MAX_WEIGHT = Integer.MAX_VALUE;

	public static final Comparator<Pair<Integer, PsiElement>> ourComparator = new Comparator<Pair<Integer, PsiElement>>()
	{
		@Override
		public int compare(Pair<Integer, PsiElement> o1, Pair<Integer, PsiElement> o2)
		{
			return o2.getFirst() - o1.getFirst();
		}
	};

	@NotNull
	public static ResolveResult[] sortAndReturn(List<Pair<Integer, PsiElement>> list)
	{
		if(list.isEmpty())
		{
			return ResolveResult.EMPTY_ARRAY;
		}
		ContainerUtil.sort(list, ourComparator);

		ResolveResult[] resolveResults = new ResolveResult[list.size()];
		int i = 0;
		for(Pair<Integer, PsiElement> pair : list)
		{
			resolveResults[i++] = new PsiElementResolveResult(pair.getSecond(), pair.getFirst() == MAX_WEIGHT);
		}
		return resolveResults;
	}
}
