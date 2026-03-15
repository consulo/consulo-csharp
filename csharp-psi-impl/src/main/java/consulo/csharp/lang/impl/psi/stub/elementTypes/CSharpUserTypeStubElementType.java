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


import consulo.csharp.lang.psi.CSharpUserType;
import consulo.csharp.lang.impl.psi.source.CSharpStubUserTypeImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpWithStringValueStub;
import consulo.index.io.StringRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;

/**
 * @author VISTALL
 * @since 19.10.14
 */
public class CSharpUserTypeStubElementType extends CSharpAbstractStubElementType<CSharpWithStringValueStub<CSharpUserType>,
		CSharpUserType>
{
	public CSharpUserTypeStubElementType()
	{
		super("USER_TYPE");
	}

	@Override
	public PsiElement createElement(ASTNode astNode)
	{
		return new CSharpStubUserTypeImpl(astNode);
	}

	@Override
	public CSharpUserType createPsi(CSharpWithStringValueStub<CSharpUserType> stub)
	{
		return new CSharpStubUserTypeImpl(stub, this);
	}

	@Override
	public CSharpWithStringValueStub<CSharpUserType> createStub(CSharpUserType cSharpUserType, StubElement stubElement)
	{

		return new CSharpWithStringValueStub<CSharpUserType>(stubElement, this, cSharpUserType.getReferenceText());
	}

	@Override
	public void serialize(CSharpWithStringValueStub<CSharpUserType> stub,
			StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(stub.getReferenceText());
	}

	@Override
	public CSharpWithStringValueStub<CSharpUserType> deserialize(StubInputStream stubInputStream,
			StubElement stubElement) throws IOException
	{
		StringRef ref = stubInputStream.readName();
		return new CSharpWithStringValueStub<CSharpUserType>(stubElement, this, ref);
	}
}
