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
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpDummyDeclarationImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpDummyDefStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

/**
 * @author VISTALL
 * @since 06.03.14
 */
public class CSharpDummyDefElementType extends CSharpAbstractStubElementType<CSharpDummyDefStub, CSharpDummyDeclarationImpl>
{
	public CSharpDummyDefElementType()
	{
		super("DUMMY_DECLARATION");
	}

	@Override
	public CSharpDummyDeclarationImpl createElement(@NotNull ASTNode astNode)
	{
		return new CSharpDummyDeclarationImpl(astNode);
	}

	@Override
	public CSharpDummyDeclarationImpl createPsi(@NotNull CSharpDummyDefStub cSharpDummyDefStub)
	{
		return new CSharpDummyDeclarationImpl(cSharpDummyDefStub, this);
	}

	@Override
	public CSharpDummyDefStub createStub(@NotNull CSharpDummyDeclarationImpl cSharpDummyDeclaration, StubElement stubElement)
	{
		return new CSharpDummyDefStub(stubElement, this);
	}

	@Override
	public void serialize(@NotNull CSharpDummyDefStub cSharpDummyDefStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{

	}

	@NotNull
	@Override
	public CSharpDummyDefStub deserialize(@NotNull StubInputStream inputStream, StubElement stubElement) throws IOException
	{
		return new CSharpDummyDefStub(stubElement, this);
	}
}
