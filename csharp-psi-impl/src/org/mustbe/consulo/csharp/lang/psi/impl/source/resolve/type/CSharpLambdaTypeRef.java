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
import org.mustbe.consulo.csharp.lang.psi.CSharpPseudoMethod;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 05.05.14
 */
public class CSharpLambdaTypeRef implements DotNetTypeRef
{
	private final PsiElement myTarget;
	private final DotNetTypeRef[] myParameterTypes;
	private final DotNetTypeRef myReturnType;

	public CSharpLambdaTypeRef(@NotNull CSharpPseudoMethod method)
	{
		this(method, method.getParameterTypeRefs(), method.getReturnTypeRef());
	}

	public CSharpLambdaTypeRef(@Nullable PsiElement target, @NotNull DotNetTypeRef[] parameterTypes, @NotNull DotNetTypeRef returnType)
	{
		myTarget = target;
		myParameterTypes = parameterTypes;
		myReturnType = returnType;
	}

	@NotNull
	public DotNetTypeRef getReturnType()
	{
		return myReturnType;
	}

	@NotNull
	public DotNetTypeRef[] getParameterTypes()
	{
		return myParameterTypes;
	}

	@NotNull
	@Override
	@LazyInstance
	public String getPresentableText()
	{
		if(myTarget instanceof DotNetNamedElement)
		{
			return ((DotNetNamedElement) myTarget).getName();
		}
		StringBuilder builder = new StringBuilder();
		builder.append("{(");
		for(int i = 0; i < myParameterTypes.length; i++)
		{
			if(i != 0)
			{
				builder.append(", ");
			}

			DotNetTypeRef parameterType = myParameterTypes[i];
			if(parameterType == AUTO_TYPE)
			{
				builder.append("?");
			}
			else
			{
				builder.append(myParameterTypes[i].getPresentableText());
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
		if(myTarget instanceof DotNetQualifiedElement)
		{
			return ((DotNetQualifiedElement) myTarget).getPresentableQName();
		}
		StringBuilder builder = new StringBuilder();
		builder.append("{(");
		for(int i = 0; i < myParameterTypes.length; i++)
		{
			if(i != 0)
			{
				builder.append(", ");
			}
			DotNetTypeRef parameterType = myParameterTypes[i];
			if(parameterType == AUTO_TYPE)
			{
				builder.append("?");
			}
			else
			{
				builder.append(myParameterTypes[i].getQualifiedText());
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

	@Override
	public boolean isNullable()
	{
		return true;
	}

	@Nullable
	@Override
	public PsiElement resolve(@NotNull PsiElement scope)
	{
		return DotNetPsiSearcher.getInstance(scope.getProject()).findType(DotNetTypes.System_MulticastDelegate, scope.getResolveScope(),
				DotNetPsiSearcher.TypeResoleKind.UNKNOWN, CSharpTransform.INSTANCE);
	}

	@NotNull
	@Override
	public DotNetGenericExtractor getGenericExtractor(@NotNull PsiElement resolved, @NotNull PsiElement scope)
	{
		return DotNetGenericExtractor.EMPTY;
	}
}
