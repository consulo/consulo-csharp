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

import org.jetbrains.annotations.NotNull;
import consulo.csharp.lang.psi.CSharpUsingNamespaceStatement;
import consulo.csharp.lang.psi.impl.source.CSharpUsingNamespaceStatementImpl;
import consulo.csharp.lang.psi.impl.stub.CSharpWithStringValueStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;

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

	@NotNull
	@Override
	public CSharpUsingNamespaceStatement createElement(@NotNull ASTNode astNode)
	{
		return new CSharpUsingNamespaceStatementImpl(astNode);
	}

	@Override
	public CSharpUsingNamespaceStatement createPsi(@NotNull CSharpWithStringValueStub<CSharpUsingNamespaceStatement> stub)
	{
		return new CSharpUsingNamespaceStatementImpl(stub);
	}

	@Override
	public CSharpWithStringValueStub<CSharpUsingNamespaceStatement> createStub(@NotNull CSharpUsingNamespaceStatement statement,
			StubElement stubElement)
	{
		String referenceText = statement.getReferenceText();
		return new CSharpWithStringValueStub<CSharpUsingNamespaceStatement>(stubElement, this, StringRef.fromNullableString(referenceText));
	}

	@Override
	public void serialize(@NotNull CSharpWithStringValueStub<CSharpUsingNamespaceStatement> stub,
			@NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(stub.getReferenceText());
	}

	@NotNull
	@Override
	public CSharpWithStringValueStub<CSharpUsingNamespaceStatement> deserialize(@NotNull StubInputStream stubInputStream,
			StubElement stubElement) throws IOException
	{
		StringRef referenceText = stubInputStream.readName();
		return new CSharpWithStringValueStub<CSharpUsingNamespaceStatement>(stubElement, this, referenceText);
	}
}
