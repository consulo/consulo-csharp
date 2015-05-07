package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpLikeMethodDeclarationImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodResolver;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public class CSharpElementGroupTypeRef extends DotNetTypeRef.Adapter implements CSharpFastImplicitTypeRef
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

	@Nullable
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

					MethodCalcResult calc = MethodResolver.calc(parameterTypeRefs, methodParameterTypeRef, scope);

					if(calc.isValidResult())
					{
						return new CSharpLambdaTypeRef(null, CSharpLikeMethodDeclarationImplUtil.getParametersInfos((DotNetLikeMethodDeclaration)
								psiElement), ((DotNetLikeMethodDeclaration) psiElement).getReturnTypeRef());
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean isConversion()
	{
		return false;
	}
}
