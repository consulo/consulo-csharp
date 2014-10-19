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
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayMethodDeclarationImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpArrayMethodDeclStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.typeStub.CSharpStubTypeInfoUtil;
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
public class CSharpArrayMethodStubElementType extends CSharpAbstractStubElementType<CSharpArrayMethodDeclStub, CSharpArrayMethodDeclaration>
{
	public CSharpArrayMethodStubElementType()
	{
		super("ARRAY_METHOD_DECLARATION");
	}

	@Override
	public CSharpArrayMethodDeclaration createElement(@NotNull ASTNode astNode)
	{
		return new CSharpArrayMethodDeclarationImpl(astNode);
	}

	@Override
	public CSharpArrayMethodDeclaration createPsi(@NotNull CSharpArrayMethodDeclStub methodStub)
	{
		return new CSharpArrayMethodDeclarationImpl(methodStub);
	}

	@Override
	public CSharpArrayMethodDeclStub createStub(@NotNull CSharpArrayMethodDeclaration declaration, StubElement stubElement)
	{
		StringRef name = StringRef.fromNullableString(declaration.getName());
		StringRef parentQName = StringRef.fromNullableString(declaration.getPresentableParentQName());
		val typeInfo = CSharpStubTypeInfoUtil.toStub(declaration.getReturnType());
		val implementInfo = CSharpStubTypeInfoUtil.toStub(declaration.getTypeForImplement());
		return new CSharpArrayMethodDeclStub(stubElement, name, parentQName, typeInfo, implementInfo);
	}

	@Override
	public void serialize(@NotNull CSharpArrayMethodDeclStub methodStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(methodStub.getName());
		stubOutputStream.writeName(methodStub.getParentQName());
		methodStub.getReturnType().writeTo(stubOutputStream);
		methodStub.getImplementType().writeTo(stubOutputStream);
	}

	@NotNull
	@Override
	public CSharpArrayMethodDeclStub deserialize(@NotNull StubInputStream inputStream, StubElement stubElement) throws IOException
	{
		StringRef name = inputStream.readName();
		StringRef qname = inputStream.readName();
		val typeInfo = CSharpStubTypeInfoUtil.read(inputStream);
		val implementInfo = CSharpStubTypeInfoUtil.read(inputStream);
		return new CSharpArrayMethodDeclStub(stubElement, name, qname, typeInfo, implementInfo);
	}
}
