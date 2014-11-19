package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;

/**
 * @author VISTALL
 * @since 18.09.14
 */
public class CS0132 extends CompilerCheck<CSharpConstructorDeclaration>
{
	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpConstructorDeclaration element)
	{
		if(element.hasModifier(CSharpModifier.STATIC) && element.getParameters().length > 0)
		{
			return newBuilder(element.getParameterList(), formatElement(element));
		}
		return null;
	}
}
