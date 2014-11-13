package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayTypeImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetTypeWithTypeArguments;
import org.mustbe.consulo.dotnet.psi.DotNetUserType;
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
	public CompilerCheckResult checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull DotNetType type)
	{
		if(type.getParent() instanceof DotNetTypeWithTypeArguments)
		{
			return null;
		}

		int foundCount = 0;
		PsiElement elementToHighlight = type;
		if(type instanceof DotNetUserType)
		{
			CSharpReferenceExpression referenceExpression = (CSharpReferenceExpression) ((DotNetUserType) type).getReferenceExpression();

			elementToHighlight = referenceExpression.getReferenceElement();
			foundCount = referenceExpression.getTypeArgumentListRefs().length;
		}
		else if(type instanceof CSharpArrayTypeImpl)
		{
			elementToHighlight = ((CSharpArrayTypeImpl) type).getInnerType();
			foundCount = 1;
		}

		if(elementToHighlight == null)
		{
			return null;
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
			return result(elementToHighlight, formatElement(resolvedElement), String.valueOf(expectedCount));
		}
		return null;
	}
}
