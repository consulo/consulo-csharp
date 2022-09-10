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
import consulo.csharp.lang.psi.CSharpIdentifier;
import consulo.csharp.lang.impl.psi.source.CSharpStubIdentifierImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpIdentifierStub;
import consulo.index.io.StringRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author VISTALL
 * @since 26.07.2015
 */
public class CSharpIdentifierStubElementType extends CSharpAbstractStubElementType<CSharpIdentifierStub, CSharpIdentifier>
{
	public CSharpIdentifierStubElementType()
	{
		super("IDENTIFIER");
	}

	@Nonnull
	@Override
	public PsiElement createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpStubIdentifierImpl(astNode);
	}

	@Override
	public CSharpIdentifier createPsi(@Nonnull CSharpIdentifierStub stub)
	{
		return new CSharpStubIdentifierImpl(stub, this);
	}

	@RequiredReadAction
	@Override
	public CSharpIdentifierStub createStub(@Nonnull CSharpIdentifier psi, StubElement parentStub)
	{
		String value = psi.getValue();
		return new CSharpIdentifierStub(parentStub, this, value);
	}

	@Override
	public void serialize(@Nonnull CSharpIdentifierStub stub, @Nonnull StubOutputStream dataStream) throws IOException
	{
		dataStream.writeName(stub.getValue());
	}

	@Nonnull
	@Override
	public CSharpIdentifierStub deserialize(@Nonnull StubInputStream dataStream, StubElement parentStub) throws IOException
	{
		StringRef nameRef = dataStream.readName();
		return new CSharpIdentifierStub(parentStub, this, nameRef);
	}
}
