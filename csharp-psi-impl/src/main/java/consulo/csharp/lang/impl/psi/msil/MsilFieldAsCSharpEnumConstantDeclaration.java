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
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpEnumConstantDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.language.psi.PsiElement;
import consulo.msil.lang.psi.MsilFieldEntry;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 20.07.14
 */
public class MsilFieldAsCSharpEnumConstantDeclaration extends MsilVariableAsCSharpVariable implements CSharpEnumConstantDeclaration
{
	public MsilFieldAsCSharpEnumConstantDeclaration(PsiElement parent, DotNetVariable variable)
	{
		super(parent, variable);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitEnumConstantDeclaration(this);
	}

	@RequiredReadAction
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

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableQName()
	{
		return getVariable().getPresentableQName();
	}

	@Nullable
	@Override
	protected Class<? extends PsiElement> getNavigationElementClass()
	{
		return CSharpEnumConstantDeclaration.class;
	}
}
