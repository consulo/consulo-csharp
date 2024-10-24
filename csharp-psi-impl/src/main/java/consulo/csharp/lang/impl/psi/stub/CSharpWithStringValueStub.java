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

import consulo.index.io.StringRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubBase;
import consulo.language.psi.stub.StubElement;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 17.10.14
 */
public class CSharpWithStringValueStub<T extends PsiElement> extends StubBase<T>
{
	private final String myReferenceText;

	public CSharpWithStringValueStub(StubElement parent, IStubElementType elementType, StringRef referenceText)
	{
		super(parent, elementType);
		myReferenceText = StringRef.toString(referenceText);
	}

	public CSharpWithStringValueStub(StubElement parent, IStubElementType elementType, String referenceText)
	{
		super(parent, elementType);
		myReferenceText = referenceText;
	}

	@Nullable
	public String getReferenceText()
	{
		return myReferenceText;
	}
}
