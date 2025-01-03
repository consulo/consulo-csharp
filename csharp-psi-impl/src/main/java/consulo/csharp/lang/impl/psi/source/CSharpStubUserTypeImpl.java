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

import consulo.language.ast.ASTNode;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.psi.CSharpUserType;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpUserTypeRef;
import consulo.csharp.lang.impl.psi.stub.CSharpWithStringValueStub;
import consulo.dotnet.psi.DotNetReferenceExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.stub.IStubElementType;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpStubUserTypeImpl extends CSharpStubTypeElementImpl<CSharpWithStringValueStub<CSharpUserType>> implements CSharpUserType
{
	public CSharpStubUserTypeImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpStubUserTypeImpl(@Nonnull CSharpWithStringValueStub<CSharpUserType> stub, @Nonnull IStubElementType<? extends CSharpWithStringValueStub<CSharpUserType>, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitUserType(this);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRefImpl()
	{
		return new CSharpUserTypeRef(getReferenceExpression());
	}

	@Nonnull
	@Override
	public String getReferenceText()
	{
		CSharpWithStringValueStub<CSharpUserType> stub = getGreenStub();
		if(stub != null)
		{
			//noinspection ConstantConditions
			return stub.getReferenceText();
		}
		DotNetReferenceExpression referenceExpression = getReferenceExpression();
		return referenceExpression.getText();
	}

	@Nonnull
	@Override
	public CSharpReferenceExpression getReferenceExpression()
	{
		return getRequiredStubOrPsiChild(CSharpStubElements.REFERENCE_EXPRESSION);
	}
}
