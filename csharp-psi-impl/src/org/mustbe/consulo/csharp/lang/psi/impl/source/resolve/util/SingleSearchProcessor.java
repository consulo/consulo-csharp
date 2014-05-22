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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.MsilWrapperScopeProcessor;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;

/**
 * @author VISTALL
 * @since 18.05.14
 */
public abstract class SingleSearchProcessor<T> extends MsilWrapperScopeProcessor implements PsiScopeProcessor
{
	private T myElement;

	public SingleSearchProcessor(@Nullable final String name)
	{
		if(name != null)
		{
			putUserData(CSharpResolveUtil.CONDITION_KEY, new Condition<PsiElement>()
			{
				@Override
				public boolean value(PsiElement element)
				{
					return element instanceof PsiNamedElement && Comparing.equal(((PsiNamedElement) element).getName(), name);
				}
			});
		}
	}

	public SingleSearchProcessor(@Nullable Condition<PsiElement> elementCondition)
	{
		if(elementCondition != null)
		{
			putUserData(CSharpResolveUtil.CONDITION_KEY, elementCondition);
		}
	}

	public abstract T isValidElement(@NotNull PsiElement element);

	@Nullable
	public T get()
	{
		return myElement;
	}

	@Override
	public boolean executeImpl(@NotNull PsiElement element, ResolveState state)
	{
		T validElement = isValidElement(element);
		if(validElement != null)
		{
			myElement = validElement;
			return false;
		}
		return true;
	}
}
