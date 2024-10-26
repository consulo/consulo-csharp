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
import consulo.csharp.lang.impl.psi.CSharpStubElementSets;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.impl.psi.stub.CSharpVariableDeclStub;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;

/**
 * @author VISTALL
 * @since 06.01.14.
 */
public abstract class CSharpStubVariableImpl<S extends CSharpVariableDeclStub<?>> extends CSharpStubMemberImpl<S> implements DotNetVariable
{
	private static final CSharpTypeRefCacher<CSharpStubVariableImpl> ourCacheSystem = new CSharpTypeRefCacher<CSharpStubVariableImpl>(false)
	{
		@RequiredReadAction
		@Nonnull
		@Override
		protected DotNetTypeRef toTypeRefImpl(CSharpStubVariableImpl element, boolean resolveFromParentOrInitializer)
		{
			return element.toTypeRefImpl(resolveFromParentOrInitializer);
		}
	};

	public CSharpStubVariableImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpStubVariableImpl(@Nonnull S stub, @Nonnull IStubElementType<? extends S, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@RequiredReadAction
	@Override
	public boolean isConstant()
	{
		return false;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getConstantKeywordElement()
	{
		return getExplicitConstantKeywordElement();
	}

	@RequiredReadAction
	@Override
	@Nullable
	public DotNetType getType()
	{
		return getExplicitType();
	}

	@Nullable
	public DotNetType getExplicitType()
	{
		return getStubOrPsiChildByIndex(CSharpStubElementSets.TYPE_SET, 0);
	}

	@Nullable
	@RequiredReadAction
	public DotNetModifierList getExplicitModifierList()
	{
		return getStubOrPsiChild(CSharpStubElements.MODIFIER_LIST);
	}

	@Nullable
	@RequiredReadAction
	public PsiElement getExplicitConstantKeywordElement()
	{
		return findChildByType(CSharpTokens.CONST_KEYWORD);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromInitializer)
	{
		return ourCacheSystem.toTypeRef(this, resolveFromInitializer);
	}

	@RequiredReadAction
	@Nonnull
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromInitializer)
	{
		DotNetType type = getType();
		return type == null ? DotNetTypeRef.ERROR_TYPE : type.toTypeRef();
	}
}
