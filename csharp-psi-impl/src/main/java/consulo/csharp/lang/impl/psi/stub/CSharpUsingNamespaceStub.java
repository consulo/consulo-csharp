/*
 * Copyright 2013-2023 consulo.io
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

package consulo.csharp.lang.impl.psi.stub;

import consulo.csharp.lang.impl.psi.source.CSharpUsingNamespaceStatementImpl;
import consulo.index.io.StringRef;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubBase;
import consulo.language.psi.stub.StubElement;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2023-12-30
 */
public class CSharpUsingNamespaceStub extends StubBase<CSharpUsingNamespaceStatementImpl>
{
	private final String myReferenceText;
	private final boolean myGlobal;

	public CSharpUsingNamespaceStub(StubElement parent, IStubElementType elementType, StringRef referenceText, boolean global)
	{
		super(parent, elementType);
		myReferenceText = StringRef.toString(referenceText);
		myGlobal = global;
	}

	public CSharpUsingNamespaceStub(StubElement parent, IStubElementType elementType, String referenceText, boolean global)
	{
		super(parent, elementType);
		myReferenceText = referenceText;
		myGlobal = global;
	}

	@Nullable
	public String getReferenceText()
	{
		return myReferenceText;
	}

	public boolean isGlobal()
	{
		return myGlobal;
	}
}

