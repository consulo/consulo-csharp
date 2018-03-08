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
import javax.annotation.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.impl.source.CSharpLikeMethodDeclarationImplUtil;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodResolvePriorityInfo;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodResolver;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public class CSharpElementGroupTypeRef extends DotNetTypeRefWithCachedResult implements CSharpFastImplicitTypeRef
{
	private final CSharpElementGroup<?> myElementGroup;

	public CSharpElementGroupTypeRef(CSharpElementGroup<?> elementGroup)
	{
		super(elementGroup.getProject());
		myElementGroup = elementGroup;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		return DotNetTypeResolveResult.EMPTY;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String toString()
	{
		return myElementGroup.getName();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetTypeRef doMirror(@Nonnull DotNetTypeRef another, PsiElement scope)
	{
		DotNetTypeResolveResult typeResolveResult = another.resolve();
		if(typeResolveResult instanceof CSharpLambdaResolveResult)
		{
			DotNetTypeRef[] parameterTypeRefs = ((CSharpLambdaResolveResult) typeResolveResult).getParameterTypeRefs();

			for(PsiElement psiElement : myElementGroup.getElements())
			{
				if(psiElement instanceof DotNetLikeMethodDeclaration)
				{
					DotNetTypeRef[] methodParameterTypeRef = ((DotNetLikeMethodDeclaration) psiElement).getParameterTypeRefs();

					MethodResolvePriorityInfo calc = MethodResolver.calc(parameterTypeRefs, methodParameterTypeRef, scope);

					if(calc.isValidResult())
					{
						return new CSharpLambdaTypeRef(scope, null, CSharpLikeMethodDeclarationImplUtil.getParametersInfos((DotNetLikeMethodDeclaration) psiElement),
								((DotNetLikeMethodDeclaration) psiElement).getReturnTypeRef());
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean isConversion()
	{
		return false;
	}
}
