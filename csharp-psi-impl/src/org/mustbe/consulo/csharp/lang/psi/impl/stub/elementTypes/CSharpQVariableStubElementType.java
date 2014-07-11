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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpVariableStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.MemberStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.typeStub.CSharpStubTypeInfoUtil;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.psi.DotNetVirtualImplementOwner;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import lombok.val;

/**
 * @author VISTALL
 * @since 07.01.14.
 */
public abstract class CSharpQVariableStubElementType<P extends DotNetVariable & DotNetQualifiedElement> extends
		CSharpAbstractStubElementType<CSharpVariableStub<P>, P>
{
	public CSharpQVariableStubElementType(@NotNull @NonNls String debugName)
	{
		super(debugName);
	}

	@Override
	public CSharpVariableStub<P> createStub(@NotNull P declaration, StubElement stubElement)
	{
		StringRef name = StringRef.fromNullableString(declaration.getName());
		StringRef namespaceQName = StringRef.fromNullableString(declaration.getPresentableParentQName());
		int modifierMask = MemberStub.getModifierMask(declaration);
		boolean constant = declaration.isConstant();
		val typeInfo = CSharpStubTypeInfoUtil.toStub(declaration.getType());
		val typeImplementInfo = CSharpStubTypeInfoUtil.toStub(declaration instanceof DotNetVirtualImplementOwner ? null : declaration.getType());
		return new CSharpVariableStub<P>(stubElement, this, name, namespaceQName, modifierMask, constant, typeInfo, typeImplementInfo);
	}

	@Override
	public void serialize(@NotNull CSharpVariableStub<P> variableStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(variableStub.getName());
		stubOutputStream.writeName(variableStub.getParentQName());
		stubOutputStream.writeInt(variableStub.getModifierMask());
		stubOutputStream.writeBoolean(variableStub.isConstant());
		variableStub.getTypeInfo().writeTo(stubOutputStream);
		variableStub.getImplementType().writeTo(stubOutputStream);
	}

	@NotNull
	@Override
	public CSharpVariableStub<P> deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		StringRef name = stubInputStream.readName();
		StringRef parentQName = stubInputStream.readName();
		int modifierMask = stubInputStream.readInt();
		boolean constant = stubInputStream.readBoolean();
		val typeInfo = CSharpStubTypeInfoUtil.read(stubInputStream);
		val typeImplementInfo = CSharpStubTypeInfoUtil.read(stubInputStream);
		return new CSharpVariableStub<P>(stubElement, this, name, parentQName, modifierMask, constant, typeInfo, typeImplementInfo);
	}
}
