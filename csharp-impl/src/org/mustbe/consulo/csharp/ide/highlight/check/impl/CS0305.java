package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayTypeImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetTypeWithTypeArguments;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 23.10.14
 */
public class CS0305 extends CompilerCheck<DotNetType>
{
	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull DotNetType type)
	{
		if(type.getParent() instanceof DotNetTypeWithTypeArguments)
		{
			return null;
		}

		int foundCount = 0;
		DotNetType innerType = type;
		if(type instanceof DotNetTypeWithTypeArguments)
		{
			innerType = ((DotNetTypeWithTypeArguments) type).getInnerType();
			foundCount = ((DotNetTypeWithTypeArguments) type).getArguments().length;
		}
		else if(type instanceof CSharpArrayTypeImpl)
		{
			innerType = ((CSharpArrayTypeImpl) type).getInnerType();
			foundCount = 1;
		}

		DotNetTypeResolveResult typeResolveResult = type.toTypeRef().resolve(type);

		int expectedCount = 0;
		PsiElement resolvedElement = typeResolveResult.getElement();
		if(resolvedElement == null)
		{
			return null;
		}

		if(resolvedElement instanceof CSharpConstructorDeclaration)
		{
			resolvedElement = resolvedElement.getParent();
		}

		if(resolvedElement instanceof DotNetGenericParameterListOwner)
		{
			expectedCount = ((DotNetGenericParameterListOwner) resolvedElement).getGenericParametersCount();
		}

		if(expectedCount != foundCount)
		{
			return newBuilder(innerType, formatElement(resolvedElement), String.valueOf(expectedCount));
		}
		return null;
	}
}
