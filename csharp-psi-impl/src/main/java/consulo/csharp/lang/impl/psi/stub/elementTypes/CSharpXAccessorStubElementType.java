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

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.source.CSharpXAccessorImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpXAccessorStub;
import consulo.dotnet.psi.DotNetXAccessor;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.StubOutputStream;

import java.io.IOException;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class CSharpXAccessorStubElementType extends CSharpAbstractStubElementType<CSharpXAccessorStub, DotNetXAccessor>
{
	public CSharpXAccessorStubElementType()
	{
		super("XACCESSOR");
	}

	@Override
	public CSharpXAccessorImpl createElement(ASTNode astNode)
	{
		return new CSharpXAccessorImpl(astNode);
	}

	@Override
	public CSharpXAccessorImpl createPsi(CSharpXAccessorStub stub)
	{
		return new CSharpXAccessorImpl(stub);
	}

	@RequiredReadAction
	@Override
	public CSharpXAccessorStub createStub(DotNetXAccessor accessor, StubElement stubElement)
	{
		int otherModifiers = CSharpXAccessorStub.getOtherModifiers(accessor);
		return new CSharpXAccessorStub(stubElement, otherModifiers);
	}

	@Override
	public void serialize(CSharpXAccessorStub stub, StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeVarInt(stub.getOtherModifierMask());
	}

	@Override
	public CSharpXAccessorStub deserialize(StubInputStream inputStream, StubElement stubElement) throws IOException
	{
		int otherModifiers = inputStream.readVarInt();
		return new CSharpXAccessorStub(stubElement, otherModifiers);
	}
}
