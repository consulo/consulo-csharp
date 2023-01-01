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

import javax.annotation.Nonnull;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.source.CSharpGenericParameterImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpGenericParameterStub;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpGenericParameterStubElementType extends CSharpAbstractStubElementType<CSharpGenericParameterStub, DotNetGenericParameter>
{
	public CSharpGenericParameterStubElementType()
	{
		super("GENERIC_PARAMETER");
	}

	@Nonnull
	@Override
	public DotNetGenericParameter createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpGenericParameterImpl(astNode);
	}

	@Override
	public DotNetGenericParameter createPsi(@Nonnull CSharpGenericParameterStub cSharpGenericParameterStub)
	{
		return new CSharpGenericParameterImpl(cSharpGenericParameterStub);
	}

	@RequiredReadAction
	@Override
	public CSharpGenericParameterStub createStub(@Nonnull DotNetGenericParameter genericParameter, StubElement stubElement)
	{
		return new CSharpGenericParameterStub(stubElement);
	}

	@Override
	public void serialize(@Nonnull CSharpGenericParameterStub cSharpGenericParameterStub, @Nonnull StubOutputStream stubOutputStream) throws IOException
	{
	}

	@Nonnull
	@Override
	public CSharpGenericParameterStub deserialize(@Nonnull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		return new CSharpGenericParameterStub(stubElement);
	}
}
