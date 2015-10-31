/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpConstantTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpFastImplicitTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 31.10.2015
 */
public abstract class CSharpConstantBaseTypeRef extends DotNetTypeRef.Delegate implements CSharpFastImplicitTypeRef
{
	protected CSharpConstantExpressionImpl myExpression;

	public CSharpConstantBaseTypeRef(CSharpConstantExpressionImpl expression, DotNetTypeRef delegate)
	{
		super(delegate);
		myExpression = expression;
	}

	@Nullable
	@Override
	@RequiredReadAction
	public DotNetTypeRef doMirror(@NotNull DotNetTypeRef another, PsiElement scope)
	{
		DotNetTypeRef anotherTypeRef = CSharpConstantTypeRef.testNumberConstant(myExpression, getPrefix(), another, scope);
		if(anotherTypeRef != null)
		{
			DotNetTypeRef defaultConstantTypeRef = myExpression.getDefaultConstantTypeRef();
			if(defaultConstantTypeRef != null && CSharpTypeUtil.isTypeEqual(anotherTypeRef, defaultConstantTypeRef, myExpression))
			{
				return null;
			}
			return anotherTypeRef;
		}
		return null;
	}

	@NotNull
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
}
