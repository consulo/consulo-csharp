/*
 * Copyright 2013-2017 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.impl.source.resolve.operatorResolving.ImplicitCastInfo;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public class NParamsCallArgument extends NCallArgument
{
	@Nonnull
	private final List<CSharpCallArgument> myCallArguments;
	private final NotNullLazyValue<DotNetTypeRef> myTypeRefValue;

	public NParamsCallArgument(@Nonnull List<CSharpCallArgument> callArguments, @Nullable DotNetParameter parameter)
	{
		super(DotNetTypeRef.ERROR_TYPE, null, parameter);
		myCallArguments = callArguments;
		myTypeRefValue = NotNullLazyValue.createValue(() ->
		{
			assert !myCallArguments.isEmpty();
			List<DotNetTypeRef> typeRefs = new ArrayList<>(myCallArguments.size());
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
		});
	}

	@Override
	@RequiredReadAction
	public int calcValid(@Nonnull PsiElement scope)
	{
		myValid = validate(getParameterTypeRef(), getTypeRef(), this, scope);
		return myValid;
	}

	@RequiredReadAction
	protected static int validate(@Nullable DotNetTypeRef parameterTypeRef, @Nonnull DotNetTypeRef typeRef, @Nonnull UserDataHolderBase holder, @Nonnull PsiElement scope)
	{
		int newVal = FAIL;
		if(parameterTypeRef != null)
		{
			if(CSharpTypeUtil.isTypeEqual(parameterTypeRef, typeRef, scope))
			{
				newVal = PARAMS;
			}
			else
			{
				CSharpTypeUtil.InheritResult inheritable = CSharpTypeUtil.isInheritable(parameterTypeRef, typeRef, scope, CSharpCastType.IMPLICIT);
				if(inheritable.isSuccess())
				{
					if(inheritable.isConversion())
					{
						holder.putUserData(ImplicitCastInfo.IMPLICIT_CAST_INFO, new ImplicitCastInfo(typeRef, parameterTypeRef));
					}

					newVal = PARAMS_INSTANCE_OF;
				}
			}
		}
		return newVal;
	}

	@Nonnull
	@Override
	public DotNetTypeRef getTypeRef()
	{
		return myTypeRefValue.getValue();
	}

	@Nonnull
	@Override
	public Collection<CSharpCallArgument> getCallArguments()
	{
		return myCallArguments;
	}
}
