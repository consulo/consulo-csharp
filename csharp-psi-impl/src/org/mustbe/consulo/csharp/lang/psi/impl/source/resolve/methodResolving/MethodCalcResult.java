package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving;

import java.util.Collections;
import java.util.List;

import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.WeightUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public class MethodCalcResult
{
	public static final MethodCalcResult VALID = new MethodCalcResult(WeightUtil.MAX_WEIGHT, Collections.<NCallArgument>emptyList());

	private int myWeight;
	private List<NCallArgument> myArguments;

	public MethodCalcResult(int weight, List<NCallArgument> arguments)
	{
		myWeight = weight;
		myArguments = arguments;
	}

	public boolean isValidResult()
	{
		return myWeight == WeightUtil.MAX_WEIGHT;
	}

	public int getWeight()
	{
		return myWeight;
	}

	public List<NCallArgument> getArguments()
	{
		return myArguments;
	}
}
