package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpUserType;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 05.08.2015
 */
public class CS0146 extends CompilerCheck<CSharpUserType>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpUserType element)
	{
		if(element.getParent() instanceof DotNetTypeList && element.getParent().getParent() instanceof CSharpTypeDeclaration)
		{
			CSharpTypeDeclaration parent = (CSharpTypeDeclaration) element.getParent().getParent();

			DotNetTypeResolveResult typeResolveResult = element.toTypeRef().resolve();

			PsiElement resolvedElement = typeResolveResult.getElement();
			if(resolvedElement instanceof CSharpTypeDeclaration)
			{
				CSharpTypeDeclaration resolvedTypeDeclaration = (CSharpTypeDeclaration) resolvedElement;
				if(resolvedElement == parent || resolvedTypeDeclaration.isInheritor(((CSharpTypeDeclaration) resolvedElement).getVmQName(), true))
				{
					return newBuilder(element, formatElement(parent), formatElement(resolvedElement));
				}
			}
		}
		return null;
	}
}
