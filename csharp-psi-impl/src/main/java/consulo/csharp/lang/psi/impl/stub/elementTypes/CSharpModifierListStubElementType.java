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
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.impl.source.CSharpStubModifierListImpl;
import consulo.csharp.lang.psi.impl.stub.CSharpModifierListStub;
import consulo.dotnet.psi.DotNetModifierList;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

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

	@NotNull
	@Override
	public PsiElement createElement(@NotNull ASTNode astNode)
	{
		return new CSharpStubModifierListImpl(astNode);
	}

	@Override
	public DotNetModifierList createPsi(@NotNull CSharpModifierListStub stub)
	{
		return new CSharpStubModifierListImpl(stub, this);
	}

	@RequiredReadAction
	@Override
	public CSharpModifierListStub createStub(@NotNull DotNetModifierList modifierList, StubElement stubElement)
	{
		int modifierMask = CSharpModifierListStub.getModifierMask(modifierList);
		return new CSharpModifierListStub(stubElement, this, modifierMask);
	}

	@Override
	public void serialize(@NotNull CSharpModifierListStub stub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeInt(stub.getModifierMask());
	}

	@NotNull
	@Override
	public CSharpModifierListStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		int modifierMask = stubInputStream.readInt();
		return new CSharpModifierListStub(stubElement, this, modifierMask);
	}
}