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

import consulo.index.io.StringRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.impl.psi.source.CSharpIndexMethodDeclarationImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpIndexMethodDeclStub;
import consulo.language.psi.stub.StubOutputStream;
import consulo.util.lang.BitUtil;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author VISTALL
 * @since 01.03.14
 */
public class CSharpIndexMethodStubElementType extends CSharpAbstractStubElementType<CSharpIndexMethodDeclStub, CSharpIndexMethodDeclaration>
{
	public CSharpIndexMethodStubElementType()
	{
		super("INDEX_METHOD_DECLARATION");
	}

	@Nonnull
	@Override
	public CSharpIndexMethodDeclaration createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpIndexMethodDeclarationImpl(astNode);
	}

	@Override
	public CSharpIndexMethodDeclaration createPsi(@Nonnull CSharpIndexMethodDeclStub methodStub)
	{
		return new CSharpIndexMethodDeclarationImpl(methodStub);
	}

	@Nonnull
	@RequiredReadAction
	@Override
	public CSharpIndexMethodDeclStub createStub(@Nonnull CSharpIndexMethodDeclaration declaration, StubElement stubElement)
	{
		String parentQName = declaration.getPresentableParentQName();
		int otherModifiers = 0;
		otherModifiers = BitUtil.set(otherModifiers, CSharpIndexMethodDeclStub.AUTO_GET, declaration.isAutoGet());
		return new CSharpIndexMethodDeclStub(stubElement, parentQName, otherModifiers);
	}

	@Override
	public void serialize(@Nonnull CSharpIndexMethodDeclStub methodStub, @Nonnull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(methodStub.getParentQName());
		stubOutputStream.writeVarInt(methodStub.getOtherModifierMask());
	}

	@Nonnull
	@Override
	public CSharpIndexMethodDeclStub deserialize(@Nonnull StubInputStream inputStream, StubElement stubElement) throws IOException
	{
		StringRef qname = inputStream.readName();
		int otherModifiers = inputStream.readVarInt();
		return new CSharpIndexMethodDeclStub(stubElement, StringRef.toString(qname), otherModifiers);
	}
}
