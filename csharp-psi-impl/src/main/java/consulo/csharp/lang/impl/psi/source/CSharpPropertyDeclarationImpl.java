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

package consulo.csharp.lang.impl.psi.source;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpStubElementSets;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.impl.psi.stub.CSharpVariableDeclStub;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetXAccessor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;

/**
 * @author VISTALL
 * @since 04.12.13.
 */
public class CSharpPropertyDeclarationImpl extends CSharpStubVariableImpl<CSharpVariableDeclStub<CSharpPropertyDeclarationImpl>> implements CSharpPropertyDeclaration
{
	public CSharpPropertyDeclarationImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpPropertyDeclarationImpl(@Nonnull CSharpVariableDeclStub<CSharpPropertyDeclarationImpl> stub)
	{
		super(stub, CSharpStubElements.PROPERTY_DECLARATION);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitPropertyDeclaration(this);
	}

	@Nonnull
	@Override
	public DotNetXAccessor[] getAccessors()
	{
		return getStubOrPsiChildren(CSharpStubElements.XACCESSOR, DotNetXAccessor.ARRAY_FACTORY);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetExpression getInitializer()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetNamedElement[] getMembers()
	{
		return getAccessors();
	}

	@RequiredReadAction
	@Override
	public PsiElement getLeftBrace()
	{
		return findChildByType(CSharpTokens.LBRACE);
	}

	@RequiredReadAction
	@Override
	public PsiElement getRightBrace()
	{
		return findChildByType(CSharpTokens.RBRACE);
	}

	@Nullable
	@Override
	public DotNetType getTypeForImplement()
	{
		return getStubOrPsiChildByIndex(CSharpStubElementSets.TYPE_SET, 1);
	}

	@Nonnull
	@Override
	public DotNetTypeRef getTypeRefForImplement()
	{
		DotNetType typeForImplement = getTypeForImplement();
		if(typeForImplement == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}
		else
		{
			return typeForImplement.toTypeRef();
		}
	}

	@RequiredReadAction
	@Override
	public boolean isAutoGet()
	{
		CSharpVariableDeclStub<CSharpPropertyDeclarationImpl> greenStub = getGreenStub();
		if(greenStub != null)
		{
			return greenStub.isAutoGet();
		}

		return findChildByType(CSharpTokens.DARROW) != null;
	}
}
