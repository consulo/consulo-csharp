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

import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.language.psi.stub.StubElement;

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 01.03.14
 */
public class CSharpIndexMethodDeclStub extends MemberStub<CSharpIndexMethodDeclaration>
{
	public static final int AUTO_GET = 1 << 1;

	public CSharpIndexMethodDeclStub(StubElement parent, @Nullable String qname, int otherModifiers)
	{
		super(parent, CSharpStubElements.INDEX_METHOD_DECLARATION, qname, otherModifiers);
	}
}
