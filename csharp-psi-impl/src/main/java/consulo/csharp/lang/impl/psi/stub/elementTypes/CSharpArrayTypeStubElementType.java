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

package consulo.csharp.lang.impl.psi.stub.elementTypes;

import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.StubOutputStream;
import consulo.csharp.lang.psi.CSharpArrayType;
import consulo.csharp.lang.impl.psi.source.CSharpStubArrayTypeImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpWithIntValueStub;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;

import jakarta.annotation.Nonnull;
import java.io.IOException;

/**
 * @author VISTALL
 * @since 19.10.14
 */
public class CSharpArrayTypeStubElementType extends CSharpAbstractStubElementType<CSharpWithIntValueStub<CSharpArrayType>, CSharpArrayType>
{
	public CSharpArrayTypeStubElementType()
	{
		super("ARRAY_TYPE");
	}

	@Nonnull
	@Override
	public PsiElement createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpStubArrayTypeImpl(astNode);
	}

	@Override
	public CSharpArrayType createPsi(@Nonnull CSharpWithIntValueStub<CSharpArrayType> stub)
	{
		return new CSharpStubArrayTypeImpl(stub, this);
	}

	@Nonnull
	@Override
	public CSharpWithIntValueStub<CSharpArrayType> createStub(@Nonnull CSharpArrayType arrayType, StubElement stubElement)
	{
		return new CSharpWithIntValueStub<>(stubElement, this, arrayType.getDimensions());
	}

	@Override
	public void serialize(@Nonnull CSharpWithIntValueStub stub, @Nonnull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeVarInt(stub.getValue());
	}

	@Nonnull
	@Override
	public CSharpWithIntValueStub<CSharpArrayType> deserialize(@Nonnull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		int i = stubInputStream.readVarInt();
		return new CSharpWithIntValueStub<>(stubElement, this, i);
	}
}
