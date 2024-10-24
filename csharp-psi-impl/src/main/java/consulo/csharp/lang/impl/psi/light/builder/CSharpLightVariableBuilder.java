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

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import consulo.util.collection.SmartList;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;

/**
 * @author VISTALL
 * @since 06.02.14
 */
public abstract class CSharpLightVariableBuilder<T extends CSharpLightVariableBuilder<T>> extends CSharpLightNamedElementBuilder<T> implements
		DotNetVariable
{
	private List<CSharpModifier> myModifiers = new SmartList<>();
	private boolean myConstant;
	private DotNetTypeRef myTypeRef;

	public CSharpLightVariableBuilder(Project project)
	{
		super(project);
	}

	public CSharpLightVariableBuilder(PsiElement element)
	{
		super(element);
	}

	@RequiredReadAction
	@Override
	public boolean isConstant()
	{
		return myConstant;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getConstantKeywordElement()
	{
		return null;
	}

	@SuppressWarnings("unchecked")
	public T setConstant()
	{
		myConstant = true;
		return (T) this;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromInitializer)
	{
		return myTypeRef;
	}

	@SuppressWarnings("unchecked")
	public T withTypeRef(DotNetTypeRef typeRef)
	{
		myTypeRef = typeRef;
		return (T) this;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetType getType()
	{
		return null;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetExpression getInitializer()
	{
		return null;
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(@Nonnull DotNetModifier modifier)
	{
		return myModifiers.contains(CSharpModifier.as(modifier));
	}

	public void addModifier(CSharpModifier modifierWithMask)
	{
		myModifiers.add(modifierWithMask);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return null;
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}
}
