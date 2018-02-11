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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterList;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.msil.lang.psi.MsilMethodEntry;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilMethodAsCSharpConstructorDeclaration extends MsilMethodAsCSharpLikeMethodDeclaration implements CSharpConstructorDeclaration
{
	private final MsilClassAsCSharpTypeDefinition myTypeDefinition;
	private final boolean myDeConstructor;
	private final DotNetTypeRef myReturnTypeRef;

	public MsilMethodAsCSharpConstructorDeclaration(
			PsiElement parent, MsilClassAsCSharpTypeDefinition typeDefinition, MsilMethodEntry methodEntry, boolean deConstructor)
	{
		super(parent, methodEntry);
		myTypeDefinition = typeDefinition;
		myDeConstructor = deConstructor;
		myReturnTypeRef = new CSharpTypeRefByQName(typeDefinition, DotNetTypes.System.Void);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		return myReturnTypeRef;
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitConstructorDeclaration(this);
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

	@Override
	public String getName()
	{
		return myTypeDefinition.getName();
	}

	@Override
	public boolean isDeConstructor()
	{
		return myDeConstructor;
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}

	@Nullable
	@Override
	protected Class<? extends PsiElement> getNavigationElementClass()
	{
		return CSharpConstructorDeclaration.class;
	}
}
