package org.mustbe.consulo.csharp.lang.psi.impl;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericWrapperTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 28.10.2015
 */
public class CSharpNullableTypeUtil
{
	@NotNull
	public static DotNetTypeRef box(@NotNull DotNetTypeRef typeRef)
	{
		return new CSharpGenericWrapperTypeRef(new CSharpTypeRefByQName(DotNetTypes.System.Nullable$1), typeRef);
	}

	@NotNull
	@RequiredReadAction
	public static DotNetTypeRef unbox(@NotNull DotNetTypeRef typeRef, @NotNull PsiElement scope)
	{
		DotNetTypeResolveResult typeResolveResult = typeRef.resolve(scope);
		PsiElement element = typeResolveResult.getElement();
		if(element == null)
		{
			return typeRef;
		}

		if(element instanceof CSharpTypeDeclaration && Comparing.equal(((CSharpTypeDeclaration) element).getVmQName(), DotNetTypes.System.Nullable$1))
		{
			DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();

			DotNetGenericParameter[] genericParameters = ((CSharpTypeDeclaration) element).getGenericParameters();
			if(genericParameters.length == 1)
			{
				DotNetTypeRef extract = genericExtractor.extract(genericParameters[0]);
				if(extract != null)
				{
					return extract;
				}
			}
		}
		return typeRef;
	}
}
