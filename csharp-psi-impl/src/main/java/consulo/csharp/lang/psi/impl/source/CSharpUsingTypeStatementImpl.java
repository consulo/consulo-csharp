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
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpStubElements;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpUsingTypeStatement;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.EmptyStub;
import com.intellij.psi.stubs.IStubElementType;

/**
 * @author VISTALL
 * @since 11.12.14
 */
public class CSharpUsingTypeStatementImpl extends CSharpStubElementImpl<EmptyStub<CSharpUsingTypeStatement>> implements CSharpUsingTypeStatement
{
	public CSharpUsingTypeStatementImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpUsingTypeStatementImpl(@Nonnull EmptyStub<CSharpUsingTypeStatement> stub,
			@Nonnull IStubElementType<? extends EmptyStub<CSharpUsingTypeStatement>, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public PsiElement getUsingKeywordElement()
	{
		return findNotNullChildByType(CSharpTokens.USING_KEYWORD);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitUsingTypeStatement(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getReferenceElement()
	{
		return getType();
	}

	@Nonnull
	@Override
	public DotNetTypeRef getTypeRef()
	{
		DotNetType type = getType();
		if(type == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}
		return type.toTypeRef();
	}

	@Nullable
	@Override
	public DotNetType getType()
	{
		return getStubOrPsiChildByIndex(CSharpStubElements.TYPE_SET, 0);
	}
}
