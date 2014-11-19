package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpLikeMethodDeclarationImplUtil;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetVirtualImplementOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import lombok.val;

/**
 * @author VISTALL
 * @since 08.11.14
 */
public class CS0539 extends CompilerCheck<DotNetVirtualImplementOwner>
{
	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull DotNetVirtualImplementOwner element)
	{
		PsiElement nameIdentifier = ((PsiNameIdentifierOwner) element).getNameIdentifier();
		if(nameIdentifier == null)
		{
			return null;
		}
		val resultPair = CSharpLikeMethodDeclarationImplUtil.resolveVirtualImplementation(element, element);
		switch(resultPair.getFirst())
		{
			case CANT_HAVE:
			case FOUND:
			default:
				return null;
			case NOT_FOUND:
				return newBuilder(nameIdentifier, formatElement(element));
		}
	}
}
