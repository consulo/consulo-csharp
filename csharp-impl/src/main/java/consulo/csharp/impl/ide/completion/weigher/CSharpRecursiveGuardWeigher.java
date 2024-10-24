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

package consulo.csharp.impl.ide.completion.weigher;

import java.util.Set;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import consulo.language.editor.completion.lookup.LookupElementWeigher;
import consulo.language.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.editor.completion.lookup.LookupElement;

/**
 * @author VISTALL
 * @since 18.04.2016
 */
public class CSharpRecursiveGuardWeigher extends LookupElementWeigher
{
	private Set<PsiElement> myElementSet;

	@RequiredReadAction
	public CSharpRecursiveGuardWeigher(Set<PsiElement> elementSet)
	{
		super("csharpRecursiveWeigher");
		myElementSet = elementSet;
	}

	@Nullable
	@Override
	public Integer weigh(@Nonnull LookupElement element)
	{
		PsiElement psiElement = element.getPsiElement();
		if(psiElement == null)
		{
			return 0;
		}

		for(PsiElement e : myElementSet)
		{
			if(psiElement.isEquivalentTo(e))
			{
				return Integer.MIN_VALUE;
			}
		}
		return 0;
	}
}
