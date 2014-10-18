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
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodDeclarationImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpMethodStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.typeStub.CSharpStubTypeInfoUtil;
import org.mustbe.consulo.dotnet.psi.DotNetNamespaceUtil;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.psi.util.QualifiedName;
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

	@NotNull
	@Override
	public CSharpMethodDeclaration createElement(@NotNull ASTNode astNode)
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
		int otherModifierMask = CSharpMethodStub.getOtherModifierMask(methodDeclaration);
		val typeInfo = CSharpStubTypeInfoUtil.toStub(methodDeclaration.getReturnType());
		int operatorIndex = CSharpMethodStub.getOperatorIndex(methodDeclaration);
		val typeForImplement = CSharpStubTypeInfoUtil.toStub(methodDeclaration.getTypeForImplement());
		return new CSharpMethodStub(stubElement, name, parentQName, otherModifierMask, typeInfo, typeForImplement, operatorIndex);
	}

	@Override
	public void serialize(@NotNull CSharpMethodStub stub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(stub.getName());
		stubOutputStream.writeName(stub.getParentQName());
		stubOutputStream.writeInt(stub.getOtherModifierMask());
		stub.getReturnType().writeTo(stubOutputStream);
		stub.getImplementType().writeTo(stubOutputStream);
		stubOutputStream.writeInt(stub.getOperatorIndex());
	}

	@NotNull
	@Override
	public CSharpMethodStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		StringRef name = stubInputStream.readName();
		StringRef qname = stubInputStream.readName();
		int otherModifierMask = stubInputStream.readInt();
		val typeInfo = CSharpStubTypeInfoUtil.read(stubInputStream);
		val typeImplementInfo = CSharpStubTypeInfoUtil.read(stubInputStream);
		int operatorIndex = stubInputStream.readInt();
		return new CSharpMethodStub(stubElement, name, qname, otherModifierMask, typeInfo, typeImplementInfo, operatorIndex);
	}

	@Override
	public void indexStub(@NotNull CSharpMethodStub stub, @NotNull IndexSink indexSink)
	{
		String name = stub.getName();
		if(!StringUtil.isEmpty(name))
		{
			indexSink.occurrence(CSharpIndexKeys.METHOD_INDEX, name);

			if(BitUtil.isSet(stub.getOtherModifierMask(), CSharpMethodStub.DELEGATE_MASK))
			{
				val parentQName = stub.getParentQName();

				indexSink.occurrence(CSharpIndexKeys.MEMBER_BY_NAMESPACE_QNAME_INDEX, DotNetNamespaceUtil.getIndexableNamespace(parentQName));

				if(!StringUtil.isEmpty(parentQName))
				{
					QualifiedName parent = QualifiedName.fromDottedString(parentQName);
					do
					{
						indexSink.occurrence(CSharpIndexKeys.MEMBER_BY_ALL_NAMESPACE_QNAME_INDEX, DotNetNamespaceUtil.getIndexableNamespace(parent));
					}
					while((parent = parent.getParent()) != null);
				}
			}

			if(BitUtil.isSet(stub.getOtherModifierMask(), CSharpMethodStub.EXTENSION_MASK))
			{
				indexSink.occurrence(CSharpIndexKeys.EXTENSION_METHOD_INDEX, name);
			}
		}
	}
}
