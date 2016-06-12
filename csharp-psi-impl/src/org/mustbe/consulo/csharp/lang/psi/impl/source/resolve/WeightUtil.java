package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 24.10.14
 */
public class WeightUtil
{
	public static final int MAX_WEIGHT = Integer.MIN_VALUE;

	public static final Comparator<MethodResolveResult> ourComparator = new Comparator<MethodResolveResult>()
	{
		@Override
		public int compare(MethodResolveResult o1, MethodResolveResult o2)
		{
			return o2.getCalcResult().getWeight() - o1.getCalcResult().getWeight();
		}
	};

	@RequiredReadAction
	public static void sortAndProcess(@NotNull List<MethodResolveResult> list, @NotNull Processor<ResolveResult> processor, @NotNull PsiElement place)
	{
		if(list.isEmpty())
		{
			return;
		}

		ContainerUtil.sort(list, ourComparator);

		for(MethodResolveResult methodResolveResult : list)
		{
			methodResolveResult.setAssignable(place);

			if(!processor.process(methodResolveResult))
			{
				return;
			}
		}
	}
}
