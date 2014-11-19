package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpOperatorReferenceImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;

/**
 * @author VISTALL
 * @since 19.11.14
 */
public class CC0002 extends CompilerCheck<CSharpOperatorReferenceImpl>
{
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpOperatorReferenceImpl element)
	{
		return CC0001.checkReference(element, element);
	}
}
