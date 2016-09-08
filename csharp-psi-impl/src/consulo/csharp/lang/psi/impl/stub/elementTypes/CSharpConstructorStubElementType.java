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

package consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import consulo.csharp.lang.psi.impl.source.CSharpConstructorDeclarationImpl;
import consulo.csharp.lang.psi.impl.stub.CSharpMethodDeclStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 18.12.13.
 */
public class CSharpConstructorStubElementType extends CSharpAbstractStubElementType<CSharpMethodDeclStub, CSharpConstructorDeclarationImpl>
{
	public CSharpConstructorStubElementType()
	{
		super("CONSTRUCTOR_DECLARATION");
	}

	@NotNull
	@Override
	public CSharpConstructorDeclarationImpl createElement(@NotNull ASTNode astNode)
	{
		return new CSharpConstructorDeclarationImpl(astNode);
	}

	@Override
	public CSharpConstructorDeclarationImpl createPsi(@NotNull CSharpMethodDeclStub cSharpTypeStub)
	{
		return new CSharpConstructorDeclarationImpl(cSharpTypeStub, this);
	}

	@Override
	public CSharpMethodDeclStub createStub(@NotNull CSharpConstructorDeclarationImpl methodDeclaration, StubElement stubElement)
	{
		StringRef qname = StringRef.fromNullableString(methodDeclaration.getPresentableParentQName());
		int otherModifierMask = CSharpMethodDeclStub.getOtherModifierMask(methodDeclaration);
		return new CSharpMethodDeclStub(stubElement, this, qname, otherModifierMask, -1);
	}

	@Override
	public void serialize(@NotNull CSharpMethodDeclStub cSharpTypeStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(cSharpTypeStub.getParentQName());
		stubOutputStream.writeVarInt(cSharpTypeStub.getOtherModifierMask());
	}

	@NotNull
	@Override
	public CSharpMethodDeclStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		StringRef qname = stubInputStream.readName();
		int otherModifierMask = stubInputStream.readVarInt();
		return new CSharpMethodDeclStub(stubElement, this, qname, otherModifierMask, -1);
	}
}
