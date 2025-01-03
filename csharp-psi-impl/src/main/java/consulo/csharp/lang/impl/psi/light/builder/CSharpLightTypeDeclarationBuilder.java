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
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.source.CSharpTypeDeclarationImplUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpGenericConstraintList;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.navigation.Navigatable;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.SmartList;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;

/**
 * @author VISTALL
 * @since 08.05.14
 */
public class CSharpLightTypeDeclarationBuilder extends CSharpLightNamedElementBuilder<CSharpLightTypeDeclarationBuilder> implements CSharpTypeDeclaration
{
	public enum Type
	{
		DEFAULT,
		STRUCT,
		ENUM,
		INTERFACE
	}

	private List<DotNetQualifiedElement> myMembers = new SmartList<>();
	private List<DotNetModifier> myModifiers = new SmartList<>();
	private List<DotNetTypeRef> myExtendTypes = new SmartList<>();
	private List<DotNetGenericParameter> myGenericParameters = new SmartList<>();
	private Type myType = Type.DEFAULT;
	private String myParentQName;

	private DotNetTypeRef myEnumConstantTypeRef;

	@RequiredReadAction
	public CSharpLightTypeDeclarationBuilder(Project project, GlobalSearchScope scope)
	{
		super(project);
		myEnumConstantTypeRef = new CSharpTypeRefByQName(project, scope, DotNetTypes.System.Int32);
	}

	@Override
	public void navigate(boolean requestFocus)
	{
		PsiElement navigationElement = getNavigationElement();
		if(navigationElement instanceof Navigatable)
		{
			((Navigatable) navigationElement).navigate(requestFocus);
			return;
		}

		super.navigate(requestFocus);
	}

	@RequiredReadAction
	@Override
	public PsiElement getLeftBrace()
	{
		return null;
	}

	@RequiredReadAction
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

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef[] getExtendTypeRefs()
	{
		return ContainerUtil.toArray(myExtendTypes, DotNetTypeRef.ARRAY_FACTORY);
	}

	@RequiredReadAction
	@Override
	public boolean isInheritor(@Nonnull String other, boolean deep)
	{
		return DotNetInheritUtil.isInheritor(this, other, deep);
	}

	@Nonnull
	@RequiredReadAction
	@Override
	public DotNetTypeRef getTypeRefForEnumConstants()
	{
		return myEnumConstantTypeRef;
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
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitTypeDeclaration(this);
	}

	@Override
	public int getGenericParametersCount()
	{
		return getGenericParameters().length;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetQualifiedElement[] getMembers()
	{
		return ContainerUtil.toArray(myMembers, DotNetQualifiedElement.ARRAY_FACTORY);
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
		return null;
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
	@Override
	public String getVmQName()
	{
		String presentableQName = getPresentableQName();
		int genericParametersCount = getGenericParametersCount();
		if(genericParametersCount == 0)
		{
			return presentableQName;
		}
		return presentableQName + DotNetTypeDeclarationUtil.GENERIC_MARKER_IN_NAME + genericParametersCount;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getVmName()
	{
		return DotNetTypeDeclarationUtil.getVmName(this);
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

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}

	@Nullable
	@Override
	public CSharpGenericConstraintList getGenericConstraintList()
	{
		return null;
	}

	@Nonnull
	@Override
	public CSharpGenericConstraint[] getGenericConstraints()
	{
		return CSharpGenericConstraint.EMPTY_ARRAY;
	}

	@Override
	public boolean isEquivalentTo(PsiElement another)
	{
		return CSharpTypeDeclarationImplUtil.isEquivalentTo(this, another);
	}

	@Nonnull
	public CSharpLightTypeDeclarationBuilder withParentQName(String parentQName)
	{
		myParentQName = parentQName;
		return this;
	}

	@Nonnull
	public CSharpLightTypeDeclarationBuilder addModifier(DotNetModifier modifier)
	{
		myModifiers.add(CSharpModifier.as(modifier));
		return this;
	}

	@Nonnull
	public CSharpLightTypeDeclarationBuilder addExtendType(DotNetTypeRef typeRef)
	{
		myExtendTypes.add(typeRef);
		return this;
	}

	@Nonnull
	public CSharpLightTypeDeclarationBuilder addGenericParameter(DotNetGenericParameter genericParameter)
	{
		if(genericParameter instanceof CSharpLightGenericParameterBuilder)
		{
			((CSharpLightGenericParameterBuilder) genericParameter).setIndex(myGenericParameters.size());
		}

		myGenericParameters.add(genericParameter);
		return this;
	}

	@Nonnull
	public CSharpLightTypeDeclarationBuilder withType(Type type)
	{
		myType = type;
		return this;
	}

	@Nonnull
	public CSharpLightTypeDeclarationBuilder addMember(@Nonnull DotNetQualifiedElement element)
	{
		if(element instanceof CSharpLightElementBuilder)
		{
			((CSharpLightElementBuilder) element).withParent(this);
		}
		myMembers.add(element);
		return this;
	}
}
