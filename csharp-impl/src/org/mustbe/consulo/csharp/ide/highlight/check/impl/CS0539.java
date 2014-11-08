package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetVirtualImplementOwner;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 08.11.14
 */
public class CS0539 extends CompilerCheck<DotNetVirtualImplementOwner>
{
	@Nullable
	@Override
	public CompilerCheckResult checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull DotNetVirtualImplementOwner element)
	{
		DotNetTypeRef typeRefForImplement = element.getTypeRefForImplement();

		DotNetTypeResolveResult typeResolveResult = typeRefForImplement.resolve(element);

		PsiElement resolvedElement = typeResolveResult.getElement();
		DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();
		if(!(resolvedElement instanceof CSharpTypeDeclaration))
		{
			return null;
		}

		for(DotNetNamedElement namedElement : ((CSharpTypeDeclaration) resolvedElement).getMembers())
		{
			namedElement = GenericUnwrapTool.extract(namedElement, genericExtractor, false);

		}
		return null;
	}
}
