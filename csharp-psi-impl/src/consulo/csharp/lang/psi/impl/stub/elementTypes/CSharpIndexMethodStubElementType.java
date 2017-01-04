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
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.impl.source.CSharpIndexMethodDeclarationImpl;
import consulo.csharp.lang.psi.impl.stub.CSharpIndexMethodDeclStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;

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

	@NotNull
	@Override
	public CSharpIndexMethodDeclaration createElement(@NotNull ASTNode astNode)
	{
		return new CSharpIndexMethodDeclarationImpl(astNode);
	}

	@Override
	public CSharpIndexMethodDeclaration createPsi(@NotNull CSharpIndexMethodDeclStub methodStub)
	{
		return new CSharpIndexMethodDeclarationImpl(methodStub);
	}

	@RequiredReadAction
	@Override
	public CSharpIndexMethodDeclStub createStub(@NotNull CSharpIndexMethodDeclaration declaration, StubElement stubElement)
	{
		StringRef parentQName = StringRef.fromNullableString(declaration.getPresentableParentQName());
		return new CSharpIndexMethodDeclStub(stubElement, parentQName);
	}

	@Override
	public void serialize(@NotNull CSharpIndexMethodDeclStub methodStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(methodStub.getParentQName());
	}

	@NotNull
	@Override
	public CSharpIndexMethodDeclStub deserialize(@NotNull StubInputStream inputStream, StubElement stubElement) throws IOException
	{
		StringRef qname = inputStream.readName();
		return new CSharpIndexMethodDeclStub(stubElement, qname);
	}
}
