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
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 17.05.14
 */
@SuppressWarnings("unchecked")
public abstract class CSharpLightLikeMethodDeclarationBuilder<T extends CSharpLightLikeMethodDeclarationBuilder<T>> extends
		CSharpLightNamedElementBuilder<T> implements DotNetLikeMethodDeclaration
{
	private List<DotNetModifier> myModifiers = new ArrayList<DotNetModifier>();
	private List<DotNetParameter> myParameters = new ArrayList<DotNetParameter>();
	private String myParentQName;
	private DotNetTypeRef myReturnType;
	private CSharpLightModifierListBuilder myModifierListBuilder;

	public CSharpLightLikeMethodDeclarationBuilder(Project project)
	{
		super(project);
	}

	@Nullable
	@Override
	public DotNetType getReturnType()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		return myReturnType;
	}

	@Nullable
	@Override
	public PsiElement getCodeBlock()
	{
		return null;
	}

	@Nullable
	@Override
	public DotNetGenericParameterList getGenericParameterList()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetGenericParameter[] getGenericParameters()
	{
		return new DotNetGenericParameter[0];
	}

	@Override
	public int getGenericParametersCount()
	{
		return 0;
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

	@NotNull
	@Override
	public DotNetTypeRef[] getParameterTypesForRuntime()
	{
		DotNetParameter[] parameters = getParameters();
		DotNetTypeRef[] typeRefs = new DotNetTypeRef[parameters.length];
		for(int i = 0; i < parameters.length; i++)
		{
			DotNetParameter parameter = parameters[i];
			typeRefs[i] = parameter.toTypeRef(false);
		}
		return typeRefs;
	}

	@Nullable
	@Override
	public DotNetParameterList getParameterList()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetParameter[] getParameters()
	{
		return ContainerUtil.toArray(myParameters, DotNetParameter.ARRAY_FACTORY);
	}

	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		PsiElement parent = getParent();
		if(parent instanceof DotNetQualifiedElement)
		{
			return ((DotNetQualifiedElement) parent).getPresentableQName();
		}
		return myParentQName;
	}

	@Nullable
	@Override
	public String getPresentableQName()
	{
		String parentQName = getPresentableParentQName();
		if(StringUtil.isEmpty(parentQName))
		{
			return getName();
		}
		return parentQName + "." + getName();
	}

@NotNull
	public T withReturnType(DotNetTypeRef type)
	{
		myReturnType = type;
		return (T) this;
	}

	@NotNull
	public T withParentQName(String parentQName)
	{
		myParentQName = parentQName;
		return (T) this;
	}

	@NotNull
	public T addModifier(DotNetModifier modifierWithMask)
	{
		myModifiers.add(modifierWithMask);
		return (T) this;
	}

	@NotNull
	public T addParameter(DotNetParameter parameter)
	{
		if(parameter instanceof CSharpLightElementBuilder)
		{
			((CSharpLightElementBuilder) parameter).withParent(this);
		}
		myParameters.add(parameter);
		return (T) this;
	}
}
