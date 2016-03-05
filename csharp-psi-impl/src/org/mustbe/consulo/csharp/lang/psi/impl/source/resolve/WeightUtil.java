package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
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
	//FIXME [VISTALL] we really need this?
	public static class WeightResult
	{
		@NotNull
		public static WeightUtil.WeightResult from(MethodCalcResult calcResult, PsiElement element, @Nullable ResolveResult resolveResult)
		{
			PsiElement providerElement = null;
			if(resolveResult instanceof CSharpResolveResult)
			{
				providerElement = ((CSharpResolveResult) resolveResult).getProviderElement();
			}
			return new WeightResult(calcResult, element, providerElement);
		}

		private MethodCalcResult myCalcResult;
		private PsiElement myElement;
		private PsiElement myProviderElement;

		private WeightResult(MethodCalcResult calcResult, PsiElement element, PsiElement providerElement)
		{
			myCalcResult = calcResult;
			myElement = element;
			myProviderElement = providerElement;
		}

		@NotNull
		public MethodResolveResult make()
		{
			return (MethodResolveResult) new MethodResolveResult(myElement, myCalcResult).withProvider(myProviderElement);
		}
	}

	public static final int MAX_WEIGHT = Integer.MIN_VALUE;

	public static final Comparator<WeightResult> ourComparator = new Comparator<WeightResult>()
	{
		@Override
		public int compare(WeightResult o1, WeightResult o2)
		{
			return o2.myCalcResult.getWeight() - o1.myCalcResult.getWeight();
		}
	};

	public static void sortAndReturn(@NotNull List<WeightResult> list, @NotNull Processor<ResolveResult> processor)
	{
		if(list.isEmpty())
		{
			return;
		}

		ContainerUtil.sort(list, ourComparator);

		for(WeightResult weightResult : list)
		{
			if(!processor.process(weightResult.make()))
			{
				return;
			}
		}
	}
}
