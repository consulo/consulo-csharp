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
import consulo.csharp.lang.impl.psi.source.CSharpLikeMethodDeclarationImplUtil;
import consulo.csharp.lang.psi.CSharpCodeBodyProxy;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 17.05.14
 */
@SuppressWarnings("unchecked")
public abstract class CSharpLightLikeMethodDeclarationBuilder<T extends CSharpLightLikeMethodDeclarationBuilder<T>> extends
		CSharpLightNamedElementWiModifierListBuilder<T> implements DotNetLikeMethodDeclaration
{
	private List<DotNetParameter> myParameters = Collections.emptyList();
	private List<DotNetGenericParameter> myGenericParameters = Collections.emptyList();

	private String myParentQName;
	private DotNetTypeRef myReturnType;

	public CSharpLightLikeMethodDeclarationBuilder(Project project)
	{
		super(project);
	}

	@RequiredReadAction
	@Nonnull
	public CSharpSimpleParameterInfo[] getParameterInfos()
	{
		return CSharpLikeMethodDeclarationImplUtil.getParametersInfos(this);
	}

	@Override
	public boolean isEquivalentTo(PsiElement another)
	{
		return CSharpLikeMethodDeclarationImplUtil.isEquivalentTo(this, another);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetType getReturnType()
	{
		return null;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		return myReturnType;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public CSharpCodeBodyProxy getCodeBlock()
	{
		return CSharpCodeBodyProxy.EMPTY;
	}

	@Nullable
	@Override
	public DotNetGenericParameterList getGenericParameterList()
	{
		return null;
	}

	@Nonnull
	@Override
	public DotNetGenericParameter[] getGenericParameters()
	{
		return ContainerUtil.toArray(myGenericParameters, DotNetGenericParameter.ARRAY_FACTORY);
	}

	@Override
	public int getGenericParametersCount()
	{
		return myGenericParameters.size();
	}

	@Nonnull
	@Override
	public DotNetTypeRef[] getParameterTypeRefs()
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

	@Nonnull
	@Override
	public DotNetParameter[] getParameters()
	{
		return ContainerUtil.toArray(myParameters, DotNetParameter.ARRAY_FACTORY);
	}

	@RequiredReadAction
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

	@RequiredReadAction
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

	@Override
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState state, PsiElement lastParent, @Nonnull PsiElement
			place)
	{
		return CSharpLikeMethodDeclarationImplUtil.processDeclarations(this, processor, state, lastParent, place);
	}

	@Nonnull
	public T withReturnType(DotNetTypeRef type)
	{
		myReturnType = type;
		return (T) this;
	}

	@Nonnull
	public T withParentQName(String parentQName)
	{
		myParentQName = parentQName;
		return (T) this;
	}

	public T addGenericParameter(DotNetGenericParameter genericParameter)
	{
		if(myGenericParameters.isEmpty())
		{
			myGenericParameters = new ArrayList<DotNetGenericParameter>(2);
		}

		if(genericParameter instanceof CSharpLightGenericParameterBuilder)
		{
			((CSharpLightGenericParameterBuilder) genericParameter).setIndex(myGenericParameters.size());
		}

		myGenericParameters.add(genericParameter);
		return (T) this;
	}

	@Nonnull
	public T addParameter(DotNetParameter parameter)
	{
		if(myParameters.isEmpty())
		{
			myParameters = new ArrayList<DotNetParameter>(5);
		}

		if(parameter instanceof CSharpLightElementBuilder)
		{
			((CSharpLightElementBuilder) parameter).withParent(this);
		}
		if(parameter instanceof CSharpLightParameterBuilder)
		{
			((CSharpLightParameterBuilder) parameter).setMethod(this);
		}
		myParameters.add(parameter);
		return (T) this;
	}
}
