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

import javax.annotation.Nonnull;

import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.impl.source.CSharpNamespaceDeclarationImpl;
import consulo.csharp.lang.psi.impl.stub.CSharpNamespaceDeclStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public class CSharpNamespaceStubElementType extends CSharpAbstractStubElementType<CSharpNamespaceDeclStub, CSharpNamespaceDeclarationImpl>
{
	public CSharpNamespaceStubElementType()
	{
		super("NAMESPACE_DECLARATION");
	}

	@Override
	public CSharpNamespaceDeclarationImpl createPsi(@Nonnull CSharpNamespaceDeclStub cSharpNamespaceStub)
	{
		return new CSharpNamespaceDeclarationImpl(cSharpNamespaceStub);
	}

	@Nonnull
	@Override
	public CSharpNamespaceDeclarationImpl createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpNamespaceDeclarationImpl(astNode);
	}

	@RequiredReadAction
	@Override
	public CSharpNamespaceDeclStub createStub(@Nonnull CSharpNamespaceDeclarationImpl declaration, StubElement stubElement)
	{
		String referenceText = declaration.getReferenceText();
		return new CSharpNamespaceDeclStub(stubElement, this, referenceText);
	}

	@Override
	public void serialize(@Nonnull CSharpNamespaceDeclStub namespaceStub, @Nonnull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(namespaceStub.getReferenceTextRef());
	}

	@Nonnull
	@Override
	public CSharpNamespaceDeclStub deserialize(@Nonnull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		StringRef referenceTextRef = stubInputStream.readName();
		return new CSharpNamespaceDeclStub(stubElement, this, referenceTextRef);
	}
}
