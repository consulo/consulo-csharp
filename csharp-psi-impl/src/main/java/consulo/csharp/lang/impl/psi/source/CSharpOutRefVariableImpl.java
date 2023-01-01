/*
 * Copyright 2013-2019 consulo.io
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

package consulo.csharp.lang.impl.psi.source;

import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.ObjectUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.source.resolve.MethodResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.arguments.NCallArgument;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpRefTypeRef;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.ResolveResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author VISTALL
 * @since 2019-09-25
 */
public class CSharpOutRefVariableImpl extends CSharpVariableImpl
{
	public CSharpOutRefVariableImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitOutRefVariable(this);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromInitializer)
	{
		DotNetType type = getType();
		if(type == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		DotNetTypeRef typeRef = type.toTypeRef();
		if(!resolveFromInitializer && typeRef == DotNetTypeRef.AUTO_TYPE)
		{
			return new CSharpOutRefAutoTypeRef(getExpressionType());
		}

		if(typeRef == DotNetTypeRef.AUTO_TYPE)
		{
			if(myTypeRefProcessing.get())
			{
				return new CSharpOutRefAutoTypeRef(getExpressionType());
			}

			try
			{
				myTypeRefProcessing.set(Boolean.TRUE);
				return searchTypeRefFromCall((DotNetExpression) getParent());
			}
			finally
			{
				myTypeRefProcessing.set(Boolean.FALSE);
			}
		}
		else
		{
			return typeRef;
		}
	}

	@Nonnull
	@RequiredReadAction
	private CSharpRefTypeRef.Type getExpressionType()
	{
		CSharpOutRefVariableExpressionImpl parent = (CSharpOutRefVariableExpressionImpl) getParent();
		return parent.getExpressionType();
	}

	@Nonnull
	public static DotNetTypeRef searchTypeRefFromCall(DotNetExpression expression)
	{
		PsiElement exprParent = expression.getParent();
		if(!(exprParent instanceof CSharpCallArgument))
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		CSharpCallArgument callArgument = (CSharpCallArgument) exprParent;

		CSharpCallArgumentListOwner listOwner = PsiTreeUtil.getParentOfType(exprParent, CSharpCallArgumentListOwner.class);
		if(listOwner == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		ResolveResult[] resolveResults = listOwner.multiResolve(false);
		if(resolveResults.length != 1)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		ResolveResult result = resolveResults[0];
		if(!(result instanceof MethodResolveResult))
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		List<NCallArgument> arguments = ((MethodResolveResult) result).getCalcResult().getArguments();

		for(NCallArgument argument : arguments)
		{
			CSharpCallArgument searchCallArgument = argument.getCallArgument();
			if(callArgument == searchCallArgument)
			{
				DotNetTypeRef parameterTypeRef = argument.getParameterTypeRef();
				return ObjectUtil.notNull(parameterTypeRef, DotNetTypeRef.ERROR_TYPE);
			}
		}
		return DotNetTypeRef.ERROR_TYPE;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetType getType()
	{
		return findNotNullChildByClass(DotNetType.class);
	}
}
