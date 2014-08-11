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

package org.mustbe.consulo.csharp.lang.psi.impl.light.builder;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 11.08.14
 */
@SuppressWarnings("unchecked")
public abstract class CSharpLightNamedElementWiModifierListBuilder<T extends CSharpLightNamedElementWiModifierListBuilder<T>> extends
		CSharpLightNamedElementBuilder<T> implements DotNetModifierListOwner
{
	private CSharpLightModifierListBuilder myModifierListBuilder;
	private List<DotNetModifier> myModifiers = new ArrayList<DotNetModifier>();

	public CSharpLightNamedElementWiModifierListBuilder(Project project)
	{
		super(project);
	}

	public CSharpLightNamedElementWiModifierListBuilder(PsiElement element)
	{
		super(element);
	}

	@NotNull
	public T addModifier(DotNetModifier modifierWithMask)
	{
		myModifiers.add(modifierWithMask);
		return (T) this;
	}

	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		return myModifiers.contains(CSharpModifier.as(modifier));
	}

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
