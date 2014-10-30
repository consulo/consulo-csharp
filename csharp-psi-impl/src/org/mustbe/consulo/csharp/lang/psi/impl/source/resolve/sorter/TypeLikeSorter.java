package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.sorter;

import java.util.Comparator;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeWithTypeArguments;
import org.mustbe.consulo.dotnet.psi.DotNetUserType;
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
			int compare = compare(o1);
			if(compare != 0)
			{
				return -compare;
			}
			compare = compare(o2);
			if(compare != 0)
			{
				return compare;
			}
			return 0;
		}

		private int compare(ResolveResult r)
		{
			PsiElement element = r.getElement();
			if(element instanceof DotNetNamespaceAsElement)
			{
				return -1;
			}

			if(element instanceof DotNetVariable && element instanceof DotNetQualifiedElement)
			{
				return 2;
			}

			if(element instanceof DotNetGenericParameterListOwner && ((DotNetGenericParameterListOwner) element).getGenericParametersCount() ==
					myGenericCount)
			{
				return 1;
			}
			return 0;
		}
	}

	@NotNull
	public static TypeLikeSorter createByReference(@NotNull PsiElement element, boolean codeFragmentIsAvailable)
	{
		int size = 0;
		// when we working from with codefragment we dont need go parent
		PsiElement parent = codeFragmentIsAvailable ? element : element.getParent();
		if(parent instanceof DotNetUserType)
		{
			PsiElement userTypeParent = parent.getParent();
			if(userTypeParent instanceof DotNetTypeWithTypeArguments)
			{
				size = ((DotNetTypeWithTypeArguments) userTypeParent).getArguments().length;
			}
		}
		return new TypeLikeSorter(size);
	}

	private final Comparator<ResolveResult> myComparator;

	public TypeLikeSorter(int genericCount)
	{
		myComparator = new ResolveResultComparator(genericCount);
	}

	@Override
	public void sort(@NotNull ResolveResult[] resolveResults)
	{
	/*	System.out.println("before: ");
		for(ResolveResult resolveResult : resolveResults)
		{
			PsiElement element = resolveResult.getElement();
			System.out.println(" element: " + element.getClass().getSimpleName());
			if(element instanceof DotNetGenericParameterListOwner)
			{
				System.out.println(" generic count: " + ((DotNetGenericParameterListOwner) element).getGenericParametersCount());
			}
		}
		          */
		ContainerUtil.sort(resolveResults, myComparator);

	/*	System.out.println("after: ");
		for(ResolveResult resolveResult : resolveResults)
		{
			PsiElement element = resolveResult.getElement();
			System.out.println(" element: " + element.getClass().getSimpleName());
			if(element instanceof DotNetGenericParameterListOwner)
			{
				System.out.println(" generic count: " + ((DotNetGenericParameterListOwner) element).getGenericParametersCount());
			}
		}
               */
	}
}
