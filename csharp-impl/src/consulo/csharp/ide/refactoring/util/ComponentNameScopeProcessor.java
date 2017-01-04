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

package consulo.csharp.ide.refactoring.util;

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.BaseScopeProcessor;

public class ComponentNameScopeProcessor extends BaseScopeProcessor
{
	private final Set<PsiNamedElement> myResult;
	@Nullable
	private PsiElement myToSkip;

	public ComponentNameScopeProcessor(@NotNull Set<PsiNamedElement> result, @Nullable PsiElement toSkip)
	{
		myResult = result;
		myToSkip = toSkip;
	}

	@Override
	public boolean execute(@NotNull PsiElement element, ResolveState state)
	{
		if(myToSkip == element)
		{
			return true;
		}
		if(element instanceof PsiNamedElement)
		{
			myResult.add((PsiNamedElement) element);
		}
		return true;
	}
}
