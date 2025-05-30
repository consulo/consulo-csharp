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

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.source.CSharpStubModifierListImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpModifierListStub;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubOutputStream;

import jakarta.annotation.Nonnull;
import java.io.IOException;

/**
 * @author VISTALL
 * @since 17.10.14
 */
public class CSharpModifierListStubElementType extends CSharpAbstractStubElementType<CSharpModifierListStub, DotNetModifierList>
{
	public CSharpModifierListStubElementType()
	{
		super("MODIFIER_LIST");
	}

	@Nonnull
	@Override
	public PsiElement createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpStubModifierListImpl(astNode);
	}

	@Override
	public DotNetModifierList createPsi(@Nonnull CSharpModifierListStub stub)
	{
		return new CSharpStubModifierListImpl(stub, this);
	}

	@RequiredReadAction
	@Override
	public CSharpModifierListStub createStub(@Nonnull DotNetModifierList modifierList, StubElement stubElement)
	{
		int modifierMask = CSharpModifierListStub.getModifierMask(modifierList);
		return new CSharpModifierListStub(stubElement, this, modifierMask);
	}

	@Override
	public void serialize(@Nonnull CSharpModifierListStub stub, @Nonnull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeVarInt(stub.getModifierMask());
	}

	@Nonnull
	@Override
	public CSharpModifierListStub deserialize(@Nonnull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		int modifierMask = stubInputStream.readVarInt();
		return new CSharpModifierListStub(stubElement, this, modifierMask);
	}
}
