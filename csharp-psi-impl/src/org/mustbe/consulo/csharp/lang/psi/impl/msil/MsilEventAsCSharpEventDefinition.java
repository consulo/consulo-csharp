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
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpEventDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import org.mustbe.consulo.msil.lang.psi.MsilEventEntry;
import org.mustbe.consulo.msil.lang.psi.MsilMethodEntry;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;

/**
 * @author VISTALL
 * @since 24.05.14
 */
public class MsilEventAsCSharpEventDefinition extends MsilVariableAsCSharpVariable implements CSharpEventDeclaration
{
	public MsilEventAsCSharpEventDefinition(PsiElement parent, MsilEventEntry variable, List<Pair<DotNetXXXAccessor,
			MsilMethodEntry>> pairs)
	{
		super(parent, MsilPropertyAsCSharpPropertyDefinition.getAdditionalModifiers(pairs), variable);
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
		return new DotNetXXXAccessor[0];
	}

	@NotNull
	@Override
	public DotNetNamedElement[] getMembers()
	{
		return new DotNetNamedElement[0];
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
}
