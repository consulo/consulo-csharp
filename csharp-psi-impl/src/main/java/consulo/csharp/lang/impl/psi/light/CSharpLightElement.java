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

package consulo.csharp.lang.impl.psi.light;

import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.language.impl.psi.LightElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFile;
import consulo.navigation.ItemPresentation;
import consulo.navigation.ItemPresentationProvider;
import consulo.navigation.Navigatable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

	@Override
	public boolean isEquivalentTo(@Nullable PsiElement another)
	{
		PsiElement ori1 = another == null ? null : another.getOriginalElement();
		PsiElement ori2 = getOriginalElement();

		if(ori1 != null && ori1.isEquivalentTo(ori2))
		{
			return true;
		}

		return super.isEquivalentTo(another);
	}

	@SuppressWarnings("unchecked")
	public S withParent(@Nonnull PsiElement parent)
	{
		myParent = parent;
		return (S) this;
	}

	@Override
	public PsiElement getParent()
	{
		if(myParent != null)
		{
			return myParent;
		}
		PsiElement parent = myOriginal.getParent();
		if(parent != null)
		{
			return parent;
		}
		return null;
	}

	@Nonnull
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
		return ItemPresentationProvider.getItemPresentation(this);
	}

	@Override
	public final void accept(@Nonnull PsiElementVisitor visitor)
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

	public abstract void accept(@Nonnull CSharpElementVisitor visitor);

	@Override
	public String toString()
	{
		return getClass().getName();
	}
}
