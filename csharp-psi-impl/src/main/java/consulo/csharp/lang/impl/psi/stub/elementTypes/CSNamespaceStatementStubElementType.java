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

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.source.CSharpNamespaceStatementImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpNamespaceProviderStub;
import consulo.index.io.StringRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author VISTALL
 * @since 30.12.2023
 */
public class CSNamespaceStatementStubElementType extends CSharpAbstractStubElementType<CSharpNamespaceProviderStub<CSharpNamespaceStatementImpl>, CSharpNamespaceStatementImpl>
{
	public CSNamespaceStatementStubElementType()
	{
		super("NAMESPACE_STATEMENT");
	}

	@Override
	public CSharpNamespaceStatementImpl createPsi(@Nonnull CSharpNamespaceProviderStub<CSharpNamespaceStatementImpl> stub)
	{
		return new CSharpNamespaceStatementImpl(stub, this);
	}

	@Nonnull
	@Override
	public CSharpNamespaceStatementImpl createElement(@Nonnull ASTNode node)
	{
		return new CSharpNamespaceStatementImpl(node);
	}

	@Nonnull
	@RequiredReadAction
	@Override
	public CSharpNamespaceProviderStub<CSharpNamespaceStatementImpl> createStub(@Nonnull CSharpNamespaceStatementImpl statement, StubElement stubElement)
	{
		String referenceText = statement.getReferenceText();
		return new CSharpNamespaceProviderStub<>(stubElement, this, referenceText);
	}

	@Override
	public void serialize(@Nonnull CSharpNamespaceProviderStub<CSharpNamespaceStatementImpl> namespaceStub, @Nonnull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(namespaceStub.getReferenceTextRef());
	}

	@Nonnull
	@Override
	public CSharpNamespaceProviderStub<CSharpNamespaceStatementImpl> deserialize(@Nonnull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		StringRef referenceTextRef = stubInputStream.readName();
		return new CSharpNamespaceProviderStub<>(stubElement, this, referenceTextRef);
	}
}
