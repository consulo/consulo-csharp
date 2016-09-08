/*
 * Copyright 2013-2016 must-be.org
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementWeigher;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 18.04.2016
 */
public class CSharpRecursiveGuardWeigher extends LookupElementWeigher
{
	enum Set
	{
		normal,
		recursive
	}

	private PsiElement myTarget;

	public CSharpRecursiveGuardWeigher(CSharpCallArgumentListOwner argumentListOwner)
	{
		super("csharpRecursiveWeigher");
		if(argumentListOwner instanceof CSharpMethodCallExpressionImpl)
		{
			myTarget = argumentListOwner.resolveToCallable();
		}
	}

	@Nullable
	@Override
	public Set weigh(@NotNull LookupElement element)
	{
		PsiElement psiElement = element.getPsiElement();
		if(psiElement == null)
		{
			return Set.normal;
		}

		if(psiElement.isEquivalentTo(myTarget))
		{
			return Set.recursive;
		}
		return Set.normal;
	}
}
