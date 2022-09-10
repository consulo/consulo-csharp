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

import java.io.IOException;

import javax.annotation.Nonnull;

import consulo.csharp.lang.psi.CSharpNativeType;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.impl.psi.source.CSharpStubNativeTypeImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpWithIntValueStub;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import consulo.language.psi.stub.StubElement;
import consulo.util.collection.ArrayUtil;

/**
 * @author VISTALL
 * @since 19.10.14
 */
public class CSharpNativeTypeStubElementType extends CSharpAbstractStubElementType<CSharpWithIntValueStub<CSharpNativeType>, CSharpNativeType>
{
	public CSharpNativeTypeStubElementType()
	{
		super("NATIVE_TYPE");
	}

	@Nonnull
	@Override
	public PsiElement createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpStubNativeTypeImpl(astNode);
	}

	@Override
	public CSharpNativeType createPsi(@Nonnull CSharpWithIntValueStub<CSharpNativeType> stub)
	{
		return new CSharpStubNativeTypeImpl(stub, this);
	}

	@Override
	public CSharpWithIntValueStub<CSharpNativeType> createStub(@Nonnull CSharpNativeType cSharpNativeType, StubElement stubElement)
	{
		int index = ArrayUtil.indexOf(CSharpTokenSets.NATIVE_TYPES_AS_ARRAY, cSharpNativeType.getTypeElementType());
		assert index != -1;
		return new CSharpWithIntValueStub<CSharpNativeType>(stubElement, this, index);
	}

	@Override
	public void serialize(@Nonnull CSharpWithIntValueStub<CSharpNativeType> stub, @Nonnull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeVarInt(stub.getValue());
	}

	@Nonnull
	@Override
	public CSharpWithIntValueStub<CSharpNativeType> deserialize(@Nonnull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		int index = stubInputStream.readVarInt();
		return new CSharpWithIntValueStub<CSharpNativeType>(stubElement, this, index);
	}
}
