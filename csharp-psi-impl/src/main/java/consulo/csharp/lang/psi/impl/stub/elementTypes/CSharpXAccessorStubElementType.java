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

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.impl.source.CSharpXAccessorImpl;
import consulo.csharp.lang.psi.impl.stub.CSharpXXXAccessorStub;
import consulo.dotnet.psi.DotNetXAccessor;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class CSharpXAccessorStubElementType extends CSharpAbstractStubElementType<CSharpXXXAccessorStub, DotNetXAccessor>
{
	public CSharpXAccessorStubElementType()
	{
		super("XACCESSOR");
	}

	@Nonnull
	@Override
	public CSharpXAccessorImpl createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpXAccessorImpl(astNode);
	}

	@Override
	public CSharpXAccessorImpl createPsi(@Nonnull CSharpXXXAccessorStub cSharpXXXAccessorStub)
	{
		return new CSharpXAccessorImpl(cSharpXXXAccessorStub);
	}

	@Nonnull
	@RequiredReadAction
	@Override
	public CSharpXXXAccessorStub createStub(@Nonnull DotNetXAccessor accessor, StubElement stubElement)
	{
		int otherModifiers = CSharpXXXAccessorStub.getOtherModifiers(accessor);
		return new CSharpXXXAccessorStub(stubElement, otherModifiers);
	}

	@Override
	public void serialize(@Nonnull CSharpXXXAccessorStub stub, @Nonnull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeVarInt(stub.getOtherModifierMask());
	}

	@Nonnull
	@Override
	public CSharpXXXAccessorStub deserialize(@Nonnull StubInputStream inputStream, StubElement stubElement) throws IOException
	{
		int otherModifiers = inputStream.readVarInt();
		return new CSharpXXXAccessorStub(stubElement, otherModifiers);
	}
}
