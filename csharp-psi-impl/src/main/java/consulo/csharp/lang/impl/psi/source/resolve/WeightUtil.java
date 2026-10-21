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

package consulo.csharp.lang.impl.psi.source.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpDynamicTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.dotnet.DotNetTypes;
import java.util.ArrayList;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.arguments.NCallArgument;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.CSharpCastType;
import consulo.application.progress.ProgressManager;
import consulo.application.util.function.Processor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.util.collection.ContainerUtil;

import java.util.Comparator;
import java.util.List;

/**
 * @author VISTALL
 * @since 24.10.14
 */
public class WeightUtil
{
	public static final int MAX_WEIGHT = Integer.MIN_VALUE;

	public static final Comparator<MethodResolveResult> ourComparator = (o1, o2) -> o2.getCalcResult().getWeight() - o1.getCalcResult().getWeight();

	@RequiredReadAction
	public static void sortAndProcess(List<MethodResolveResult> list, Processor<ResolveResult> processor, PsiElement place)
	{
		if(list.isEmpty())
		{
			return;
		}

		ContainerUtil.sort(list, ourComparator);

		moveBetterToFront(list, place);

		for(MethodResolveResult methodResolveResult : list)
		{
			ProgressManager.checkCanceled();

			methodResolveResult.setAssignable(place);

			if(!processor.process(methodResolveResult))
			{
				return;
			}
		}
	}

	/**
	 * Among the valid candidates of the best weight, the better function member goes first (C# spec 12.6.4.3): its
	 * parameter types are better conversion targets for some arguments and worse for none.
	 */
	@RequiredReadAction
	private static void moveBetterToFront(List<MethodResolveResult> sortedList, PsiElement place)
	{
		if(sortedList.size() < 2 || !sortedList.get(0).isValidResult())
		{
			return;
		}

		int bestWeight = sortedList.get(0).getCalcResult().getWeight();
		List<MethodResolveResult> tied = new ArrayList<>();
		for(MethodResolveResult result : sortedList)
		{
			if(!result.isValidResult() || result.getCalcResult().getWeight() != bestWeight)
			{
				break;
			}
			tied.add(result);
		}
		if(tied.size() < 2)
		{
			return;
		}

		GlobalSearchScope scope = place.getResolveScope();
		for(MethodResolveResult candidate : tied)
		{
			boolean betterThanAll = true;
			for(MethodResolveResult other : tied)
			{
				if(other != candidate && !isBetter(candidate, other, scope))
				{
					betterThanAll = false;
					break;
				}
			}
			if(betterThanAll)
			{
				sortedList.remove(candidate);
				sortedList.add(0, candidate);
				return;
			}
		}
	}

	@RequiredReadAction
	private static boolean isBetter(MethodResolveResult first, MethodResolveResult second, GlobalSearchScope scope)
	{
		List<NCallArgument> firstArguments = first.getCalcResult().getArguments();
		List<NCallArgument> secondArguments = second.getCalcResult().getArguments();
		if(firstArguments.size() != secondArguments.size())
		{
			return false;
		}

		boolean better = false;
		for(int i = 0; i < firstArguments.size(); i++)
		{
			DotNetTypeRef firstType = firstArguments.get(i).getParameterTypeRef();
			DotNetTypeRef secondType = secondArguments.get(i).getParameterTypeRef();
			if(firstType == null || secondType == null)
			{
				continue;
			}
			if(isBetterConversionTarget(secondType, firstType, scope))
			{
				return false;
			}
			better |= isBetterConversionTarget(firstType, secondType, scope);
		}
		return better;
	}

	/**
	 * T1 is better than T2 if T1 converts to T2 implicitly and not the other way; delegates compare by their return types
	 * (a lambda converts to both).
	 */
	@RequiredReadAction
	private static boolean isBetterConversionTarget(DotNetTypeRef first, DotNetTypeRef second, GlobalSearchScope scope)
	{
		// dynamic is object here - F(C) is better than F(dynamic) for F(null)
		first = dynamicAsObject(first, scope);
		second = dynamicAsObject(second, scope);
		if(CSharpTypeUtil.isTypeEqual(first, second))
		{
			return false;
		}
		if(first.resolve() instanceof CSharpLambdaResolveResult firstLambda && second.resolve() instanceof CSharpLambdaResolveResult secondLambda)
		{
			DotNetTypeRef firstReturn = firstLambda.getReturnTypeRef();
			DotNetTypeRef secondReturn = secondLambda.getReturnTypeRef();
			boolean firstVoid = DotNetTypeRefUtil.isVmQNameEqual(firstReturn, DotNetTypes.System.Void);
			boolean secondVoid = DotNetTypeRefUtil.isVmQNameEqual(secondReturn, DotNetTypes.System.Void);
			// a delegate returning a value is better than a void one
			if(firstVoid || secondVoid)
			{
				return !firstVoid;
			}
			return isBetterConversionTarget(firstReturn, secondReturn, scope);
		}
		return CSharpTypeUtil.isInheritable(second, first, CSharpCastType.IMPLICIT, scope).isSuccess()
				&& !CSharpTypeUtil.isInheritable(first, second, CSharpCastType.IMPLICIT, scope).isSuccess();
	}

	private static DotNetTypeRef dynamicAsObject(DotNetTypeRef typeRef, GlobalSearchScope scope)
	{
		return typeRef instanceof CSharpDynamicTypeRef ? new CSharpTypeRefByQName(typeRef.getProject(), scope, DotNetTypes.System.Object) : typeRef;
	}
}
