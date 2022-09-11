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

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpStubElementSets;
import consulo.csharp.lang.psi.CSharpNullableType;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.impl.psi.CSharpNullableTypeUtil;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.EmptyStub;

/**
 * @author VISTALL
 * @since 17.04.14
 */
public class CSharpStubNullableTypeImpl extends CSharpStubTypeElementImpl<EmptyStub<CSharpNullableType>> implements CSharpNullableType
{
	public CSharpStubNullableTypeImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpStubNullableTypeImpl(@Nonnull EmptyStub<CSharpNullableType> stub,
			@Nonnull IStubElementType<? extends EmptyStub<CSharpNullableType>, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitNullableType(this);
	}

	@Override
	@Nonnull
	@RequiredReadAction
	public DotNetTypeRef toTypeRefImpl()
	{
		DotNetType innerType = getInnerType();
		if(innerType == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}
		return CSharpNullableTypeUtil.box(innerType.toTypeRef());
	}

	@RequiredReadAction
	@Override
	@Nullable
	public DotNetType getInnerType()
	{
		return getStubOrPsiChildByIndex(CSharpStubElementSets.TYPE_SET, 0);
	}

	@RequiredReadAction
	@Override
	@Nonnull
	public PsiElement getQuestElement()
	{
		return findNotNullChildByType(CSharpTokens.QUEST);
	}
}
