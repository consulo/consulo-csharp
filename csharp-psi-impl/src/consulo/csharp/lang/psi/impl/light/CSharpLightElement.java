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

package consulo.csharp.lang.psi.impl.light;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProviders;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.util.ObjectUtil;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public abstract class CSharpLightElement<S extends PsiElement> extends LightElement
{
	protected final S myOriginal;

	@Nullable
	private PsiElement myParent;

	protected CSharpLightElement(S original)
	{
		super(original.getManager(), CSharpLanguage.INSTANCE);
		myOriginal = original;
	}

	@SuppressWarnings("unchecked")
	public S withParent(@NotNull PsiElement parent)
	{
		myParent = parent;
		return (S) this;
	}

	@Override
	public PsiElement getParent()
	{
		return ObjectUtil.notNull(myParent, myOriginal.getParent());
	}

	@NotNull
	@Override
	public PsiElement getNavigationElement()
	{
		return myOriginal;
	}

	@Override
	public void navigate(boolean requestFocus)
	{
		if(myOriginal instanceof Navigatable)
		{
			((Navigatable) myOriginal).navigate(requestFocus);
		}
		else
		{
			super.navigate(requestFocus);
		}
	}

	@Override
	public boolean canNavigate()
	{
		if(myOriginal instanceof Navigatable)
		{
			return ((Navigatable) myOriginal).canNavigate();
		}
		else
		{
			return super.canNavigate();
		}
	}

	@Override
	public PsiFile getContainingFile()
	{
		return myOriginal.getContainingFile();
	}

	@Override
	public PsiElement getOriginalElement()
	{
		return myOriginal.getOriginalElement();
	}

	@Override
	public ItemPresentation getPresentation()
	{
		return ItemPresentationProviders.getItemPresentation(this);
	}

	@Override
	public final void accept(@NotNull PsiElementVisitor visitor)
	{
		if(visitor instanceof CSharpElementVisitor)
		{
			accept((CSharpElementVisitor) visitor);
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
