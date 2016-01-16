package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments;

import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 12.11.14
 */
public class NEmptyParamsCallArgument extends NParamsCallArgument
{
	public NEmptyParamsCallArgument(@NotNull DotNetParameter parameter)
	{
		super(Collections.<CSharpCallArgument>emptyList(), parameter);
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@RequiredReadAction
	@Override
	public int calcValid(@NotNull PsiElement scope)
	{
		return PARAMS;
	}

	@NotNull
	@Override
	public DotNetTypeRef getTypeRef()
	{
		DotNetTypeRef parameterTypeRef = getParameterTypeRef();
		assert parameterTypeRef != null;
		return parameterTypeRef;
	}
}
