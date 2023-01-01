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

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpNativeType;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.impl.psi.stub.CSharpWithIntValueStub;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;

/**
 * @author VISTALL
 * @since 13.12.13.
 */
public class CSharpStubNativeTypeImpl extends CSharpStubTypeElementImpl<CSharpWithIntValueStub<CSharpNativeType>> implements CSharpNativeType
{
	public CSharpStubNativeTypeImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpStubNativeTypeImpl(@Nonnull CSharpWithIntValueStub<CSharpNativeType> stub,
			@Nonnull IStubElementType<? extends CSharpWithIntValueStub<CSharpNativeType>, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitNativeType(this);
	}

	@RequiredReadAction
	@Override
	@Nonnull
	public IElementType getTypeElementType()
	{
		CSharpWithIntValueStub<CSharpNativeType> stub = getGreenStub();
		if(stub != null)
		{
			return CSharpTokenSets.NATIVE_TYPES_AS_ARRAY[stub.getValue()];
		}
		return getTypeElement().getNode().getElementType();
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRefImpl()
	{
		return CSharpNativeTypeImplUtil.toTypeRef(this);
	}

	@Nonnull
	@Override
	public PsiElement getTypeElement()
	{
		return findNotNullChildByFilter(CSharpTokenSets.NATIVE_TYPES);
	}
}
