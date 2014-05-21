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
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpParameterListImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpParameterListStub;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpParameterListStubElementType extends CSharpAbstractStubElementType<CSharpParameterListStub, DotNetParameterList>
{
	public CSharpParameterListStubElementType()
	{
		super("PARAMETER_LIST");
	}

	@Override
	public DotNetParameterList createPsi(@NotNull ASTNode astNode)
	{
		return new CSharpParameterListImpl(astNode);
	}

	@Override
	public DotNetParameterList createPsi(@NotNull CSharpParameterListStub cSharpParameterListStub)
	{
		return new CSharpParameterListImpl(cSharpParameterListStub);
	}

	@Override
	public CSharpParameterListStub createStub(@NotNull DotNetParameterList dotNetParameterList, StubElement stubElement)
	{
		return new CSharpParameterListStub(stubElement, this);
	}

	@Override
	public void serialize(@NotNull CSharpParameterListStub cSharpParameterListStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
	}

	@NotNull
	@Override
	public CSharpParameterListStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		return new CSharpParameterListStub(stubElement, this);
	}
}
