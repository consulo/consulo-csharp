package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodAcceptorImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.WeightUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public class CSharpElementGroupTypeRef extends DotNetTypeRef.Adapter implements CSharpChameleonTypeRef
{
	private final CSharpElementGroup<?> myElementGroup;

	public CSharpElementGroupTypeRef(CSharpElementGroup<?> elementGroup)
	{
		myElementGroup = elementGroup;
	}

	@NotNull
	@Override
	public String getPresentableText()
	{
		return myElementGroup.getName();
	}

	@NotNull
	@Override
	public String getQualifiedText()
	{
		return myElementGroup.getName();
	}

	@NotNull
	@Override
	public DotNetTypeRef doMirror(@NotNull DotNetTypeRef another, PsiElement scope)
	{
		DotNetTypeResolveResult typeResolveResult = another.resolve(scope);
		if(typeResolveResult instanceof CSharpLambdaResolveResult)
		{
			DotNetTypeRef[] parameterTypeRefs = ((CSharpLambdaResolveResult) typeResolveResult).getParameterTypeRefs();

			for(PsiElement psiElement : myElementGroup.getElements())
			{
				if(psiElement instanceof DotNetLikeMethodDeclaration)
				{
					DotNetTypeRef[] methodParameterTypeRef = ((DotNetLikeMethodDeclaration) psiElement).getParameterTypeRefs();

					int weight = MethodAcceptorImpl.calcSimpleAcceptableWeight(scope, parameterTypeRefs, methodParameterTypeRef);

					if(weight == WeightUtil.MAX_WEIGHT)
					{
						return new CSharpLambdaTypeRef(null, methodParameterTypeRef, ((DotNetLikeMethodDeclaration) psiElement).getReturnTypeRef());
					}
				}
			}
		}
		return this;
	}
}
