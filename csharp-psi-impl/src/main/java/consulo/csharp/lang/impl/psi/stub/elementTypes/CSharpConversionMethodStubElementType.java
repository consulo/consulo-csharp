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

import jakarta.annotation.Nonnull;

import consulo.index.io.StringRef;
import consulo.language.psi.stub.StubElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.source.CSharpConversionMethodDeclarationImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpMethodDeclStub;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;

/**
 * @author VISTALL
 * @since 09.01.14
 */
public class CSharpConversionMethodStubElementType extends CSharpAbstractStubElementType<CSharpMethodDeclStub, CSharpConversionMethodDeclarationImpl>
{
	public CSharpConversionMethodStubElementType()
	{
		super("CONVERSION_METHOD_DECLARATION");
	}

	@Nonnull
	@Override
	public CSharpConversionMethodDeclarationImpl createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpConversionMethodDeclarationImpl(astNode);
	}

	@Override
	public CSharpConversionMethodDeclarationImpl createPsi(@Nonnull CSharpMethodDeclStub cSharpTypeStub)
	{
		return new CSharpConversionMethodDeclarationImpl(cSharpTypeStub);
	}

	@RequiredReadAction
	@Override
	public CSharpMethodDeclStub createStub(@Nonnull CSharpConversionMethodDeclarationImpl methodDeclaration, StubElement stubElement)
	{
		String qname = methodDeclaration.getPresentableParentQName();
		return new CSharpMethodDeclStub(stubElement, this, qname, 0, -1);
	}

	@Override
	public void serialize(@Nonnull CSharpMethodDeclStub cSharpTypeStub, @Nonnull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(cSharpTypeStub.getParentQName());
	}

	@Nonnull
	@Override
	public CSharpMethodDeclStub deserialize(@Nonnull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		StringRef qname = stubInputStream.readName();
		return new CSharpMethodDeclStub(stubElement, this, StringRef.toString(qname), 0, -1);
	}
}
