package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.sorter;

import java.util.Comparator;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImplUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;

/**
 * @author VISTALL
 * @since 27.10.14
 */
public class TypeLikeComparator implements Comparator<ResolveResult>
{
	@NotNull
	public static TypeLikeComparator create(@NotNull PsiElement element)
	{
		int size = 0;
		if(element instanceof CSharpReferenceExpression)
		{
			size = CSharpReferenceExpressionImplUtil.getTypeArgumentListSize((CSharpReferenceExpression) element);
		}
		return new TypeLikeComparator(size);
	}

	private final int myGenericCount;

	public TypeLikeComparator(int genericCount)
	{
		myGenericCount = genericCount;
	}

	@Override
	public int compare(ResolveResult o1, ResolveResult o2)
	{
		return getWeight(o2) - getWeight(o1);
	}

	public int getWeight(ResolveResult resolveResult)
	{
		PsiElement element = resolveResult.getElement();

		if(element instanceof DotNetVariable || element instanceof CSharpElementGroup)
		{
			return 100000;
		}
		if(element instanceof DotNetGenericParameterListOwner)
		{
			if(((DotNetGenericParameterListOwner) element).getGenericParametersCount() == myGenericCount)
			{
				return 50000;
			}
			return -((DotNetGenericParameterListOwner) element).getGenericParametersCount() * 100;
		}

		if(element instanceof DotNetNamespaceAsElement)
		{
			return 0;
		}

		return 10;
	}
}
