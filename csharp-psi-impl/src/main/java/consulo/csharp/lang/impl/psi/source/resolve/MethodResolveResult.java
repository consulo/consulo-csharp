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

import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.MethodResolvePriorityInfo;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.util.collection.ArrayFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public class MethodResolveResult extends CSharpResolveResult
{
	public static final MethodResolveResult[] EMPTY_ARRAY = new MethodResolveResult[0];

	public static ArrayFactory<MethodResolveResult> ARRAY_FACTORY = count -> count == 0 ? EMPTY_ARRAY : new MethodResolveResult[count];

	@Nonnull
	public static MethodResolveResult createResult(@Nonnull MethodResolvePriorityInfo calcResult, @Nullable PsiElement element, @Nullable ResolveResult resolveResult)
	{
		PsiElement providerElement = element == null ? null : element.getUserData(FORCE_PROVIDER_ELEMENT);
		if(providerElement == null && resolveResult instanceof CSharpResolveResult)
		{
			providerElement = ((CSharpResolveResult) resolveResult).getProviderElement();
		}
		MethodResolveResult methodResolveResult = new MethodResolveResult(element, calcResult);
		methodResolveResult.setProvider(providerElement);
		if(resolveResult instanceof CSharpUndefinedResolveResult)
		{
			methodResolveResult.myUnknown = true;
		}
		return methodResolveResult;
	}

	@Nonnull
	private final MethodResolvePriorityInfo myCalcResult;

	private boolean myUnknown;

	private MethodResolveResult(@Nullable PsiElement element, @Nonnull MethodResolvePriorityInfo calcResult)
	{
		super(element, calcResult.isValidResult());
		myCalcResult = calcResult;
	}

	public boolean isUnknown()
	{
		return myUnknown;
	}

	@Nonnull
	public MethodResolvePriorityInfo getCalcResult()
	{
		return myCalcResult;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}
		if(!super.equals(o))
		{
			return false;
		}

		MethodResolveResult that = (MethodResolveResult) o;

		if(!myCalcResult.equals(that.myCalcResult))
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + myCalcResult.hashCode();
		return result;
	}
}
