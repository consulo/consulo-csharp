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

import javax.annotation.Nonnull;

import com.intellij.openapi.project.Project;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetGenericWrapperTypeRef;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpGenericWrapperTypeRef extends DotNetTypeRefWithCachedResult implements DotNetGenericWrapperTypeRef
{
	private final DotNetTypeRef myInnerTypeRef;
	private final DotNetTypeRef[] myArguments;

	public CSharpGenericWrapperTypeRef(Project project, @Nonnull DotNetTypeRef innerTypeRef, @Nonnull DotNetTypeRef... rArguments)
	{
		super(project);
		myInnerTypeRef = innerTypeRef;
		myArguments = rArguments;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(getInnerTypeRef().toString());
		builder.append("<");
		for(int i = 0; i < getArgumentTypeRefs().length; i++)
		{
			if(i != 0)
			{
				builder.append(", ");
			}
			DotNetTypeRef argument = getArgumentTypeRefs()[i];
			builder.append(argument.toString());
		}
		builder.append(">");
		return builder.toString();
	}

	@RequiredReadAction
	@Nonnull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		DotNetTypeResolveResult typeResolveResult = getInnerTypeRef().resolve();
		PsiElement element = typeResolveResult.getElement();

		if(typeResolveResult instanceof CSharpLambdaResolveResult)
		{
			CSharpMethodDeclaration target = ((CSharpLambdaResolveResult) typeResolveResult).getTarget();
			if(target == null)
			{
				return new CSharpUserTypeRef.Result<PsiElement>(element, getGenericExtractor(element));
			}
			return new CSharpUserTypeRef.LambdaResult(target, target, getGenericExtractor(target));
		}
		return new CSharpUserTypeRef.Result<PsiElement>(element, getGenericExtractor(element));
	}

	public DotNetGenericExtractor getGenericExtractor(PsiElement resolved)
	{
		if(!(resolved instanceof DotNetGenericParameterListOwner))
		{
			return DotNetGenericExtractor.EMPTY;
		}

		DotNetGenericParameter[] genericParameters = ((DotNetGenericParameterListOwner) resolved).getGenericParameters();
		if(genericParameters.length != getArgumentTypeRefs().length)
		{
			return DotNetGenericExtractor.EMPTY;
		}
		return CSharpGenericExtractor.create(genericParameters, getArgumentTypeRefs());
	}

	@Override
	@Nonnull
	public DotNetTypeRef getInnerTypeRef()
	{
		return myInnerTypeRef;
	}

	@Override
	@Nonnull
	public DotNetTypeRef[] getArgumentTypeRefs()
	{
		return myArguments;
	}
}
