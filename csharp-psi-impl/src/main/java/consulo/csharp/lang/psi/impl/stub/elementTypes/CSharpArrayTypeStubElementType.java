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

package consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import consulo.csharp.lang.psi.CSharpArrayType;
import consulo.csharp.lang.psi.impl.source.CSharpStubArrayTypeImpl;
import consulo.csharp.lang.psi.impl.stub.CSharpWithIntValueStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

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

	@NotNull
	@Override
	public PsiElement createElement(@NotNull ASTNode astNode)
	{
		return new CSharpStubArrayTypeImpl(astNode);
	}

	@Override
	public CSharpArrayType createPsi(@NotNull CSharpWithIntValueStub<CSharpArrayType> stub)
	{
		return new CSharpStubArrayTypeImpl(stub, this);
	}

	@Override
	public CSharpWithIntValueStub<CSharpArrayType> createStub(@NotNull CSharpArrayType cSharpArrayType, StubElement stubElement)
	{
		return new CSharpWithIntValueStub<CSharpArrayType>(stubElement, this, cSharpArrayType.getDimensions());
	}

	@Override
	public void serialize(@NotNull CSharpWithIntValueStub stub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeVarInt(stub.getValue());
	}

	@NotNull
	@Override
	public CSharpWithIntValueStub<CSharpArrayType> deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		int i = stubInputStream.readVarInt();
		return new CSharpWithIntValueStub<CSharpArrayType>(stubElement, this, i);
	}
}
