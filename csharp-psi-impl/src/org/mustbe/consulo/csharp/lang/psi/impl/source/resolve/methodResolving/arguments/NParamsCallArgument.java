package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.operatorResolving.ImplicitCastInfo;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;
import consulo.csharp.lang.CSharpCastType;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public class NParamsCallArgument extends NCallArgument
{
	@NotNull
	private final List<CSharpCallArgument> myCallArguments;

	public NParamsCallArgument(@NotNull List<CSharpCallArgument> callArguments, @Nullable DotNetParameter parameter)
	{
		super(DotNetTypeRef.ERROR_TYPE, null, parameter);
		myCallArguments = callArguments;
	}

	@Override
	@RequiredReadAction
	public int calcValid(@NotNull PsiElement scope)
	{
		DotNetTypeRef parameterTypeRef = getParameterTypeRef();
		int newVal = FAIL;
		if(parameterTypeRef != null)
		{
			if(CSharpTypeUtil.isTypeEqual(parameterTypeRef, getTypeRef(), scope))
			{
				newVal = PARAMS;
			}
			else
			{
				CSharpTypeUtil.InheritResult inheritable = CSharpTypeUtil.isInheritable(parameterTypeRef, getTypeRef(), scope, CSharpCastType.IMPLICIT);
				if(inheritable.isSuccess())
				{
					if(inheritable.isConversion())
					{
						putUserData(ImplicitCastInfo.IMPLICIT_CAST_INFO, new ImplicitCastInfo(getTypeRef(), parameterTypeRef));
					}

					newVal = PARAMS_INSTANCE_OF;
				}
			}
		}

		myValid = newVal;
		return myValid;
	}

	@NotNull
	@Override
	@LazyInstance
	public DotNetTypeRef getTypeRef()
	{
		assert !myCallArguments.isEmpty();
		List<DotNetTypeRef> typeRefs = new ArrayList<DotNetTypeRef>(myCallArguments.size());
		for(CSharpCallArgument expression : myCallArguments)
		{
			DotNetExpression argumentExpression = expression.getArgumentExpression();
			if(argumentExpression == null)
			{
				continue;
			}
			typeRefs.add(argumentExpression.toTypeRef(false));
		}

		return new CSharpArrayTypeRef(myCallArguments.get(0), typeRefs.get(0), 0);
	}

	@NotNull
	@Override
	public Collection<CSharpCallArgument> getCallArguments()
	{
		return myCallArguments;
	}
}
