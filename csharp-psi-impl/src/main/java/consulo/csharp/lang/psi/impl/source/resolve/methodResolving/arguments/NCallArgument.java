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

import com.intellij.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.csharp.lang.psi.impl.CSharpInheritableChecker;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.impl.source.resolve.operatorResolving.ImplicitCastInfo;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.util.dataholder.UserDataHolderBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public class NCallArgument extends UserDataHolderBase
{
	public static final int NOT_CALCULATED = -1;
	public static final int FAIL = 0;
	public static final int EQUAL = 1;
	public static final int INSTANCE_OF = 2;
	public static final int PARAMS = 3;
	public static final int PARAMS_INSTANCE_OF = 4;

	private final DotNetTypeRef myTypeRef;
	@Nullable
	private final CSharpCallArgument myCallArgument;

	/**
	 * It can be DotNetTypeRef or DotNetParameter
	 */
	private final Object myParameterObject;

	protected int myValid = NOT_CALCULATED;

	public NCallArgument(@Nonnull DotNetTypeRef typeRef, @Nullable CSharpCallArgument callArgument, @Nullable Object parameterObject)
	{
		myTypeRef = typeRef;
		myCallArgument = callArgument;
		myParameterObject = parameterObject;

		if(callArgument != null)
		{
			ImplicitCastInfo implicitCastInfo = callArgument.getUserData(ImplicitCastInfo.IMPLICIT_CAST_INFO);
			if(implicitCastInfo != null)
			{
				// copy it
				putUserData(ImplicitCastInfo.IMPLICIT_CAST_INFO, implicitCastInfo);
			}
		}
	}

	@Nonnull
	public Collection<CSharpCallArgument> getCallArguments()
	{
		if(myCallArgument == null)
		{
			return Collections.emptyList();
		}
		return Collections.singletonList(myCallArgument);
	}

	@Nonnull
	public DotNetTypeRef getTypeRef()
	{
		return myTypeRef;
	}

	@Nullable
	public DotNetTypeRef getParameterTypeRef()
	{
		if(myParameterObject instanceof DotNetTypeRef)
		{
			return (DotNetTypeRef) myParameterObject;
		}
		else if(myParameterObject instanceof DotNetParameter)
		{
			return ((DotNetParameter) myParameterObject).toTypeRef(true);
		}
		else if(myParameterObject instanceof CSharpSimpleParameterInfo)
		{
			return ((CSharpSimpleParameterInfo) myParameterObject).getTypeRef();
		}
		return null;
	}

	public boolean isValid()
	{
		if(myValid == NOT_CALCULATED)
		{
			throw new IllegalArgumentException("This parameter valid not calculated");
		}
		return myValid != FAIL;
	}

	@RequiredReadAction
	public int calcValid(@Nonnull PsiElement scope, boolean disableNullableElementCheck)
	{
		DotNetTypeRef parameterTypeRef = getParameterTypeRef();
		int newVal = FAIL;
		if(parameterTypeRef != null)
		{
			DotNetTypeRef typeRef = getTypeRef();
			if(CSharpTypeUtil.isTypeEqual(parameterTypeRef, typeRef, scope))
			{
				newVal = EQUAL;
			}
			else
			{
				CSharpInheritableChecker checker = CSharpInheritableChecker.create(parameterTypeRef, typeRef, scope);
				checker = checker.withCastType(CSharpCastType.IMPLICIT);
				if(disableNullableElementCheck)
				{
					checker = checker.withDisableNullableCheck();
				}
				
				CSharpTypeUtil.InheritResult inheritable = checker.check();
				if(inheritable.isSuccess())
				{
					if(inheritable.isConversion())
					{
						putUserData(ImplicitCastInfo.IMPLICIT_CAST_INFO, new ImplicitCastInfo(typeRef, parameterTypeRef));
					}

					newVal = INSTANCE_OF;
				}
			}
		}

		myValid = newVal;
		return myValid;
	}

	@Nullable
	public String getParameterName()
	{
		if(myParameterObject instanceof DotNetParameter)
		{
			return ((DotNetParameter) myParameterObject).getName();
		}
		else if(myParameterObject instanceof CSharpSimpleParameterInfo)
		{
			return ((CSharpSimpleParameterInfo) myParameterObject).getNotNullName();
		}
		return null;
	}

	@Nullable
	public PsiElement getParameterElement()
	{
		if(myParameterObject instanceof DotNetParameter)
		{
			return (PsiElement) myParameterObject;
		}
		else if(myParameterObject instanceof CSharpSimpleParameterInfo)
		{
			return ((CSharpSimpleParameterInfo) myParameterObject).getElement();
		}
		return null;
	}

	@Nullable
	public Object getParameterObject()
	{
		return myParameterObject;
	}

	@Nullable
	public CSharpCallArgument getCallArgument()
	{
		return myCallArgument;
	}
}
