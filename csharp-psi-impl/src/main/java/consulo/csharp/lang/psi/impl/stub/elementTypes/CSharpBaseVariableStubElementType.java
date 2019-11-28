/*
 * Copyright 2013-2019 consulo.io
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

import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.impl.stub.CSharpVariableDeclStub;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetVariable;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * @author VISTALL
 * @since 2019-10-10
 */
public abstract class CSharpBaseVariableStubElementType<V extends DotNetVariable> extends CSharpAbstractStubElementType<CSharpVariableDeclStub<V>, V>
{
	public CSharpBaseVariableStubElementType(@Nonnull @NonNls String debugName)
	{
		super(debugName);
	}

	@RequiredReadAction
	@Override
	public CSharpVariableDeclStub<V> createStub(@Nonnull V variable, StubElement stubElement)
	{
		int otherModifierMask = CSharpVariableDeclStub.getOtherModifierMask(variable);
		String qName = null;
		String initializerText = null;
		if(supportsParentQName())
		{
			qName = getParentQName(variable);
		}

		if(supportsInitializer(otherModifierMask))
		{
			initializerText = getInitializerText(variable);
		}

		return new CSharpVariableDeclStub<>(stubElement, this, qName, otherModifierMask, initializerText);
	}

	@Override
	@RequiredReadAction
	public void serialize(@Nonnull CSharpVariableDeclStub<V> variableStub, @Nonnull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeVarInt(variableStub.getOtherModifierMask());

		if(supportsParentQName())
		{
			stubOutputStream.writeName(variableStub.getParentQName());
		}

		if(supportsInitializer(variableStub.getOtherModifierMask()))
		{
			stubOutputStream.writeName(variableStub.getInitializerText());
		}
	}

	@Nonnull
	@Override
	public CSharpVariableDeclStub<V> deserialize(@Nonnull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		int otherModifierMask = stubInputStream.readVarInt();

		String parentQName = null;
		if(supportsParentQName())
		{
			parentQName = StringRef.toString(stubInputStream.readName());
		}

		String initializerText = null;
		if(supportsInitializer(otherModifierMask))
		{
			initializerText = StringRef.toString(stubInputStream.readName());
		}
		return new CSharpVariableDeclStub<>(stubElement, this, parentQName, otherModifierMask, initializerText);
	}

	@RequiredReadAction
	private String getInitializerText(@Nonnull V v)
	{
		DotNetExpression initializer = v.getInitializer();
		return initializer != null ? initializer.getText() : null;
	}

	protected boolean supportsParentQName()
	{
		return false;
	}

	@Nullable
	@RequiredReadAction
	protected String getParentQName(@Nonnull V variable)
	{
		return null;
	}

	protected boolean supportsInitializer(int modifiers)
	{
		return false;
	}
}
