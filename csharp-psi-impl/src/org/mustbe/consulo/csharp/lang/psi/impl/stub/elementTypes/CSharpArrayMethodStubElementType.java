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
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayMethodDeclarationImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpArrayMethodStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.MemberStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.typeStub.CSharpStubTypeInfoUtil;
import org.mustbe.consulo.dotnet.psi.DotNetArrayMethodDeclaration;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import lombok.val;

/**
 * @author VISTALL
 * @since 01.03.14
 */
public class CSharpArrayMethodStubElementType extends CSharpAbstractStubElementType<CSharpArrayMethodStub, DotNetArrayMethodDeclaration>
{
	public CSharpArrayMethodStubElementType()
	{
		super("ARRAY_METHOD_DECLARATION");
	}

	@Override
	public DotNetArrayMethodDeclaration createPsi(@NotNull ASTNode astNode)
	{
		return new CSharpArrayMethodDeclarationImpl(astNode);
	}

	@Override
	public DotNetArrayMethodDeclaration createPsi(@NotNull CSharpArrayMethodStub cSharpArrayMethodStub)
	{
		return new CSharpArrayMethodDeclarationImpl(cSharpArrayMethodStub);
	}

	@Override
	public CSharpArrayMethodStub createStub(@NotNull DotNetArrayMethodDeclaration dotNetArrayMethodDeclaration, StubElement stubElement)
	{
		StringRef name = StringRef.fromNullableString(dotNetArrayMethodDeclaration.getName());
		StringRef parentQName = StringRef.fromNullableString(dotNetArrayMethodDeclaration.getPresentableParentQName());
		int modifierMask = MemberStub.getModifierMask(dotNetArrayMethodDeclaration);
		val typeInfo = CSharpStubTypeInfoUtil.toStub(dotNetArrayMethodDeclaration.getReturnType());
		return new CSharpArrayMethodStub(stubElement, name, parentQName, modifierMask, typeInfo);

	}

	@Override
	public void serialize(@NotNull CSharpArrayMethodStub cSharpArrayMethodStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(cSharpArrayMethodStub.getName());
		stubOutputStream.writeName(cSharpArrayMethodStub.getParentQName());
		stubOutputStream.writeInt(cSharpArrayMethodStub.getModifierMask());
		cSharpArrayMethodStub.getReturnType().writeTo(stubOutputStream);

	}

	@NotNull
	@Override
	public CSharpArrayMethodStub deserialize(@NotNull StubInputStream inputStream, StubElement stubElement) throws IOException
	{
		StringRef name = inputStream.readName();
		StringRef qname = inputStream.readName();
		int modifierMask = inputStream.readInt();
		val typeInfo = CSharpStubTypeInfoUtil.read(inputStream);
		return new CSharpArrayMethodStub(stubElement, name, qname, modifierMask, typeInfo);

	}
}
