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

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpStubElementSets;
import consulo.csharp.lang.psi.CSharpArrayType;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpArrayTypeRef;
import consulo.csharp.lang.impl.psi.stub.CSharpWithIntValueStub;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.IStubElementType;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 13.12.13.
 */
public class CSharpStubArrayTypeImpl extends CSharpStubTypeElementImpl<CSharpWithIntValueStub<CSharpArrayType>> implements CSharpArrayType
{
	public CSharpStubArrayTypeImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpStubArrayTypeImpl(@Nonnull CSharpWithIntValueStub<CSharpArrayType> stub, @Nonnull IStubElementType<? extends CSharpWithIntValueStub<CSharpArrayType>, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRefImpl()
	{
		DotNetType innerType = getInnerType();

		return new CSharpArrayTypeRef(getProject(), getResolveScope(), innerType.toTypeRef(), getDimensions());
	}

	@RequiredReadAction
	@Override
	public int getDimensions()
	{
		CSharpWithIntValueStub<CSharpArrayType> stub = getGreenStub();
		if(stub != null)
		{
			return stub.getValue();
		}
		return findChildrenByType(CSharpTokenSets.COMMA).size();
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitArrayType(this);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetType getInnerType()
	{
		return getRequiredStubOrPsiChildByIndex(CSharpStubElementSets.TYPE_SET, 0);
	}
}
