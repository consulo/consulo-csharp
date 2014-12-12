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

package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import java.util.List;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpEventDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.typeParsing.SomeType;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.typeParsing.SomeTypeParser;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.msil.lang.psi.MsilEventEntry;
import org.mustbe.consulo.msil.lang.psi.MsilMethodEntry;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;

/**
 * @author VISTALL
 * @since 24.05.14
 */
public class MsilEventAsCSharpEventDeclaration extends MsilVariableAsCSharpVariable implements CSharpEventDeclaration
{
	private final DotNetXXXAccessor[] myAccessors;

	public MsilEventAsCSharpEventDeclaration(PsiElement parent, MsilEventEntry variable, List<Pair<DotNetXXXAccessor, MsilMethodEntry>> pairs)
	{
		super(parent, MsilPropertyAsCSharpPropertyDeclaration.getAdditionalModifiers(variable, pairs), variable);
		myAccessors = MsilPropertyAsCSharpPropertyDeclaration.buildAccessors(this, pairs);
	}

	@Override
	public void accept(@NotNull PsiElementVisitor visitor)
	{
		if(visitor instanceof CSharpElementVisitor)
		{
			((CSharpElementVisitor) visitor).visitEventDeclaration(this);
		}
		else
		{
			visitor.visitElement(this);
		}
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

	@NotNull
	@Override
	public DotNetXXXAccessor[] getAccessors()
	{
		return myAccessors;
	}

	@NotNull
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

	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return getVariable().getPresentableParentQName();
	}

	@Nullable
	@Override
	public String getPresentableQName()
	{
		return getVariable().getPresentableQName();
	}

	@Nullable
	@Override
	@LazyInstance(notNull = false)
	public DotNetType getTypeForImplement()
	{
		String nameFromBytecode = getVariable().getNameFromBytecode();
		String typeBeforeDot = StringUtil.getPackageName(nameFromBytecode);
		SomeType someType = SomeTypeParser.parseType(typeBeforeDot, nameFromBytecode);
		if(someType != null)
		{
			return new DummyType(getProject(), myMsilElement, someType);
		}
		return null;
	}

	@NotNull
	@Override
	@LazyInstance
	public DotNetTypeRef getTypeRefForImplement()
	{
		DotNetType typeForImplement = getTypeForImplement();
		return typeForImplement != null ? typeForImplement.toTypeRef() : DotNetTypeRef.ERROR_TYPE;
	}
}
