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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpEventDeclaration;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.impl.psi.stub.CSharpVariableDeclStub;
import consulo.language.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.DotNetEventDeclaration;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetXAccessor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;

/**
 * @author VISTALL
 * @since 04.12.13.
 */
public class CSharpEventDeclarationImpl extends CSharpStubVariableImpl<CSharpVariableDeclStub<DotNetEventDeclaration>> implements CSharpEventDeclaration
{
	public CSharpEventDeclarationImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpEventDeclarationImpl(@Nonnull CSharpVariableDeclStub<DotNetEventDeclaration> stub)
	{
		super(stub, CSharpStubElements.EVENT_DECLARATION);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetType getType()
	{
		return CSharpStubVariableImplUtil.getType(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return CSharpStubVariableImplUtil.getModifierList(this);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitEventDeclaration(this);
	}

	@Nonnull
	@Override
	public DotNetXAccessor[] getAccessors()
	{
		return getStubOrPsiChildren(CSharpStubElements.XACCESSOR, DotNetXAccessor.ARRAY_FACTORY);
	}

	@Nullable
	@Override
	public DotNetExpression getInitializer()
	{
		return findChildByClass(DotNetExpression.class);
	}

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
		return getStubOrPsiChildByIndex(CSharpStubElements.TYPE_SET, 1);
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
}
