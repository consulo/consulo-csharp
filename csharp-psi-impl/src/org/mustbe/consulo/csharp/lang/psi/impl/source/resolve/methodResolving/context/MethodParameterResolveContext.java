package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.context;

import consulo.lombok.annotations.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterListOwner;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Trinity;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public class MethodParameterResolveContext implements ParameterResolveContext<DotNetParameter>
{
	private final PsiElement myScope;
	private final boolean myResolveFromParent;
	private final DotNetParameter[] myParameters;
	private DotNetParameter myParamsParameter;

	public MethodParameterResolveContext(DotNetParameterListOwner parameterListOwner, PsiElement scope, boolean resolveFromParent)
	{
		myScope = scope;
		myResolveFromParent = resolveFromParent;
		myParameters = parameterListOwner.getParameters();
		myParamsParameter = ArrayUtil.getLastElement(myParameters);
		if(myParamsParameter != null && !myParamsParameter.hasModifier(CSharpModifier.PARAMS))
		{
			myParamsParameter = null;
		}
	}

	@Override
	@NotNull
	@Lazy
	public DotNetTypeRef getInnerParamsParameterTypeRef()
	{
		return myParamsParameter == null ? DotNetTypeRef.ERROR_TYPE : CSharpResolveUtil.resolveIterableType(myScope, getParamsParameterTypeRef());
	}

	@Override
	@NotNull
	@Lazy
	public DotNetTypeRef getParamsParameterTypeRef()
	{
		return myParamsParameter == null ? DotNetTypeRef.ERROR_TYPE : myParamsParameter.toTypeRef(true);
	}

	@Override
	@Nullable
	public DotNetParameter getParamsParameter()
	{
		return myParamsParameter;
	}

	@Override
	public int getParametersSize()
	{
		return myParameters.length;
	}

	@Override
	@Nullable
	public DotNetParameter getParameterByIndex(int i)
	{
		return ArrayUtil2.safeGet(myParameters, i);
	}

	@Override
	public DotNetParameter getParameterByName(@NotNull String name)
	{
		for(DotNetParameter parameter : myParameters)
		{
			if(Comparing.equal(parameter.getName(), name))
			{
				return parameter;
			}
		}
		return null;
	}

	@NotNull
	@RequiredReadAction
	@Override
	public Trinity<String, DotNetTypeRef, Boolean> getParameterInfo(@NotNull DotNetParameter parameter)
	{
		return Trinity.create(parameter.getName(), parameter.toTypeRef(true), parameter.hasModifier(CSharpModifier.OPTIONAL));
	}

	@NotNull
	@Override
	public DotNetParameter[] getParameters()
	{
		return myParameters;
	}

	@Override
	public boolean isResolveFromParentTypeRef()
	{
		return myResolveFromParent;
	}
}
