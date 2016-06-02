package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.WeightUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import com.intellij.openapi.util.UserDataHolderBase;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public class MethodCalcResult extends UserDataHolderBase
{
	public static final MethodCalcResult VALID = new MethodCalcResult(true, WeightUtil.MAX_WEIGHT, Collections.<NCallArgument>emptyList());

	private final boolean myValid;
	private final int myWeight;
	private final List<NCallArgument> myArguments;

	public MethodCalcResult(boolean valid, int weight, List<NCallArgument> arguments)
	{
		myValid = valid;
		myWeight = weight;
		myArguments = arguments;
	}

	@NotNull
	public MethodCalcResult dupNoResult(int weight)
	{
		return new MethodCalcResult(false, getWeight() + weight, getArguments());
	}

	@NotNull
	public MethodCalcResult dupWithResult(int weight)
	{
		return new MethodCalcResult(myValid, getWeight() + weight, getArguments());
	}

	public boolean isValidResult()
	{
		return myValid;
	}

	public int getWeight()
	{
		return myWeight;
	}

	public List<NCallArgument> getArguments()
	{
		return myArguments;
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

		MethodCalcResult that = (MethodCalcResult) o;

		if(myValid != that.myValid)
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		return (myValid ? 1 : 0);
	}
}
