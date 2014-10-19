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
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpConversionMethodDeclarationImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpConversionMethodDeclStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.typeStub.CSharpStubTypeInfoUtil;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import lombok.val;

/**
 * @author VISTALL
 * @since 09.01.14
 */
public class CSharpConversionMethodStubElementType extends CSharpAbstractStubElementType<CSharpConversionMethodDeclStub,
		CSharpConversionMethodDeclarationImpl>
{
	public CSharpConversionMethodStubElementType()
	{
		super("CONVERSION_METHOD_DECLARATION");
	}

	@Override
	public CSharpConversionMethodDeclarationImpl createElement(@NotNull ASTNode astNode)
	{
		return new CSharpConversionMethodDeclarationImpl(astNode);
	}

	@Override
	public CSharpConversionMethodDeclarationImpl createPsi(@NotNull CSharpConversionMethodDeclStub cSharpTypeStub)
	{
		return new CSharpConversionMethodDeclarationImpl(cSharpTypeStub);
	}

	@Override
	public CSharpConversionMethodDeclStub createStub(@NotNull CSharpConversionMethodDeclarationImpl methodDeclaration, StubElement stubElement)
	{
		StringRef name = StringRef.fromNullableString(methodDeclaration.getName());
		StringRef qname = StringRef.fromNullableString(methodDeclaration.getPresentableParentQName());
		val typeInfo = CSharpStubTypeInfoUtil.toStub(methodDeclaration.getReturnType());
		val conversionType = CSharpStubTypeInfoUtil.toStub(methodDeclaration.getConversionType());
		return new CSharpConversionMethodDeclStub(stubElement, name, qname, 0, typeInfo, conversionType);
	}

	@Override
	public void serialize(@NotNull CSharpConversionMethodDeclStub cSharpTypeStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(cSharpTypeStub.getName());
		stubOutputStream.writeName(cSharpTypeStub.getParentQName());
		cSharpTypeStub.getReturnType().writeTo(stubOutputStream);
		cSharpTypeStub.getConversionTypeInfo().writeTo(stubOutputStream);
	}

	@NotNull
	@Override
	public CSharpConversionMethodDeclStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		StringRef name = stubInputStream.readName();
		StringRef qname = stubInputStream.readName();
		val typeInfo = CSharpStubTypeInfoUtil.read(stubInputStream);
		val conversionTypeInfo = CSharpStubTypeInfoUtil.read(stubInputStream);
		return new CSharpConversionMethodDeclStub(stubElement, name, qname,  0, typeInfo, conversionTypeInfo);
	}
}
