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

package consulo.csharp.lang.impl.psi.msil;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.NullableLazyValue;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.msil.typeParsing.SomeType;
import consulo.csharp.lang.impl.psi.msil.typeParsing.SomeTypeParser;
import consulo.csharp.lang.psi.CSharpEventDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetXAccessor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.msil.lang.psi.MsilEventEntry;
import consulo.msil.lang.psi.MsilMethodEntry;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;

import jakarta.annotation.Nullable;
import java.util.List;

/**
 * @author VISTALL
 * @since 24.05.14
 */
public class MsilEventAsCSharpEventDeclaration extends MsilVariableAsCSharpVariable implements CSharpEventDeclaration
{
	private final DotNetXAccessor[] myAccessors;

	private final NullableLazyValue<DotNetType> myTypeForImplementValue;

	@RequiredReadAction
	public MsilEventAsCSharpEventDeclaration(PsiElement parent, MsilEventEntry variable, List<Pair<DotNetXAccessor, MsilMethodEntry>> pairs)
	{
		super(parent, MsilPropertyAsCSharpPropertyDeclaration.getAdditionalModifiers(variable, pairs), variable);
		myAccessors = MsilPropertyAsCSharpPropertyDeclaration.buildAccessors(this, pairs);

		myTypeForImplementValue = NullableLazyValue.of(() ->
		{
			String nameFromBytecode = getVariable().getNameFromBytecode();
			String typeBeforeDot = StringUtil.getPackageName(nameFromBytecode);
			SomeType someType = SomeTypeParser.parseType(typeBeforeDot, nameFromBytecode);
			if(someType != null)
			{
				return new DummyType(getProject(), MsilEventAsCSharpEventDeclaration.this, someType);
			}
			return null;
		});
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitEventDeclaration(this);
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

	@Nonnull
	@Override
	public DotNetXAccessor[] getAccessors()
	{
		return myAccessors;
	}

	@Nonnull
	@Override
	public DotNetNamedElement[] getMembers()
	{
		return getAccessors();
	}

	@Override
	public MsilEventEntry getVariable()
	{
		return (MsilEventEntry) super.getVariable();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return getVariable().getPresentableParentQName();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableQName()
	{
		return getVariable().getPresentableQName();
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

	@Nullable
	@Override
	protected Class<? extends PsiElement> getNavigationElementClass()
	{
		return CSharpEventDeclaration.class;
	}
}
