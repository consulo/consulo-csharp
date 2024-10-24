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

import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.util.dataholder.Key;
import consulo.util.dataholder.KeyWithDefaultValue;
import jakarta.annotation.Nonnull;

import java.util.EnumSet;

/**
 * @author VISTALL
 * @since 09.10.14
 */
public class ExecuteTargetUtil
{
	public static final Key<EnumSet<ExecuteTarget>> EXECUTE_TARGETS = KeyWithDefaultValue.create("execute.targets", () -> EnumSet.noneOf(ExecuteTarget.class));

	public static boolean isMyElement(@Nonnull PsiScopeProcessor psiScopeProcessor, @Nonnull PsiElement element)
	{
		EnumSet<ExecuteTarget> hint = psiScopeProcessor.getHint(EXECUTE_TARGETS);
		if(hint == null)
		{
			return false;
		}
		for(ExecuteTarget executeTarget : hint)
		{
			if(executeTarget.isMyElement(element))
			{
				return true;
			}
		}
		return false;
	}

	public static <E extends Enum<E>> EnumSet<E> of(E[] c)
	{
		if(c.length == 0)
		{
			throw new IllegalArgumentException("Collection is empty");
		}

		EnumSet<E> result = EnumSet.of(c[0]);
		if(c.length > 1)
		{
			for(int i = 1; i < c.length; i++)
			{
				result.add(c[i]);
			}
		}
		return result;
	}

	public static boolean canProcess(@Nonnull PsiScopeProcessor psiScopeProcessor, @Nonnull ExecuteTarget... executeTargets)
	{
		EnumSet<ExecuteTarget> hint = psiScopeProcessor.getHint(EXECUTE_TARGETS);
		if(hint == null)
		{
			return false;
		}

		for(ExecuteTarget executeTarget : executeTargets)
		{
			if(hint.contains(executeTarget))
			{
				return true;
			}
		}
		return false;
	}
}
