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

package consulo.csharp.lang.psi.impl.source;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpStubElements;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpPointerTypeRef;
import consulo.dotnet.psi.DotNetPointerType;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.EmptyStub;
import com.intellij.psi.stubs.IStubElementType;

/**
 * @author VISTALL
 * @since 13.12.13.
 */
public class CSharpStubPointerTypeImpl extends CSharpStubTypeElementImpl<EmptyStub<DotNetPointerType>> implements DotNetPointerType
{
	public CSharpStubPointerTypeImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpStubPointerTypeImpl(@Nonnull EmptyStub<DotNetPointerType> stub,
			@Nonnull IStubElementType<? extends EmptyStub<DotNetPointerType>, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitPointerType(this);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRefImpl()
	{
		DotNetType innerType = getInnerType();
		if(innerType == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}
		return new CSharpPointerTypeRef(this, innerType.toTypeRef());
	}

	@Nullable
	@Override
	public DotNetType getInnerType()
	{
		return getStubOrPsiChildByIndex(CSharpStubElements.TYPE_SET, 0);
	}

	@Nonnull
	@Override
	public PsiElement getAsterisk()
	{
		return findNotNullChildByType(CSharpTokens.MUL);
	}
}
