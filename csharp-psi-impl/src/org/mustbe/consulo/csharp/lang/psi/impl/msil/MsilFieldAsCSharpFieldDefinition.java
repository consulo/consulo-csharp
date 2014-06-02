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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.msil.lang.psi.MsilFieldEntry;
import com.intellij.psi.PsiElementVisitor;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilFieldAsCSharpFieldDefinition extends MsilVariableAsCSharpVariable implements CSharpFieldDeclaration
{
	public MsilFieldAsCSharpFieldDefinition(DotNetQualifiedElement buildRoot, DotNetVariable variable)
	{
		super(buildRoot, variable);
	}

	@Override
	public void accept(@NotNull PsiElementVisitor visitor)
	{
		if(visitor instanceof CSharpElementVisitor)
		{
			((CSharpElementVisitor) visitor).visitFieldDeclaration(this);
		}
		else
		{
			visitor.visitElement(this);
		}
	}

	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return getVariable().getPresentableQName();
	}

	@Override
	public MsilFieldEntry getVariable()
	{
		return (MsilFieldEntry) super.getVariable();
	}

	@Nullable
	@Override
	public String getPresentableQName()
	{
		return getVariable().getPresentableQName();
	}
}
