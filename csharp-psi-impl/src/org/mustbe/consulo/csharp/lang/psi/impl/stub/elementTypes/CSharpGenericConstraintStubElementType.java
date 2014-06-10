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

package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraint;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpGenericConstraintImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpGenericConstraintStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 10.06.14
 */
public class CSharpGenericConstraintStubElementType extends CSharpAbstractStubElementType<CSharpGenericConstraintStub, CSharpGenericConstraint>
{
	public CSharpGenericConstraintStubElementType()
	{
		super("GENERIC_CONSTRAINT");
	}

	@Override
	public CSharpGenericConstraint createPsi(@NotNull ASTNode astNode)
	{
		return new CSharpGenericConstraintImpl(astNode);
	}

	@Override
	public CSharpGenericConstraint createPsi(@NotNull CSharpGenericConstraintStub cSharpGenericConstraintStub)
	{
		return new CSharpGenericConstraintImpl(cSharpGenericConstraintStub, this);
	}

	@Override
	public CSharpGenericConstraintStub createStub(
			@NotNull CSharpGenericConstraint cSharpGenericConstraint, StubElement stubElement)
	{
		CSharpReferenceExpression genericParameterReference = cSharpGenericConstraint.getGenericParameterReference();
		String text = genericParameterReference == null ? null : genericParameterReference.getText();
		return new CSharpGenericConstraintStub(stubElement, this, text);
	}

	@Override
	public void serialize(
			@NotNull CSharpGenericConstraintStub cSharpGenericConstraintStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(cSharpGenericConstraintStub.getReferenceText());
	}

	@NotNull
	@Override
	public CSharpGenericConstraintStub deserialize(
			@NotNull StubInputStream inputStream, StubElement stubElement) throws IOException
	{
		StringRef text = inputStream.readName();
		return new CSharpGenericConstraintStub(stubElement, this, text);
	}
}
