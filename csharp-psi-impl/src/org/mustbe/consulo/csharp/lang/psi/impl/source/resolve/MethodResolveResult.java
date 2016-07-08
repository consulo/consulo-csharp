package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import consulo.lombok.annotations.ArrayFactoryFields;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;

/**
 * @author VISTALL
 * @since 02.11.14
 */
@ArrayFactoryFields
public class MethodResolveResult extends CSharpResolveResult
{
	@NotNull
	public static MethodResolveResult createResult(@NotNull MethodCalcResult calcResult, @NotNull PsiElement element, @Nullable ResolveResult resolveResult)
	{
		PsiElement providerElement = element.getUserData(FORCE_PROVIDER_ELEMENT);
		if(providerElement == null && resolveResult instanceof CSharpResolveResult)
		{
			providerElement = ((CSharpResolveResult) resolveResult).getProviderElement();
		}
		MethodResolveResult methodResolveResult = new MethodResolveResult(element, calcResult);
		methodResolveResult.setProvider(providerElement);
		return methodResolveResult;
	}

	@NotNull
	private final MethodCalcResult myCalcResult;

	private MethodResolveResult(@NotNull PsiElement element, @NotNull MethodCalcResult calcResult)
	{
		super(element, calcResult.isValidResult());
		myCalcResult = calcResult;
	}

	@NotNull
	public MethodCalcResult getCalcResult()
	{
		return myCalcResult;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}
		if(!super.equals(o))
		{
			return false;
		}

		MethodResolveResult that = (MethodResolveResult) o;

		if(!myCalcResult.equals(that.myCalcResult))
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + myCalcResult.hashCode();
		return result;
	}
}
