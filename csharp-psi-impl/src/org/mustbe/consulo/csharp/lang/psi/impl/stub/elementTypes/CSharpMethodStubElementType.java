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
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpNamespaceHelper;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodDeclarationImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpMethodStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.MemberStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.typeStub.CSharpStubTypeInfoUtil;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.BitUtil;
import com.intellij.util.io.StringRef;
import lombok.val;

/**
 * @author VISTALL
 * @since 18.12.13.
 */
public class CSharpMethodStubElementType extends CSharpAbstractStubElementType<CSharpMethodStub, CSharpMethodDeclaration>
{
	public CSharpMethodStubElementType()
	{
		super("METHOD_DECLARATION");
	}

	@Override
	public CSharpMethodDeclaration createPsi(@NotNull ASTNode astNode)
	{
		return new CSharpMethodDeclarationImpl(astNode);
	}

	@Override
	public CSharpMethodDeclaration createPsi(@NotNull CSharpMethodStub cSharpTypeStub)
	{
		return new CSharpMethodDeclarationImpl(cSharpTypeStub, this);
	}

	@Override
	public CSharpMethodStub createStub(@NotNull CSharpMethodDeclaration methodDeclaration, StubElement stubElement)
	{
		StringRef name = StringRef.fromNullableString(methodDeclaration.getName());
		StringRef parentQName = StringRef.fromNullableString(methodDeclaration.getPresentableParentQName());
		int modifierMask = MemberStub.getModifierMask(methodDeclaration);
		int otherModifierMask = CSharpMethodStub.getOtherModifierMask(methodDeclaration);
		val typeInfo = CSharpStubTypeInfoUtil.toStub(methodDeclaration.getReturnType());
		return new CSharpMethodStub(stubElement, name, parentQName, modifierMask, otherModifierMask, typeInfo);
	}

	@Override
	public void serialize(@NotNull CSharpMethodStub cSharpTypeStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(cSharpTypeStub.getName());
		stubOutputStream.writeName(cSharpTypeStub.getParentQName());
		stubOutputStream.writeInt(cSharpTypeStub.getModifierMask());
		stubOutputStream.writeInt(cSharpTypeStub.getOtherModifierMask());
		cSharpTypeStub.getReturnType().writeTo(stubOutputStream);
	}

	@NotNull
	@Override
	public CSharpMethodStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		StringRef name = stubInputStream.readName();
		StringRef qname = stubInputStream.readName();
		int modifierMask = stubInputStream.readInt();
		int otherModifierMask = stubInputStream.readInt();
		val typeInfo = CSharpStubTypeInfoUtil.read(stubInputStream);
		return new CSharpMethodStub(stubElement, name, qname, modifierMask, otherModifierMask, typeInfo);
	}

	@Override
	public void indexStub(@NotNull CSharpMethodStub cSharpTypeStub, @NotNull IndexSink indexSink)
	{
		String name = cSharpTypeStub.getName();
		if(!StringUtil.isEmpty(name))
		{
			indexSink.occurrence(CSharpIndexKeys.METHOD_INDEX, name);

			val parentQName = cSharpTypeStub.getParentQName();

			indexSink.occurrence(CSharpIndexKeys.MEMBER_BY_NAMESPACE_QNAME_INDEX, CSharpNamespaceHelper.getNamespaceForIndexing(parentQName));

			if(BitUtil.isSet(cSharpTypeStub.getOtherModifierMask(), CSharpMethodStub.EXTENSION_MASK))
			{
				indexSink.occurrence(CSharpIndexKeys.EXTENSION_METHOD_INDEX, name);
			}
		}
	}
}
