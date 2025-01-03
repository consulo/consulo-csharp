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

import consulo.csharp.lang.psi.CSharpAttribute;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.impl.psi.source.CSharpStubAttributeImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpWithStringValueStub;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.StubElement;
import consulo.index.io.StringRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 17.10.14
 */
public class CSharpAttributeStubElementType extends CSharpAbstractStubElementType<CSharpWithStringValueStub<CSharpAttribute>, CSharpAttribute>
{
	public CSharpAttributeStubElementType()
	{
		super("ATTRIBUTE");
	}

	@Nonnull
	@Override
	public PsiElement createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpStubAttributeImpl(astNode);
	}

	@Override
	public CSharpAttribute createPsi(@Nonnull CSharpWithStringValueStub<CSharpAttribute> stub)
	{
		return new CSharpStubAttributeImpl(stub, this);
	}

	@Override
	public CSharpWithStringValueStub<CSharpAttribute> createStub(@Nonnull CSharpAttribute attribute, StubElement stubElement)
	{
		CSharpReferenceExpression referenceExpression = attribute.getReferenceExpression();
		String referenceText = referenceExpression == null ? null : referenceExpression.getText();
		return new CSharpWithStringValueStub<CSharpAttribute>(stubElement, this, referenceText);
	}

	@Override
	public void serialize(@Nonnull CSharpWithStringValueStub stub, @Nonnull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(stub.getReferenceText());
	}

	@Nonnull
	@Override
	public CSharpWithStringValueStub<CSharpAttribute> deserialize(@Nonnull StubInputStream stubInputStream,
			StubElement stubElement) throws IOException
	{
		StringRef referenceText = stubInputStream.readName();
		return new CSharpWithStringValueStub<CSharpAttribute>(stubElement, this, referenceText);
	}
}
