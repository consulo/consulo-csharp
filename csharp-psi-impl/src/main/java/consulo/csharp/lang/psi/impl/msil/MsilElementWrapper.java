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

package consulo.csharp.lang.psi.impl.msil;

import com.intellij.openapi.util.Ref;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.util.Consumer;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.psi.CSharpElementCompareUtil;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.light.CSharpLightElement;
import consulo.msil.representation.MsilRepresentationNavigateUtil;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 02.06.14
 */
public abstract class MsilElementWrapper<T extends PsiElement> extends CSharpLightElement<T>
{
	private final PsiElement myParent;

	public MsilElementWrapper(@Nullable PsiElement parent, T msilElement)
	{
		super(msilElement);
		myParent = parent;
	}

	@Override
	public boolean isPhysical()
	{
		return true;
	}

	@Override
	public PsiElement getParent()
	{
		return myParent;
	}

	@Override
	public PsiFile getContainingFile()
	{
		return null;
	}

	@Override
	public boolean canNavigate()
	{
		return true;
	}

	@Nullable
	protected Class<? extends PsiElement> getNavigationElementClass()
	{
		return null;
	}

	protected boolean isEquivalentTo(PsiElement o1, PsiElement o2)
	{
		return CSharpElementCompareUtil.isEqual(o1, o2, CSharpElementCompareUtil.CHECK_RETURN_TYPE | CSharpElementCompareUtil.CHECK_VIRTUAL_IMPL_TYPE, myOriginal);
	}

	@Override
	@RequiredUIAccess
	public void navigate(boolean requestFocus)
	{
		final Class<? extends PsiElement> navigationElementClass = getNavigationElementClass();

		Consumer<PsiFile> consumer = navigationElementClass == null ? MsilRepresentationNavigateUtil.DEFAULT_NAVIGATOR : new Consumer<PsiFile>()
		{
			@Override
			public void consume(PsiFile file)
			{
				final Ref<Navigatable> navigatableRef = Ref.create();
				file.accept(new PsiRecursiveElementWalkingVisitor()
				{
					@Override
					@RequiredReadAction
					public void visitElement(PsiElement element)
					{
						MsilElementWrapper<T> msilWrapper = MsilElementWrapper.this;
						if(navigationElementClass.isAssignableFrom(element.getClass()) && isEquivalentTo(element, msilWrapper))
						{
							PsiElement elementParent = element.getParent();
							PsiElement wrapperParent = msilWrapper.getParent();
							// check if parent type is equal to self type
							if(elementParent instanceof CSharpTypeDeclaration && wrapperParent instanceof CSharpTypeDeclaration)
							{
								if(!CSharpElementCompareUtil.isEqual(elementParent, wrapperParent, myOriginal))
								{
									return;
								}
							}
							navigatableRef.set((Navigatable) element);
							stopWalking();
							return;
						}
						super.visitElement(element);
					}
				});

				Navigatable navigatable = navigatableRef.get();
				if(navigatable != null)
				{
					navigatable.navigate(true);
				}

				file.navigate(true);
			}
		};

		MsilRepresentationNavigateUtil.navigateToRepresentation(myOriginal, CSharpFileType.INSTANCE, consumer);
	}

	@Nonnull
	@Override
	public PsiElement getNavigationElement()
	{
		return this;
	}
}
