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

package consulo.csharp.lang.impl.psi.light.builder;

import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.language.impl.psi.LightElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.project.Project;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 06.02.14
 */
public abstract class CSharpLightElementBuilder<T extends CSharpLightElementBuilder<T>> extends LightElement
{
	private PsiElement myParent;

	private Object myHashAndEqualObject = this;

	public CSharpLightElementBuilder(PsiElement element)
	{
		super(element.getManager(), CSharpLanguage.INSTANCE);
	}

	public CSharpLightElementBuilder(Project project)
	{
		super(PsiManager.getInstance(project), CSharpLanguage.INSTANCE);
	}

	@SuppressWarnings("unchecked")
	public T withParent(PsiElement parent)
	{
		myParent = parent;
		return (T) this;
	}

	@Override
	public void accept(@Nonnull PsiElementVisitor visitor)
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
	public PsiFile getContainingFile()
	{
		return myParent == null ? null : myParent.getContainingFile();
	}

	@SuppressWarnings("unchecked")
	public T withHashAndEqualObject(@Nonnull Object hashAndEqualObject)
	{
		myHashAndEqualObject = hashAndEqualObject;
		return (T) this;
	}

	@Override
	public PsiElement getParent()
	{
		return myParent;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	public boolean equals(Object o)
	{
		if(myHashAndEqualObject == this)
		{
			return super.equals(o);
		}

		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}
		CSharpLightElementBuilder<?> that = (CSharpLightElementBuilder<?>) o;
		return Objects.equals(myHashAndEqualObject, that.myHashAndEqualObject);
	}

	@Override
	public int hashCode()
	{
		return myHashAndEqualObject == this ? super.hashCode() : myHashAndEqualObject.hashCode();
	}
}
