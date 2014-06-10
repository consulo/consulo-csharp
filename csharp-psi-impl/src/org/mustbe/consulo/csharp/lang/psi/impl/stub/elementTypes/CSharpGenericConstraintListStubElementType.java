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
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintList;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpGenericConstraintListImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpGenericConstraintListStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

/**
 * @author VISTALL
 * @since 10.06.14
 */
public class CSharpGenericConstraintListStubElementType extends CSharpAbstractStubElementType<CSharpGenericConstraintListStub, CSharpGenericConstraintList>
{
	public CSharpGenericConstraintListStubElementType()
	{
		super("GENERIC_CONSTRAINT_LIST");
	}

	@Override
	public CSharpGenericConstraintList createPsi(@NotNull ASTNode astNode)
	{
		return new CSharpGenericConstraintListImpl(astNode);
	}

	@Override
	public CSharpGenericConstraintList createPsi(@NotNull CSharpGenericConstraintListStub cSharpGenericConstraintListStub)
	{
		return new CSharpGenericConstraintListImpl(cSharpGenericConstraintListStub, this);
	}

	@Override
	public CSharpGenericConstraintListStub createStub(
			@NotNull CSharpGenericConstraintList cSharpGenericConstraintList, StubElement stubElement)
	{
		return new CSharpGenericConstraintListStub(stubElement, this);
	}

	@Override
	public void serialize(
			@NotNull CSharpGenericConstraintListStub cSharpGenericConstraintListStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{

	}

	@NotNull
	@Override
	public CSharpGenericConstraintListStub deserialize(
			@NotNull StubInputStream inputStream, StubElement stubElement) throws IOException
	{
		return new CSharpGenericConstraintListStub(stubElement, this);
	}
}
