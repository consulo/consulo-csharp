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

package consulo.csharp.lang.impl.psi.stub;

import consulo.csharp.lang.psi.CSharpNamespaceProvider;
import consulo.index.io.StringRef;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubBase;
import consulo.language.psi.stub.StubElement;

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public class CSharpNamespaceProviderStub<N extends CSharpNamespaceProvider> extends StubBase<N>
{
	private final String myReferenceTextRef;

	public CSharpNamespaceProviderStub(StubElement parent, IStubElementType elementType, @Nullable StringRef referenceTextRef)
	{
		super(parent, elementType);
		myReferenceTextRef = StringRef.toString(referenceTextRef);
	}

	public CSharpNamespaceProviderStub(final StubElement parent, final IStubElementType elementType, @Nullable final String referenceTextRef)
	{
		super(parent, elementType);
		myReferenceTextRef = referenceTextRef;
	}

	@Nullable
	public String getReferenceTextRef()
	{
		return myReferenceTextRef;
	}
}
