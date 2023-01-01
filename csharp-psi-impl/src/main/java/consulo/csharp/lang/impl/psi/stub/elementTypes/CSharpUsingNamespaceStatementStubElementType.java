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

package consulo.csharp.lang.impl.psi.stub.elementTypes;

import java.io.IOException;

import javax.annotation.Nonnull;

import consulo.csharp.lang.psi.CSharpUsingNamespaceStatement;
import consulo.csharp.lang.impl.psi.source.CSharpUsingNamespaceStatementImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpWithStringValueStub;
import consulo.index.io.StringRef;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.StubElement;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpUsingNamespaceStatementStubElementType extends
		CSharpAbstractStubElementType<CSharpWithStringValueStub<CSharpUsingNamespaceStatement>, CSharpUsingNamespaceStatement>
{
	public CSharpUsingNamespaceStatementStubElementType()
	{
		super("USING_NAMESPACE_STATEMENT");
	}

	@Nonnull
	@Override
	public CSharpUsingNamespaceStatement createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpUsingNamespaceStatementImpl(astNode);
	}

	@Override
	public CSharpUsingNamespaceStatement createPsi(@Nonnull CSharpWithStringValueStub<CSharpUsingNamespaceStatement> stub)
	{
		return new CSharpUsingNamespaceStatementImpl(stub);
	}

	@Override
	public CSharpWithStringValueStub<CSharpUsingNamespaceStatement> createStub(@Nonnull CSharpUsingNamespaceStatement statement,
			StubElement stubElement)
	{
		String referenceText = statement.getReferenceText();
		return new CSharpWithStringValueStub<CSharpUsingNamespaceStatement>(stubElement, this, StringRef.fromNullableString(referenceText));
	}

	@Override
	public void serialize(@Nonnull CSharpWithStringValueStub<CSharpUsingNamespaceStatement> stub,
			@Nonnull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(stub.getReferenceText());
	}

	@Nonnull
	@Override
	public CSharpWithStringValueStub<CSharpUsingNamespaceStatement> deserialize(@Nonnull StubInputStream stubInputStream,
			StubElement stubElement) throws IOException
	{
		StringRef referenceText = stubInputStream.readName();
		return new CSharpWithStringValueStub<CSharpUsingNamespaceStatement>(stubElement, this, referenceText);
	}
}
