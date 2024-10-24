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

import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.impl.psi.source.CSharpGenericConstraintImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpWithStringValueStub;
import consulo.language.psi.stub.StubOutputStream;
import consulo.index.io.StringRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;

/**
 * @author VISTALL
 * @since 10.06.14
 */
public class CSharpGenericConstraintStubElementType extends CSharpAbstractStubElementType<CSharpWithStringValueStub<CSharpGenericConstraint>,
		CSharpGenericConstraint>
{
	public CSharpGenericConstraintStubElementType()
	{
		super("GENERIC_CONSTRAINT");
	}

	@Nonnull
	@Override
	public CSharpGenericConstraint createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpGenericConstraintImpl(astNode);
	}

	@Override
	public CSharpGenericConstraint createPsi(@Nonnull CSharpWithStringValueStub<CSharpGenericConstraint> stub)
	{
		return new CSharpGenericConstraintImpl(stub, this);
	}

	@Override
	public CSharpWithStringValueStub<CSharpGenericConstraint> createStub(@Nonnull CSharpGenericConstraint constraint, StubElement stubElement)
	{
		CSharpReferenceExpression genericParameterReference = constraint.getGenericParameterReference();
		String text = genericParameterReference == null ? null : genericParameterReference.getText();
		return new CSharpWithStringValueStub<CSharpGenericConstraint>(stubElement, this, text);
	}

	@Override
	public void serialize(@Nonnull CSharpWithStringValueStub<CSharpGenericConstraint> stub,
			@Nonnull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(stub.getReferenceText());
	}

	@Nonnull
	@Override
	public CSharpWithStringValueStub<CSharpGenericConstraint> deserialize(@Nonnull StubInputStream inputStream,
			StubElement stubElement) throws IOException
	{
		StringRef text = inputStream.readName();
		return new CSharpWithStringValueStub<CSharpGenericConstraint>(stubElement, this, text);
	}
}
