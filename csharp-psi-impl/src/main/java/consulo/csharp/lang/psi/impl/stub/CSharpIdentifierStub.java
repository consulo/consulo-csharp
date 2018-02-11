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

package consulo.csharp.lang.psi.impl.stub;

import javax.annotation.Nullable;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;
import consulo.csharp.lang.psi.CSharpIdentifier;

/**
 * @author VISTALL
 * @since 26.07.2015
 */
public class CSharpIdentifierStub extends StubBase<CSharpIdentifier>
{
	private final String myValue;

	public CSharpIdentifierStub(StubElement parent, IStubElementType elementType, @Nullable StringRef value)
	{
		this(parent, elementType, StringRef.toString(value));
	}

	public CSharpIdentifierStub(final StubElement parent, final IStubElementType elementType, @Nullable final String value)
	{
		super(parent, elementType);
		myValue = value;
	}

	public String getValue()
	{
		return myValue;
	}
}
