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
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.impl.source.CSharpConversionMethodDeclarationImpl;
import consulo.csharp.lang.psi.impl.stub.CSharpMethodDeclStub;

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

	@NotNull
	@Override
	public CSharpConversionMethodDeclarationImpl createElement(@NotNull ASTNode astNode)
	{
		return new CSharpConversionMethodDeclarationImpl(astNode);
	}

	@Override
	public CSharpConversionMethodDeclarationImpl createPsi(@NotNull CSharpMethodDeclStub cSharpTypeStub)
	{
		return new CSharpConversionMethodDeclarationImpl(cSharpTypeStub);
	}

	@RequiredReadAction
	@Override
	public CSharpMethodDeclStub createStub(@NotNull CSharpConversionMethodDeclarationImpl methodDeclaration, StubElement stubElement)
	{
		String qname = methodDeclaration.getPresentableParentQName();
		return new CSharpMethodDeclStub(stubElement, this, qname, 0, -1);
	}

	@Override
	public void serialize(@NotNull CSharpMethodDeclStub cSharpTypeStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(cSharpTypeStub.getParentQName());
	}

	@NotNull
	@Override
	public CSharpMethodDeclStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		StringRef qname = stubInputStream.readName();
		return new CSharpMethodDeclStub(stubElement, this, StringRef.toString(qname), 0, -1);
	}
}
