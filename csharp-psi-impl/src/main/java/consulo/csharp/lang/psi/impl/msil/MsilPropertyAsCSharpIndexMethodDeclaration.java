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

import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.NullableLazyValue;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpCodeBodyProxy;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.csharp.lang.psi.impl.light.CSharpLightAttributeBuilder;
import consulo.csharp.lang.psi.impl.msil.typeParsing.SomeType;
import consulo.csharp.lang.psi.impl.msil.typeParsing.SomeTypeParser;
import consulo.csharp.lang.psi.impl.source.CSharpLikeMethodDeclarationImplUtil;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.*;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.msil.lang.psi.MsilMethodEntry;
import consulo.msil.lang.psi.MsilPropertyEntry;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author VISTALL
 * @since 28.05.14
 */
public class MsilPropertyAsCSharpIndexMethodDeclaration extends MsilElementWrapper<MsilPropertyEntry> implements CSharpIndexMethodDeclaration
{
	private final MsilModifierListToCSharpModifierList myModifierList;

	private final DotNetParameter[] myParameters;

	private final DotNetXAccessor[] myAccessors;

	private final NullableLazyValue<DotNetType> myTypeForImplementValue;

	private final NotNullLazyValue<DotNetTypeRef> myReturnTypeRefValue;

	private final NotNullLazyValue<DotNetTypeRef[]> myParameterTypeRefsValue;

	@RequiredReadAction
	public MsilPropertyAsCSharpIndexMethodDeclaration(PsiElement parent, MsilPropertyEntry propertyEntry, List<Pair<DotNetXAccessor, MsilMethodEntry>> pairs)
	{
		super(parent, propertyEntry);

		myAccessors = MsilPropertyAsCSharpPropertyDeclaration.buildAccessors(this, pairs);
		myModifierList = new MsilModifierListToCSharpModifierList(MsilPropertyAsCSharpPropertyDeclaration.getAdditionalModifiers(propertyEntry, pairs), this, propertyEntry.getModifierList());

		String name = getName();
		if(!Comparing.equal(name, DotNetPropertyDeclaration.DEFAULT_INDEX_PROPERTY_NAME))
		{
			CSharpLightAttributeBuilder attribute = new CSharpLightAttributeBuilder(propertyEntry, DotNetTypes.System.Runtime.CompilerServices.IndexerName);

			attribute.addParameterExpression(name);

			myModifierList.addAdditionalAttribute(attribute);
		}
		Pair<DotNetXAccessor, MsilMethodEntry> p = pairs.get(0);

		DotNetParameter firstParameter = p.getSecond().getParameters()[0];
		myParameters = new DotNetParameter[]{new MsilParameterAsCSharpParameter(this, firstParameter, this, 0)};

		myTypeForImplementValue = NullableLazyValue.of(() ->
		{
			String nameFromBytecode = myOriginal.getNameFromBytecode();
			String typeBeforeDot = StringUtil.getPackageName(nameFromBytecode);
			SomeType someType = SomeTypeParser.parseType(typeBeforeDot, nameFromBytecode);
			if(someType != null)
			{
				return new DummyType(getProject(), MsilPropertyAsCSharpIndexMethodDeclaration.this, someType);
			}
			return null;
		});

		myReturnTypeRefValue = NotNullLazyValue.createValue(() -> MsilToCSharpUtil.extractToCSharp(myOriginal.toTypeRef(false), myOriginal));
		myParameterTypeRefsValue = NotNullLazyValue.createValue(() ->
		{
			DotNetParameter[] parameters = getParameters();
			DotNetTypeRef[] typeRefs = new DotNetTypeRef[parameters.length];
			for(int i = 0; i < parameters.length; i++)
			{
				DotNetParameter parameter = parameters[i];
				typeRefs[i] = parameter.toTypeRef(false);
			}
			return typeRefs;
		});
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitIndexMethodDeclaration(this);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public CSharpSimpleParameterInfo[] getParameterInfos()
	{
		return CSharpLikeMethodDeclarationImplUtil.getParametersInfos(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return myOriginal.getPresentableParentQName();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableQName()
	{
		return myOriginal.getPresentableQName();
	}

	@Override
	public String getName()
	{
		return myOriginal.getName();
	}

	@Override
	public PsiElement setName(@NonNls @Nonnull String s) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public String toString()
	{
		return getPresentableQName();
	}

	@Nonnull
	@Override
	public DotNetXAccessor[] getAccessors()
	{
		return myAccessors;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetType getReturnType()
	{
		throw new IllegalArgumentException();
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		return myReturnTypeRefValue.getValue();
	}

	@Nonnull
	@Override
	public DotNetNamedElement[] getMembers()
	{
		return getAccessors();
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(@Nonnull DotNetModifier modifier)
	{
		return myModifierList.hasModifier(modifier);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return myModifierList;
	}

	@Nonnull
	@Override
	public DotNetTypeRef[] getParameterTypeRefs()
	{
		return myParameterTypeRefsValue.getValue();
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
		return myParameters;
	}

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
		return DotNetGenericParameter.EMPTY_ARRAY;
	}

	@Override
	public int getGenericParametersCount()
	{
		return 0;
	}

	@Nullable
	@Override
	public DotNetType getTypeForImplement()
	{
		return myTypeForImplementValue.getValue();
	}

	@Nonnull
	@Override
	public DotNetTypeRef getTypeRefForImplement()
	{
		DotNetType typeForImplement = getTypeForImplement();
		return typeForImplement != null ? typeForImplement.toTypeRef() : DotNetTypeRef.ERROR_TYPE;
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

	@Nullable
	@Override
	protected Class<? extends PsiElement> getNavigationElementClass()
	{
		return CSharpIndexMethodDeclaration.class;
	}
}
