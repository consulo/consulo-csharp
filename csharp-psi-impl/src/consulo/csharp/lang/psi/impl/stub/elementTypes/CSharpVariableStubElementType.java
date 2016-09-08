/*
 * Copyright 2013-2014 must-be.org
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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.impl.stub.CSharpVariableDeclStub;
import consulo.dotnet.psi.DotNetVariable;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

/**
 * @author VISTALL
 * @since 15.01.14.
 */
public abstract class CSharpVariableStubElementType<P extends DotNetVariable> extends
		CSharpAbstractStubElementType<CSharpVariableDeclStub<P>, P>
{
	public CSharpVariableStubElementType(@NotNull @NonNls String debugName)
	{
		super(debugName);
	}

	@RequiredReadAction
	@Override
	public CSharpVariableDeclStub<P> createStub(@NotNull P variable, StubElement stubElement)
	{
		int otherModifierMask = CSharpVariableDeclStub.getOtherModifierMask(variable);
		return new CSharpVariableDeclStub<P>(stubElement, this, null, otherModifierMask);
	}

	@Override
	public void serialize(@NotNull CSharpVariableDeclStub<P> cSharpPropertyStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeVarInt(cSharpPropertyStub.getOtherModifierMask());
	}

	@NotNull
	@Override
	public CSharpVariableDeclStub<P> deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		int otherModifierMask = stubInputStream.readVarInt();
		return new CSharpVariableDeclStub<P>(stubElement, this, null, otherModifierMask);
	}
}
