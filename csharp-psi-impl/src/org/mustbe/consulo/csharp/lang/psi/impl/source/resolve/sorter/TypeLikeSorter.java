package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.sorter;

import java.util.Comparator;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImplUtil;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 27.10.14
 */
public class TypeLikeSorter implements ResolveResultSorter
{
	public static class ResolveResultComparator implements Comparator<ResolveResult>
	{
		private final int myGenericCount;

		public ResolveResultComparator(int genericCount)
		{
			myGenericCount = genericCount;
		}

		@Override
		public int compare(ResolveResult o1, ResolveResult o2)
		{
			return getWeight(o2) - getWeight(o1);
		}

		private int getWeight(ResolveResult resolveResult)
		{
			PsiElement element = resolveResult.getElement();
			if(element instanceof DotNetVariable)
			{
				return 1000;
			}

			if(element instanceof DotNetGenericParameterListOwner)
			{
				if(((DotNetGenericParameterListOwner) element).getGenericParametersCount() == myGenericCount)
				{
					return 500;
				}
				return 250;
			}

			if(element instanceof DotNetNamespaceAsElement)
			{
				return 0;
			}

			return 50;
		}
	}

	@NotNull
	public static TypeLikeSorter createByReference(@NotNull PsiElement element)
	{
		int size = 0;
		if(element instanceof CSharpReferenceExpression)
		{
			size = CSharpReferenceExpressionImplUtil.getTypeArgumentListSize((CSharpReferenceExpression) element);
		}
		return new TypeLikeSorter(size);
	}

	private final ResolveResultComparator myComparator;

	public TypeLikeSorter(int size)
	{
		myComparator = new ResolveResultComparator(size);
	}

	@Override
	public void sort(@NotNull ResolveResult[] resolveResults)
	{
		ContainerUtil.sort(resolveResults, myComparator);
	}
}
