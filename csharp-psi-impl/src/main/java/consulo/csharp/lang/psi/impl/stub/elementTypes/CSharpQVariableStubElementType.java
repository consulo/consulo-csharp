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

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NonNls;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.impl.stub.CSharpVariableDeclStub;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetVariable;

/**
 * @author VISTALL
 * @since 07.01.14.
 */
public abstract class CSharpQVariableStubElementType<P extends DotNetVariable & DotNetQualifiedElement> extends CSharpAbstractStubElementType<CSharpVariableDeclStub<P>, P>
{
	public CSharpQVariableStubElementType(@Nonnull @NonNls String debugName)
	{
		super(debugName);
	}

	@RequiredReadAction
	@Override
	public CSharpVariableDeclStub<P> createStub(@Nonnull P declaration, StubElement stubElement)
	{
		String namespaceQName = declaration.getPresentableParentQName();
		int otherModifierMask = CSharpVariableDeclStub.getOtherModifierMask(declaration);
		return new CSharpVariableDeclStub<P>(stubElement, this, namespaceQName, otherModifierMask, null);
	}

	@Override
	public void serialize(@Nonnull CSharpVariableDeclStub<P> variableStub, @Nonnull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(variableStub.getParentQName());
		stubOutputStream.writeVarInt(variableStub.getOtherModifierMask());
	}

	@Nonnull
	@Override
	public CSharpVariableDeclStub<P> deserialize(@Nonnull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		StringRef parentQName = stubInputStream.readName();
		int otherModifierMask = stubInputStream.readVarInt();
		return new CSharpVariableDeclStub<P>(stubElement, this, StringRef.toString(parentQName), otherModifierMask, null);
	}
}
