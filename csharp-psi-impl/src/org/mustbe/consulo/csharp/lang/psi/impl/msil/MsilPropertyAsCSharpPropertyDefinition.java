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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpAccessModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import org.mustbe.consulo.msil.lang.psi.MsilMethodEntry;
import org.mustbe.consulo.msil.lang.psi.MsilPropertyEntry;
import org.mustbe.consulo.msil.lang.psi.MsilTokens;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 24.05.14
 */
public class MsilPropertyAsCSharpPropertyDefinition extends MsilVariableAsCSharpVariable implements CSharpPropertyDeclaration
{
	public MsilPropertyAsCSharpPropertyDefinition(MsilPropertyEntry variable, List<Pair<DotNetXXXAccessor, MsilMethodEntry>> pairs)
	{
		super(getAdditionalModifiers(pairs), variable);
	}

	public static CSharpModifier[] getAdditionalModifiers(List<Pair<DotNetXXXAccessor, MsilMethodEntry>> pairs)
	{
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

		CSharpModifier access = modifiers.isEmpty() ? CSharpModifier.PUBLIC : modifiers.get(0).toModifier();
		return staticMod ? new CSharpModifier[] {access, CSharpModifier.STATIC} : new CSharpModifier[] {access};
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
		return new DotNetXXXAccessor[0];
	}

	@NotNull
	@Override
	public DotNetNamedElement[] getMembers()
	{
		return new DotNetNamedElement[0];
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
}
