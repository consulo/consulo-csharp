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

import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpEnumConstantDeclaration;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByTypeDeclaration;
import consulo.csharp.lang.impl.psi.stub.CSharpVariableDeclStub;
import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.util.PsiTreeUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 08.01.14.
 */
public class CSharpEnumConstantDeclarationImpl extends CSharpStubVariableImpl<CSharpVariableDeclStub<CSharpEnumConstantDeclarationImpl>> implements
		CSharpEnumConstantDeclaration
{
	public CSharpEnumConstantDeclarationImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpEnumConstantDeclarationImpl(@Nonnull CSharpVariableDeclStub<CSharpEnumConstantDeclarationImpl> stub)
	{
		super(stub, CSharpStubElements.ENUM_CONSTANT_DECLARATION);
	}

	@RequiredReadAction
	@Override
	public boolean isConstant()
	{
		return true;
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitEnumConstantDeclaration(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetType getType()
	{
		return null;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromInitializer)
	{
		DotNetTypeDeclaration declaration = PsiTreeUtil.getParentOfType(this, DotNetTypeDeclaration.class);
		if(declaration == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}
		return new CSharpTypeRefByTypeDeclaration(declaration);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetExpression getInitializer()
	{
		return null;
	}
}
