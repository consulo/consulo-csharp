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

package consulo.csharp.ide.completion.weigher;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementWeigher;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;

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
