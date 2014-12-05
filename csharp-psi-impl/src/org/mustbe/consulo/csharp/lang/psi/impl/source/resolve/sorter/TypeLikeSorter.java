package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.sorter;

import java.util.Comparator;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericExtractor;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
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
		DotNetTypeRef[] typeRefs = DotNetTypeRef.EMPTY_ARRAY;
		if(element instanceof CSharpReferenceExpression)
		{
			typeRefs = ((CSharpReferenceExpression) element).getTypeArgumentListRefs();
		}
		return new TypeLikeSorter(typeRefs);
	}

	private final ResolveResultComparator myComparator;

	private final DotNetTypeRef[] myTypeArgumentRefs;

	public TypeLikeSorter(DotNetTypeRef[] typeRefs)
	{
		myTypeArgumentRefs = typeRefs;
		myComparator = new ResolveResultComparator(myTypeArgumentRefs.length);
	}

	@Override
	public void sort(@NotNull ResolveResult[] resolveResults)
	{
		ContainerUtil.sort(resolveResults, myComparator);

		if(myTypeArgumentRefs.length > 0)
		{
			for(int i = 0; i < resolveResults.length; i++)
			{
				ResolveResult resolveResult = resolveResults[i];
				PsiElement element = resolveResult.getElement();
				if(element instanceof CSharpTypeDeclaration)
				{
					CSharpTypeDeclaration typeDeclaration = (CSharpTypeDeclaration) element;
					DotNetGenericParameter[] genericParameters = typeDeclaration.getGenericParameters();
					if(genericParameters.length == myTypeArgumentRefs.length)
					{
						resolveResults[i] = new PsiElementResolveResult(new CSharpLightTypeDeclaration(typeDeclaration,
								new CSharpGenericExtractor(genericParameters, myTypeArgumentRefs)));
					}
				}
			}
		}
	}
}
