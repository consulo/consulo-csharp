/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpUsingNamespaceStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpUsingNamespaceStatementStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpUsingNamespaceStatementStubElementType extends CSharpAbstractStubElementType<CSharpUsingNamespaceStatementStub,
		CSharpUsingNamespaceStatementImpl>
{
	public CSharpUsingNamespaceStatementStubElementType()
	{
		super("USING_NAMESPACE_STATEMENT");
	}

	@Override
	public CSharpUsingNamespaceStatementImpl createElement(@NotNull ASTNode astNode)
	{
		return new CSharpUsingNamespaceStatementImpl(astNode);
	}

	@Override
	public CSharpUsingNamespaceStatementImpl createPsi(@NotNull CSharpUsingNamespaceStatementStub cSharpUsingNamespaceStatementStub)
	{
		return new CSharpUsingNamespaceStatementImpl(cSharpUsingNamespaceStatementStub);
	}

	@Override
	public CSharpUsingNamespaceStatementStub createStub(@NotNull CSharpUsingNamespaceStatementImpl cSharpUsingNamespaceStatement,
			StubElement stubElement)
	{
		String referenceText = cSharpUsingNamespaceStatement.getReferenceText();
		return new CSharpUsingNamespaceStatementStub(stubElement, this, StringRef.fromNullableString(referenceText));
	}

	@Override
	public void serialize(@NotNull CSharpUsingNamespaceStatementStub stub, @NotNull StubOutputStream stubOutputStream)
			throws IOException
	{
		stubOutputStream.writeName(stub.getReferenceText());
	}

	@NotNull
	@Override
	public CSharpUsingNamespaceStatementStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		StringRef referenceText = stubInputStream.readName();
		return new CSharpUsingNamespaceStatementStub(stubElement, this, referenceText);
	}
}
