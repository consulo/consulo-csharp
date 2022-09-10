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

package consulo.csharp.lang.impl.psi.source;

import consulo.csharp.lang.impl.psi.CSharpMacroElementVisitor;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElementVisitor;
import consulo.navigation.ItemPresentation;
import consulo.navigation.ItemPresentationProvider;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 24.01.14
 */
public abstract class CSharpPreprocessorElementImpl extends AdvancedCompositePsiElement
{
	public CSharpPreprocessorElementImpl(IElementType type)
	{
		super(type);
	}

	@Override
	public ItemPresentation getPresentation()
	{
		return ItemPresentationProvider.getItemPresentation(this);
	}

	@Override
	public void accept(@Nonnull PsiElementVisitor visitor)
	{
		if(visitor instanceof CSharpMacroElementVisitor)
		{
			accept((CSharpMacroElementVisitor)visitor);
		}
		else
		{
			super.accept(visitor);
		}
	}

	public abstract void accept(@Nonnull CSharpMacroElementVisitor visitor);
}

