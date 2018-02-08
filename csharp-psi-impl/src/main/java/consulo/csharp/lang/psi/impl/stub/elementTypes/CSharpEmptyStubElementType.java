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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.EmptyStub;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

/**
 * @author VISTALL
 * @since 19.10.14
 */
public abstract class CSharpEmptyStubElementType<T extends PsiElement> extends CSharpAbstractStubElementType<EmptyStub<T>, T>
{
	public CSharpEmptyStubElementType(@NotNull @NonNls String debugName)
	{
		super(debugName);
	}

	@RequiredReadAction
	@Override
	public EmptyStub<T> createStub(@NotNull T type, StubElement stubElement)
	{
		return new EmptyStub<T>(stubElement, this);
	}

	@Override
	public void serialize(@NotNull EmptyStub cSharpEmptyStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{

	}

	@NotNull
	@Override
	public EmptyStub<T> deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		return new EmptyStub<T>(stubElement, this);
	}
}