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
import consulo.csharp.lang.impl.psi.source.CSharpUsingNamespaceStatementImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpUsingNamespaceStub;
import consulo.csharp.lang.impl.psi.stub.index.CSharpIndexKeys;
import consulo.csharp.lang.psi.CSharpUsingNamespaceStatement;
import consulo.index.io.StringRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.IndexSink;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpUsingNamespaceStatementStubElementType extends CSharpAbstractStubElementType<CSharpUsingNamespaceStub, CSharpUsingNamespaceStatement>
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
	public CSharpUsingNamespaceStatement createPsi(@Nonnull CSharpUsingNamespaceStub stub)
	{
		return new CSharpUsingNamespaceStatementImpl(stub);
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public CSharpUsingNamespaceStub createStub(@Nonnull CSharpUsingNamespaceStatement statement, StubElement stubElement)
	{
		String referenceText = statement.getReferenceText();
		boolean global = statement.isGlobal();
		return new CSharpUsingNamespaceStub(stubElement, this, StringRef.fromNullableString(referenceText), global);
	}

	@Override
	public void serialize(@Nonnull CSharpUsingNamespaceStub stub, @Nonnull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(stub.getReferenceText());
		stubOutputStream.writeBoolean(stub.isGlobal());
	}

	@Nonnull
	@Override
	public CSharpUsingNamespaceStub deserialize(@Nonnull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		StringRef referenceText = stubInputStream.readName();
		boolean global = stubInputStream.readBoolean();
		return new CSharpUsingNamespaceStub(stubElement, this, referenceText, global);
	}

	@Override
	public void indexStub(@Nonnull CSharpUsingNamespaceStub stub, @Nonnull IndexSink indexSink)
	{
		String referenceText = stub.getReferenceText();
		if(stub.isGlobal() && !StringUtil.isEmptyOrSpaces(referenceText))
		{
			indexSink.occurrence(CSharpIndexKeys.GLOBAL_USING_NAMESPACE, referenceText);
		}
	}
}
