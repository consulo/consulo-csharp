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
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeDefStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpTypeDefStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.typeStub.CSharpStubTypeInfo;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.typeStub.CSharpStubTypeInfoUtil;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 11.02.14
 */
public class CSharpTypeDefStubElementType extends CSharpAbstractStubElementType<CSharpTypeDefStub, CSharpTypeDefStatementImpl>
{
	public CSharpTypeDefStubElementType()
	{
		super("TYPE_DEF");
	}

	@Override
	public CSharpTypeDefStatementImpl createElement(@NotNull ASTNode astNode)
	{
		return new CSharpTypeDefStatementImpl(astNode);
	}

	@Override
	public CSharpTypeDefStatementImpl createPsi(@NotNull CSharpTypeDefStub cSharpTypeDefStub)
	{
		return new CSharpTypeDefStatementImpl(cSharpTypeDefStub);
	}

	@Override
	public CSharpTypeDefStub createStub(@NotNull CSharpTypeDefStatementImpl cSharpTypeDefStatement, StubElement stubElement)
	{
		return new CSharpTypeDefStub(stubElement, this, cSharpTypeDefStatement.getName(), CSharpStubTypeInfoUtil.toStub(cSharpTypeDefStatement
				.getType()));
	}

	@Override
	public void serialize(@NotNull CSharpTypeDefStub cSharpTypeDefStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(cSharpTypeDefStub.getName());
		cSharpTypeDefStub.getTypeInfo().writeTo(stubOutputStream);
	}

	@NotNull
	@Override
	public CSharpTypeDefStub deserialize(@NotNull StubInputStream inputStream, StubElement stubElement) throws IOException
	{
		StringRef ref = inputStream.readName();
		CSharpStubTypeInfo typeInfo = CSharpStubTypeInfoUtil.read(inputStream);
		return new CSharpTypeDefStub(stubElement, this, ref, typeInfo);
	}
}
