/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 05.05.14
 */
public class CSharpLambdaTypeRef implements DotNetTypeRef
{
	private final CSharpMethodDeclaration myTarget;
	private final CSharpSimpleParameterInfo[] myParameterInfos;
	private final DotNetTypeRef myReturnType;
	private final boolean myInheritParameters;

	public CSharpLambdaTypeRef(@NotNull CSharpMethodDeclaration method)
	{
		this(method, method.getParameterInfos(), method.getReturnTypeRef());
	}

	public CSharpLambdaTypeRef(@Nullable CSharpMethodDeclaration target, @NotNull CSharpSimpleParameterInfo[] parameterInfos, @NotNull DotNetTypeRef returnType)
	{
		this(target, parameterInfos, returnType, false);
	}

	public CSharpLambdaTypeRef(@Nullable CSharpMethodDeclaration target, @NotNull CSharpSimpleParameterInfo[] parameterInfos,
			@NotNull DotNetTypeRef returnType, boolean inheritParameters)
	{
		myTarget = target;
		myParameterInfos = parameterInfos;
		myReturnType = returnType;
		myInheritParameters = inheritParameters;
	}

	@NotNull
	@Override
	@LazyInstance
	public String getPresentableText()
	{
		if(myTarget != null)
		{
			return myTarget.getName();
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
				builder.append(parameterType.getPresentableText());
			}
		}
		builder.append(")");
		if(myReturnType == AUTO_TYPE)
		{
			builder.append(" => ?");
		}
		else
		{
			builder.append(" => ").append(myReturnType.getPresentableText());
		}
		builder.append("}");
		return builder.toString();
	}

	@NotNull
	@Override
	@LazyInstance
	public String getQualifiedText()
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
				builder.append(parameterType.getQualifiedText());
			}
		}
		builder.append(")");
		if(myReturnType == AUTO_TYPE)
		{
			builder.append(" => ?");
		}
		else
		{
			builder.append(" => ").append(myReturnType.getQualifiedText());
		}
		builder.append("}");
		return builder.toString();
	}

	@Nullable
	public PsiElement getTarget()
	{
		return myTarget;
	}

	@NotNull
	@Override
	public DotNetTypeResolveResult resolve(@NotNull final PsiElement scope)
	{
		return new CSharpLambdaResolveResult()
		{
			@Nullable
			@Override
			public PsiElement getElement()
			{
				if(myTarget == null)
				{
					return DotNetPsiSearcher.getInstance(scope.getProject()).findType(DotNetTypes.System.MulticastDelegate,
							scope.getResolveScope(), DotNetPsiSearcher.TypeResoleKind.UNKNOWN, CSharpTransform.INSTANCE);
				}
				return CSharpLambdaResolveResultUtil.createTypeFromDelegate(myTarget);
			}

			@NotNull
			@Override
			public DotNetGenericExtractor getGenericExtractor()
			{
				return DotNetGenericExtractor.EMPTY;
			}

			@Override
			public boolean isNullable()
			{
				return true;
			}

			@NotNull
			@Override
			public CSharpSimpleParameterInfo[] getParameterInfos()
			{
				return myParameterInfos;
			}

			@Override
			public boolean isInheritParameters()
			{
				return myInheritParameters;
			}

			@NotNull
			@Override
			public DotNetTypeRef getReturnTypeRef()
			{
				return myReturnType;
			}

			@NotNull
			@Override
			public DotNetTypeRef[] getParameterTypeRefs()
			{
				return CSharpSimpleParameterInfo.toTypeRefs(myParameterInfos);
			}

			@Nullable
			@Override
			public CSharpMethodDeclaration getTarget()
			{
				return myTarget;
			}
		};
	}
}
