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

package consulo.csharp.lang.impl.psi.source.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.CSharpConstantExpressionImpl;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpConstantTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpFastImplicitTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 31.10.2015
 */
public abstract class CSharpConstantBaseTypeRef extends DotNetTypeRefWithCachedResult implements CSharpFastImplicitTypeRef
{
	protected CSharpConstantExpressionImpl myExpression;
	private DotNetTypeRef myDelegate;

	public CSharpConstantBaseTypeRef(CSharpConstantExpressionImpl expression, DotNetTypeRef delegate)
	{
		super(expression.getProject(), expression.getResolveScope());
		myExpression = expression;
		myDelegate = delegate;
	}

	@Nonnull
	@Override
	public String getVmQName()
	{
		return myDelegate.getVmQName();
	}

	@RequiredReadAction
	@Nonnull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		return myDelegate.resolve();
	}

	@Nullable
	@Override
	@RequiredReadAction
	public DotNetTypeRef doMirror(@Nonnull DotNetTypeRef another)
	{
		DotNetTypeRef anotherTypeRef = CSharpConstantTypeRef.testNumberConstant(myExpression, getPrefix(), another);
		if(anotherTypeRef != null)
		{
			DotNetTypeRef defaultConstantTypeRef = myExpression.getDefaultConstantTypeRef();
			if(defaultConstantTypeRef != null && CSharpTypeUtil.isTypeEqual(anotherTypeRef, defaultConstantTypeRef))
			{
				return null;
			}
			return anotherTypeRef;
		}
		return null;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String toString()
	{
		return myDelegate.toString();
	}

	@Nonnull
	@RequiredReadAction
	protected String getPrefix()
	{
		return "";
	}

	@Override
	public boolean isConversion()
	{
		return false;
	}

	@Override
	public boolean equals(Object obj)
	{
		return myDelegate.equals(obj);
	}
}
