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

package consulo.csharp.lang.impl.psi.source.resolve.type;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.csharp.lang.impl.psi.msil.CSharpTransform;
import consulo.csharp.lang.impl.psi.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.resolve.*;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 05.05.14
 */
public class CSharpLambdaTypeRef extends DotNetTypeRefWithCachedResult
{
	private class Result implements CSharpLambdaResolveResult
	{
		private final Project myProject;
		private final GlobalSearchScope myResolveScope;

		public Result(Project project, GlobalSearchScope resolveScope)
		{
			myProject = project;
			myResolveScope = resolveScope;
		}

		@Nullable
		@Override
		@RequiredReadAction
		public PsiElement getElement()
		{
			if(myTarget == null)
			{
				return DotNetPsiSearcher.getInstance(myProject).findType(DotNetTypes.System.MulticastDelegate, myResolveScope, CSharpTransform.INSTANCE);
			}
			return CSharpLambdaResolveResultUtil.createTypeFromDelegate(myTarget, myExtractor);
		}

		@Nonnull
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
		@Nonnull
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
				DotNetTypeRef typeRef = GenericUnwrapTool.exchangeTypeRef(parameterInfo.getTypeRef(), getGenericExtractor());
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
		@Nonnull
		@Override
		public DotNetTypeRef getReturnTypeRef()
		{
			return GenericUnwrapTool.exchangeTypeRef(myReturnType, getGenericExtractor());
		}

		@Nullable
		@Override
		public CSharpMethodDeclaration getTarget()
		{
			return GenericUnwrapTool.extract(myTarget, getGenericExtractor());
		}
	}

	private final CSharpMethodDeclaration myTarget;
	private final CSharpSimpleParameterInfo[] myParameterInfos;
	private final DotNetTypeRef myReturnType;
	private final boolean myInheritParameters;
	private DotNetGenericExtractor myExtractor = DotNetGenericExtractor.EMPTY;

	@RequiredReadAction
	public CSharpLambdaTypeRef(@Nonnull CSharpMethodDeclaration method)
	{
		this(method.getProject(), method.getResolveScope(), method, method.getParameterInfos(), method.getReturnTypeRef());
	}

	@RequiredReadAction
	public CSharpLambdaTypeRef(@Nonnull Project project, @Nonnull GlobalSearchScope scope, @Nonnull CSharpMethodDeclaration method, @Nonnull DotNetGenericExtractor extractor)
	{
		this(project, scope, method, method.getParameterInfos(), method.getReturnTypeRef());
		myExtractor = extractor;
	}

	@RequiredReadAction
	public CSharpLambdaTypeRef(@Nonnull Project project,
							   @Nonnull GlobalSearchScope scope,
							   @Nullable CSharpMethodDeclaration target,
							   @Nonnull CSharpSimpleParameterInfo[] parameterInfos,
							   @Nonnull DotNetTypeRef returnType)
	{
		this(project, scope, target, parameterInfos, returnType, false);
	}

	@RequiredReadAction
	public CSharpLambdaTypeRef(@Nonnull Project project,
							   @Nonnull GlobalSearchScope scope,
							   @Nullable CSharpMethodDeclaration target,
							   @Nonnull CSharpSimpleParameterInfo[] parameterInfos,
							   @Nonnull DotNetTypeRef returnType,
							   boolean inheritParameters)
	{
		super(project, scope);
		myTarget = target;
		myParameterInfos = parameterInfos;
		myReturnType = returnType;
		myInheritParameters = inheritParameters;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String getVmQName()
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
	@Nonnull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		return new Result(getProject(), getResolveScope());
	}
}
