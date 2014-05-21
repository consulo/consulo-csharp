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

package org.mustbe.consulo.csharp.lang.psi.impl.light;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.light.LightElement;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public abstract class CSharpLightElement<S extends PsiElement> extends LightElement
{
	protected final S myOriginal;

	protected CSharpLightElement(S original)
	{
		super(original.getManager(), CSharpLanguage.INSTANCE);
		myOriginal = original;
	}

	@Override
	public PsiElement getParent()
	{
		return myOriginal.getParent();
	}

	@NotNull
	@Override
	public PsiElement getNavigationElement()
	{
		return myOriginal;
	}

	@Override
	public PsiFile getContainingFile()
	{
		return myOriginal.getContainingFile();
	}

	@Override
	public PsiElement getOriginalElement()
	{
		return myOriginal;
	}

	@Override
	public void accept(@NotNull PsiElementVisitor visitor)
	{
		if(visitor instanceof CSharpElementVisitor)
		{
			accept((CSharpElementVisitor)visitor);
		}
		else
		{
			super.accept(visitor);
		}
	}

	public abstract void accept(@NotNull CSharpElementVisitor visitor);

	@Override
	public String toString()
	{
		return getClass().getName();
	}
}
