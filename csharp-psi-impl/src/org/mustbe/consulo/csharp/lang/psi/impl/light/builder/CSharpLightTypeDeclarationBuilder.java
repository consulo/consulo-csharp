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
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraint;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintList;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeDeclarationImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import org.mustbe.consulo.dotnet.psi.DotNetConstructorDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclarationUtil;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 08.05.14
 */
public class CSharpLightTypeDeclarationBuilder extends CSharpLightNamedElementBuilder<CSharpLightTypeDeclarationBuilder> implements
		CSharpTypeDeclaration
{
	public enum Type
	{
		DEFAULT,
		STRUCT,
		ENUM,
		INTERFACE
	}

	private List<DotNetQualifiedElement> myMembers = new ArrayList<DotNetQualifiedElement>();
	private List<DotNetModifier> myModifiers = new ArrayList<DotNetModifier>();
	private Type myType = Type.DEFAULT;
	private String myParentQName;

	public CSharpLightTypeDeclarationBuilder(Project project)
	{
		super(project);
	}

	public CSharpLightTypeDeclarationBuilder(PsiElement element)
	{
		super(element);
	}

	@Override
	public boolean hasExtensions()
	{
		for(DotNetQualifiedElement qualifiedElement : getMembers())
		{
			if(CSharpMethodImplUtil.isExtensionMethod(qualifiedElement))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public PsiElement getLeftBrace()
	{
		return null;
	}

	@Override
	public PsiElement getRightBrace()
	{
		return null;
	}

	@Override
	public boolean isInterface()
	{
		return myType == Type.INTERFACE;
	}

	@Override
	public boolean isStruct()
	{
		return myType == Type.STRUCT;
	}

	@Override
	public boolean isEnum()
	{
		return myType == Type.ENUM;
	}

	@Override
	public boolean isNested()
	{
		return false;
	}

	@Nullable
	@Override
	public DotNetTypeList getExtendList()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetTypeRef[] getExtendTypeRefs()
	{
		return new DotNetTypeRef[0];
	}

	@Override
	public boolean isInheritor(@NotNull DotNetTypeDeclaration other, boolean deep)
	{
		return false;
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
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitTypeDeclaration(this);
	}

	@Override
	public int getGenericParametersCount()
	{
		return 0;
	}

	@NotNull
	@Override
	public DotNetQualifiedElement[] getMembers()
	{
		return ContainerUtil.toArray(myMembers, DotNetQualifiedElement.ARRAY_FACTORY);
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
		return null;
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

	@Override
	public String getVmQName()
	{
		return DotNetTypeDeclarationUtil.getVmQName(this);
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

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}

	@Override
	public void processConstructors(@NotNull Processor<DotNetConstructorDeclaration> processor)
	{
		CSharpTypeDeclarationImplUtil.processConstructors(this, processor);
	}

	@Nullable
	@Override
	public CSharpGenericConstraintList getGenericConstraintList()
	{
		return null;
	}

	@NotNull
	@Override
	public CSharpGenericConstraint[] getGenericConstraints()
	{
		return CSharpGenericConstraint.EMPTY_ARRAY;
	}

	public CSharpLightTypeDeclarationBuilder withParentQName(String parentQName)
	{
		myParentQName = parentQName;
		return this;
	}

	public CSharpLightTypeDeclarationBuilder addModifier(DotNetModifier modifierWithMask)
	{
		myModifiers.add(modifierWithMask);
		return this;
	}

	public CSharpLightTypeDeclarationBuilder withType(Type type)
	{
		myType = type;
		return this;
	}

	public CSharpLightTypeDeclarationBuilder addMember(@NotNull DotNetQualifiedElement element)
	{
		if(element instanceof CSharpLightElementBuilder)
		{
			((CSharpLightElementBuilder) element).withParent(this);
		}
		myMembers.add(element);
		return this;
	}
}
