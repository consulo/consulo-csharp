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

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nonnull;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.project.Project;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 11.08.14
 */
@SuppressWarnings("unchecked")
public abstract class CSharpLightNamedElementWiModifierListBuilder<T extends CSharpLightNamedElementWiModifierListBuilder<T>> extends CSharpLightNamedElementBuilder<T> implements
		DotNetModifierListOwner
{
	private CSharpLightModifierListBuilder myModifierListBuilder;
	private List<CSharpModifier> myModifiers = new ArrayList<CSharpModifier>();

	public CSharpLightNamedElementWiModifierListBuilder(Project project)
	{
		super(project);
	}

	public CSharpLightNamedElementWiModifierListBuilder(PsiElement element)
	{
		super(element);
	}

	@Nonnull
	public T addModifier(DotNetModifier modifier)
	{
		myModifiers.add(CSharpModifier.as(modifier));
		return (T) this;
	}

	@Nonnull
	public T removeModifier(@Nonnull DotNetModifier modifier)
	{
		myModifiers.remove(CSharpModifier.as(modifier));
		return (T) this;
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(@Nonnull DotNetModifier modifier)
	{
		return myModifiers.contains(CSharpModifier.as(modifier));
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		if(myModifierListBuilder == null)
		{
			myModifierListBuilder = new CSharpLightModifierListBuilder(myModifiers, getManager(), getLanguage());
		}
		return myModifierListBuilder;
	}
}
