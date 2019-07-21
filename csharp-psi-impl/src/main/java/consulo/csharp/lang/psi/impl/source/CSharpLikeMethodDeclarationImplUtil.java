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

package consulo.csharp.lang.psi.impl.source;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import consulo.csharp.lang.psi.impl.source.resolve.ExecuteTargetUtil;
import consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.psi.*;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 09.10.14
 */
public class CSharpLikeMethodDeclarationImplUtil
{
	public static enum ResolveVirtualImplementResult
	{
		CANT_HAVE,
		FOUND,
		NOT_FOUND
	}

	public static boolean isEquivalentTo(@Nonnull PsiElement o1, @Nullable PsiElement o2)
	{
		if(o2 == null)
		{
			return false;
		}
		PsiElement originalElement1 = o1.getOriginalElement();
		PsiElement originalElement2 = o2.getOriginalElement();

		if(o1.getUserData(CSharpResolveUtil.EXTENSION_METHOD_WRAPPER) == originalElement2)
		{
			return true;
		}

		if(originalElement1 == originalElement2)
		{
			return true;
		}

		if(o1 instanceof CSharpConstructorDeclaration && o2 instanceof CSharpTypeDeclaration)
		{
			// default constructor builder
			PsiElement navigationElement = o1.getNavigationElement();
			if(navigationElement == o2)
			{
				return true;
			}
		}

		return false;
	}

	@Nonnull
	@RequiredReadAction
	public static Pair<ResolveVirtualImplementResult, PsiElement> resolveVirtualImplementation(@Nonnull DotNetVirtualImplementOwner owner, @Nonnull PsiElement scope)
	{
		DotNetType typeForImplement = owner.getTypeForImplement();
		if(typeForImplement == null)
		{
			return Pair.create(ResolveVirtualImplementResult.CANT_HAVE, null);
		}

		DotNetTypeRef typeRefForImplement = typeForImplement.toTypeRef();

		DotNetTypeResolveResult typeResolveResult = typeRefForImplement.resolve();

		PsiElement resolvedElement = typeResolveResult.getElement();
		DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();
		if(!(resolvedElement instanceof CSharpTypeDeclaration))
		{
			return Pair.create(ResolveVirtualImplementResult.CANT_HAVE, null);
		}

		for(DotNetNamedElement namedElement : ((CSharpTypeDeclaration) resolvedElement).getMembers())
		{
			namedElement = GenericUnwrapTool.extract(namedElement, genericExtractor);

			if(CSharpElementCompareUtil.isEqual(namedElement, owner, scope))
			{
				return Pair.<ResolveVirtualImplementResult, PsiElement>create(ResolveVirtualImplementResult.FOUND, namedElement);
			}
		}
		return Pair.<ResolveVirtualImplementResult, PsiElement>create(ResolveVirtualImplementResult.NOT_FOUND, null);
	}

	@Nonnull
	@RequiredReadAction
	public static CSharpSimpleParameterInfo[] getParametersInfos(@Nonnull DotNetParameterListOwner parameterListOwner)
	{
		DotNetParameter[] parameters = parameterListOwner.getParameters();

		CSharpSimpleParameterInfo[] parameterInfos = new CSharpSimpleParameterInfo[parameters.length];
		for(int i = 0; i < parameters.length; i++)
		{
			DotNetParameter parameter = parameters[i];
			parameterInfos[i] = new CSharpSimpleParameterInfo(i, parameter, parameter.toTypeRef(true));
		}
		return parameterInfos;
	}

	public static boolean processDeclarations(@Nonnull DotNetLikeMethodDeclaration methodDeclaration,
											  @Nonnull PsiScopeProcessor processor,
											  @Nonnull ResolveState state,
											  PsiElement lastParent,
											  @Nonnull PsiElement place)
	{
		if(ExecuteTargetUtil.canProcess(processor, ExecuteTarget.GENERIC_PARAMETER))
		{
			for(DotNetGenericParameter dotNetGenericParameter : methodDeclaration.getGenericParameters())
			{
				if(!processor.execute(dotNetGenericParameter, state))
				{
					return false;
				}
			}
		}

		if(ExecuteTargetUtil.canProcess(processor, ExecuteTarget.LOCAL_VARIABLE_OR_PARAMETER))
		{
			for(DotNetParameter parameter : methodDeclaration.getParameters())
			{
				if(!processor.execute(parameter, state))
				{
					return false;
				}
			}
		}

		return true;
	}
}
