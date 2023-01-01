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

import consulo.csharp.lang.impl.psi.source.CSharpDummyDeclarationImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpDummyDeclStub;
import consulo.language.psi.stub.StubElement;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;

/**
 * @author VISTALL
 * @since 06.03.14
 */
public class CSharpDummyDefElementType extends CSharpAbstractStubElementType<CSharpDummyDeclStub, CSharpDummyDeclarationImpl>
{
	public CSharpDummyDefElementType()
	{
		super("DUMMY_DECLARATION");
	}

	@Nonnull
	@Override
	public CSharpDummyDeclarationImpl createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpDummyDeclarationImpl(astNode);
	}

	@Override
	public CSharpDummyDeclarationImpl createPsi(@Nonnull CSharpDummyDeclStub cSharpDummyDeclStub)
	{
		return new CSharpDummyDeclarationImpl(cSharpDummyDeclStub, this);
	}

	@Override
	public CSharpDummyDeclStub createStub(@Nonnull CSharpDummyDeclarationImpl cSharpDummyDeclaration, StubElement stubElement)
	{
		return new CSharpDummyDeclStub(stubElement, this);
	}

	@Override
	public void serialize(@Nonnull CSharpDummyDeclStub cSharpDummyDeclStub, @Nonnull StubOutputStream stubOutputStream) throws IOException
	{

	}

	@Nonnull
	@Override
	public CSharpDummyDeclStub deserialize(@Nonnull StubInputStream inputStream, StubElement stubElement) throws IOException
	{
		return new CSharpDummyDeclStub(stubElement, this);
	}
}
