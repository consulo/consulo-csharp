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
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.NonNls;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.impl.stub.CSharpVariableDeclStub;
import consulo.dotnet.psi.DotNetVariable;

/**
 * @author VISTALL
 * @since 15.01.14.
 */
public abstract class CSharpVariableStubElementType<P extends DotNetVariable> extends CSharpAbstractStubElementType<CSharpVariableDeclStub<P>, P>
{
	public CSharpVariableStubElementType(@Nonnull @NonNls String debugName)
	{
		super(debugName);
	}

	@RequiredReadAction
	@Override
	public CSharpVariableDeclStub<P> createStub(@Nonnull P variable, StubElement stubElement)
	{
		int otherModifierMask = CSharpVariableDeclStub.getOtherModifierMask(variable);
		Function<P, String> initializerGetter = getInitializerGetter();
		String initializerText = initializerGetter == null ? null : initializerGetter.apply(variable);
		return new CSharpVariableDeclStub<>(stubElement, this, null, otherModifierMask, initializerText);
	}

	@Override
	public void serialize(@Nonnull CSharpVariableDeclStub<P> variableStub, @Nonnull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeVarInt(variableStub.getOtherModifierMask());
		Function<P, String> initializerGetter = getInitializerGetter();
		if(initializerGetter != null)
		{
			stubOutputStream.writeName(variableStub.getInitializerText());
		}
	}

	@Nonnull
	@Override
	public CSharpVariableDeclStub<P> deserialize(@Nonnull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		int otherModifierMask = stubInputStream.readVarInt();
		String initializerText = null;
		Function<P, String> initializerGetter = getInitializerGetter();
		if(initializerGetter != null)
		{
			initializerText = StringRef.toString(stubInputStream.readName());
		}
		return new CSharpVariableDeclStub<>(stubElement, this, null, otherModifierMask, initializerText);
	}

	@Nullable
	protected Function<P, String> getInitializerGetter()
	{
		return null;
	}
}
