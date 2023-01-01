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

import consulo.csharp.lang.impl.psi.source.CSharpDummyDeclarationImpl;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubElement;

/**
 * @author VISTALL
 * @since 06.03.14
 */
public class CSharpDummyDeclStub extends MemberStub<CSharpDummyDeclarationImpl>
{
	public CSharpDummyDeclStub(StubElement parent, IStubElementType elementType)
	{
		super(parent, elementType, null, 0);
	}
}
