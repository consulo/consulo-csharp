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

package consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetPsiSearcher;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 05.05.14
 */
public class CSharpLambdaTypeRef extends DotNetTypeRefWithCachedResult
{
	private class Result implements CSharpLambdaResolveResult
	{
		private final PsiElement myScope;

		public Result(PsiElement scope)
		{
			myScope = scope;
		}

		@Nullable
		@Override
		@RequiredReadAction
		public PsiElement getElement()
		{
			if(myTarget == null)
			{
				return DotNetPsiSearcher.getInstance(myScope.getProject()).findType(DotNetTypes.System.MulticastDelegate, myScope.getResolveScope(), CSharpTransform.INSTANCE);
			}
			return CSharpLambdaResolveResultUtil.createTypeFromDelegate(myTarget, myExtractor);
		}

		@NotNull
		@Override
		public DotNetGenericExtractor getGenericExtractor()
		{
			return myExtractor;
		}

		@Override
		public boolean isNullable()
		{
			return true;
		}

		@RequiredReadAction
		@NotNull
		@Override
		public CSharpSimpleParameterInfo[] getParameterInfos()
		{
			CSharpSimpleParameterInfo[] parameterInfos = myParameterInfos;
			if(myExtractor == DotNetGenericExtractor.EMPTY)
			{
				return parameterInfos;
			}
			CSharpSimpleParameterInfo[] temp = new CSharpSimpleParameterInfo[parameterInfos.length];
			for(int i = 0; i < parameterInfos.length; i++)
			{
				CSharpSimpleParameterInfo parameterInfo = parameterInfos[i];
				DotNetTypeRef typeRef = GenericUnwrapTool.exchangeTypeRef(parameterInfo.getTypeRef(), getGenericExtractor(), myScope);
				temp[i] = new CSharpSimpleParameterInfo(parameterInfo.getIndex(), parameterInfo.getName(), parameterInfo.getElement(), typeRef);
			}
			return temp;
		}

		@RequiredReadAction
		@Override
		public boolean isInheritParameters()
		{
			return myInheritParameters;
		}

		@RequiredReadAction
		@NotNull
		@Override
		public DotNetTypeRef getReturnTypeRef()
		{
			return GenericUnwrapTool.exchangeTypeRef(myReturnType, getGenericExtractor(), myScope);
		}

		@RequiredReadAction
		@NotNull
		@Override
		public DotNetTypeRef[] getParameterTypeRefs()
		{
			return CSharpSimpleParameterInfo.toTypeRefs(getParameterInfos());
		}

		@Nullable
		@Override
		public CSharpMethodDeclaration getTarget()
		{
			return myTarget;
		}
	}

	private final PsiElement myElement;
	private final CSharpMethodDeclaration myTarget;
	private final CSharpSimpleParameterInfo[] myParameterInfos;
	private final DotNetTypeRef myReturnType;
	private final boolean myInheritParameters;
	private DotNetGenericExtractor myExtractor = DotNetGenericExtractor.EMPTY;

	@RequiredReadAction
	public CSharpLambdaTypeRef(@NotNull CSharpMethodDeclaration method)
	{
		this(method, method, method.getParameterInfos(), method.getReturnTypeRef());
	}

	@RequiredReadAction
	public CSharpLambdaTypeRef(@NotNull PsiElement scope, @NotNull CSharpMethodDeclaration method, @NotNull DotNetGenericExtractor extractor)
	{
		this(scope, method, method.getParameterInfos(), method.getReturnTypeRef());
		myExtractor = extractor;
	}

	@RequiredReadAction
	public CSharpLambdaTypeRef(@NotNull PsiElement scope, @Nullable CSharpMethodDeclaration target, @NotNull CSharpSimpleParameterInfo[] parameterInfos, @NotNull DotNetTypeRef returnType)
	{
		this(scope, target, parameterInfos, returnType, false);
	}

	@RequiredReadAction
	public CSharpLambdaTypeRef(@NotNull PsiElement scope, @Nullable CSharpMethodDeclaration target, @NotNull CSharpSimpleParameterInfo[] parameterInfos, @NotNull DotNetTypeRef returnType, boolean inheritParameters)
	{
		myElement = scope;
		myTarget = target;
		myParameterInfos = parameterInfos;
		myReturnType = returnType;
		myInheritParameters = inheritParameters;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public String toString()
	{
		if(myTarget != null)
		{
			return myTarget.getPresentableQName();
		}
		StringBuilder builder = new StringBuilder();
		builder.append("{(");
		for(int i = 0; i < myParameterInfos.length; i++)
		{
			if(i != 0)
			{
				builder.append(", ");
			}
			DotNetTypeRef parameterType = myParameterInfos[i].getTypeRef();
			if(parameterType == AUTO_TYPE)
			{
				builder.append("?");
			}
			else
			{
				builder.append(parameterType.toString());
			}
		}
		builder.append(")");
		if(myReturnType == AUTO_TYPE)
		{
			builder.append(" => ?");
		}
		else
		{
			builder.append(" => ").append(myReturnType.toString());
		}
		builder.append("}");
		return builder.toString();
	}

	@Nullable
	public PsiElement getTarget()
	{
		return myTarget;
	}

	@RequiredReadAction
	@NotNull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		return new Result(myElement);
	}
}
