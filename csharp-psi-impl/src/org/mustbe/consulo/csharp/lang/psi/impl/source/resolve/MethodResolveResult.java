package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public class MethodResolveResult extends CSharpResolveResult
{
	@NotNull
	private final MethodCalcResult myCalcResult;

	public MethodResolveResult(@NotNull PsiElement element, @NotNull MethodCalcResult calcResult)
	{
		super(element, calcResult.isValidResult());
		myCalcResult = calcResult;
	}

	@NotNull
	public MethodCalcResult getCalcResult()
	{
		return myCalcResult;
	}
}
