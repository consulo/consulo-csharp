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

import java.util.ArrayList;
import java.util.List;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpAccessModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.typeParsing.SomeType;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.typeParsing.SomeTypeParser;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.msil.lang.psi.MsilClassEntry;
import org.mustbe.consulo.msil.lang.psi.MsilMethodEntry;
import org.mustbe.consulo.msil.lang.psi.MsilPropertyEntry;
import org.mustbe.consulo.msil.lang.psi.MsilTokens;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 24.05.14
 */
public class MsilPropertyAsCSharpPropertyDeclaration extends MsilVariableAsCSharpVariable implements CSharpPropertyDeclaration
{
	private DotNetXXXAccessor[] myAccessors;

	public MsilPropertyAsCSharpPropertyDeclaration(PsiElement parent,
			MsilPropertyEntry variable,
			List<Pair<DotNetXXXAccessor, MsilMethodEntry>> pairs)
	{
		super(parent, getAdditionalModifiers(variable, pairs), variable);
		myAccessors = buildAccessors(this, pairs);
	}

	public static DotNetXXXAccessor[] buildAccessors(@NotNull PsiElement parent,
			@NotNull List<Pair<DotNetXXXAccessor, MsilMethodEntry>> pairs)
	{
		List<DotNetXXXAccessor> accessors = new ArrayList<DotNetXXXAccessor>(2);

		for(Pair<DotNetXXXAccessor, MsilMethodEntry> pair : pairs)
		{
			accessors.add(new MsilXXXAccessorAsCSharpXXXAccessor(parent, pair.getFirst(), pair.getSecond()));
		}
		return ContainerUtil.toArray(accessors, DotNetXXXAccessor.ARRAY_FACTORY);
	}

	@NotNull
	public static CSharpModifier[] getAdditionalModifiers(PsiElement parent, List<Pair<DotNetXXXAccessor, MsilMethodEntry>> pairs)
	{
		PsiElement maybeTypeParent = parent.getParent();
		if(maybeTypeParent instanceof MsilClassEntry && ((MsilClassEntry) maybeTypeParent).hasModifier(MsilTokens.INTERFACE_KEYWORD))
		{
			return CSharpModifier.EMPTY_ARRAY;
		}

		boolean staticMod = false;
		List<CSharpAccessModifier> modifiers = new SmartList<CSharpAccessModifier>();
		for(Pair<DotNetXXXAccessor, MsilMethodEntry> pair : pairs)
		{
			CSharpAccessModifier accessModifier = getAccessModifier(pair.getSecond());
			modifiers.add(accessModifier);

			if(pair.getSecond().hasModifier(MsilTokens.STATIC_KEYWORD))
			{
				staticMod = true;
			}
		}
		ContainerUtil.sort(modifiers);

		CSharpAccessModifier access = modifiers.isEmpty() ? CSharpAccessModifier.PUBLIC : modifiers.get(0);
		return staticMod ? ArrayUtil.append(access.getModifiers(), CSharpModifier.STATIC) : access.getModifiers();
	}

	private static CSharpAccessModifier getAccessModifier(MsilMethodEntry second)
	{
		if(second.hasModifier(MsilTokens.PRIVATE_KEYWORD))
		{
			return CSharpAccessModifier.PRIVATE;
		}
		else if(second.hasModifier(MsilTokens.PUBLIC_KEYWORD))
		{
			return CSharpAccessModifier.PUBLIC;
		}
		else if(second.hasModifier(MsilTokens.ASSEMBLY_KEYWORD) && second.hasModifier(MsilTokens.PROTECTED_KEYWORD))
		{
			return CSharpAccessModifier.PROTECTED_INTERNAL;
		}
		else if(second.hasModifier(MsilTokens.ASSEMBLY_KEYWORD))
		{
			return CSharpAccessModifier.INTERNAL;
		}
		else if(second.hasModifier(MsilTokens.PROTECTED_KEYWORD))
		{
			return CSharpAccessModifier.PROTECTED;
		}
		return CSharpAccessModifier.PUBLIC;
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitPropertyDeclaration(this);
	}

	@Override
	public MsilPropertyEntry getVariable()
	{
		return (MsilPropertyEntry) super.getVariable();
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
			return new DummyType(getProject(), myOriginal, someType);
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
