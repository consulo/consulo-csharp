/*
 * Copyright 2013-2026 consulo.io
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

package consulo.csharp.lang.impl.psi.source.resolve.methodResolving;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.impl.psi.CSharpInheritableChecker;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpRefTypeRef;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.util.lang.Pair;
import org.jspecify.annotations.Nullable;

/**
 * Method group conversion (C# spec 10.8): the method is applicable to the delegate's parameter list - with its ref kinds,
 * an in parameter of the delegate takes an in parameter only - and returns what the delegate returns, by reference for a
 * ref return.
 *
 * @author VISTALL
 */
public final class MethodGroupConversion
{
	private MethodGroupConversion()
	{
	}

	/**
	 * @return the weight of the method for the delegate, invalid if the method does not convert to it
	 */
	@RequiredReadAction
	public static MethodResolvePriorityInfo calc(CSharpLambdaResolveResult delegate, DotNetLikeMethodDeclaration method, GlobalSearchScope scope)
	{
		MethodResolvePriorityInfo calc = NCallArgumentBuilder.calc(delegate.getParameterTypeRefs(), method.getParameterTypeRefs(), scope);
		if(!calc.isValidResult())
		{
			return calc;
		}
		if(!returnFits(delegate, method, Pair.create(CSharpCastType.IMPLICIT, scope)))
		{
			return calc.dupNoResult(-4000000);
		}

		CSharpSimpleParameterInfo[] delegateParameters = delegate.getParameterInfos();
		DotNetParameter[] methodParameters = method.getParameters();
		int inByValue = 0;
		for(int i = 0; i < delegateParameters.length && i < methodParameters.length; i++)
		{
			boolean delegateIn = isIn(delegateParameters[i].getElement());
			boolean methodIn = isIn(methodParameters[i]);
			if(delegateIn && !methodIn)
			{
				return calc.dupNoResult(-4000000);
			}
			if(methodIn && !delegateIn)
			{
				inByValue++;
			}
		}
		// a parameter by value is better than an in one for a by value parameter of the delegate
		return inByValue == 0 ? calc : calc.dupWithResult(calc.getWeight() - inByValue);
	}

	@RequiredReadAction
	public static boolean returnFits(CSharpLambdaResolveResult delegate,
									 DotNetLikeMethodDeclaration method,
									 @Nullable Pair<CSharpCastType, GlobalSearchScope> castResolvingInfo)
	{
		if(isRefReturn(delegate) != isRefReturn(method))
		{
			return false;
		}
		DotNetTypeRef delegateReturn = delegate.getReturnTypeRef();
		DotNetTypeRef methodReturn = method.getReturnTypeRef();
		if(methodReturn == DotNetTypeRef.AUTO_TYPE || delegateReturn == DotNetTypeRef.AUTO_TYPE)
		{
			return true;
		}
		// void returns void only
		boolean delegateVoid = DotNetTypeRefUtil.isVmQNameEqual(delegateReturn, DotNetTypes.System.Void);
		boolean methodVoid = DotNetTypeRefUtil.isVmQNameEqual(methodReturn, DotNetTypes.System.Void);
		if(delegateVoid || methodVoid)
		{
			return delegateVoid == methodVoid;
		}
		return CSharpInheritableChecker.isInheritable(unref(delegateReturn), unref(methodReturn), castResolvingInfo).isSuccess();
	}

	/**
	 * {@code ref int M()} - the ref is a modifier of the method, or the return type of a compiled one
	 */
	@RequiredReadAction
	public static boolean isRefReturn(CSharpLambdaResolveResult delegate)
	{
		CSharpMethodDeclaration target = delegate.getTarget();
		return target != null ? isRefReturn(target) : delegate.getReturnTypeRef() instanceof CSharpRefTypeRef;
	}

	@RequiredReadAction
	public static boolean isRefReturn(DotNetLikeMethodDeclaration method)
	{
		if(method.hasModifier(CSharpModifier.REF) || method.getReturnTypeRef() instanceof CSharpRefTypeRef)
		{
			return true;
		}
		// a generic or light wrapper of the declaration
		PsiElement original = method.getOriginalElement();
		return original != method && original instanceof DotNetLikeMethodDeclaration originalMethod && originalMethod.hasModifier(CSharpModifier.REF);
	}

	@RequiredReadAction
	private static boolean isIn(@Nullable PsiElement parameter)
	{
		return parameter instanceof DotNetModifierListOwner owner && owner.hasModifier(CSharpModifier.IN);
	}

	private static DotNetTypeRef unref(DotNetTypeRef typeRef)
	{
		return typeRef instanceof CSharpRefTypeRef refTypeRef ? refTypeRef.getInnerTypeRef() : typeRef;
	}
}
