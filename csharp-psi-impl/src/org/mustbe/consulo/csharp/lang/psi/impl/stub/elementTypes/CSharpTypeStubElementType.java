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
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeDeclarationImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpTypeStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.MemberStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import org.mustbe.consulo.dotnet.psi.DotNetNamespaceUtil;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.psi.util.QualifiedName;
import com.intellij.util.io.StringRef;
import lombok.val;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public class CSharpTypeStubElementType extends CSharpAbstractStubElementType<CSharpTypeStub, CSharpTypeDeclaration>
{
	public CSharpTypeStubElementType()
	{
		super("TYPE_DECLARATION");
	}

	@Override
	public CSharpTypeDeclaration createElement(@NotNull ASTNode astNode)
	{
		return new CSharpTypeDeclarationImpl(astNode);
	}

	@Override
	public CSharpTypeDeclaration createPsi(@NotNull CSharpTypeStub stub)
	{
		return new CSharpTypeDeclarationImpl(stub);
	}

	@Override
	public CSharpTypeStub createStub(@NotNull CSharpTypeDeclaration typeDeclaration, StubElement stubElement)
	{
		StringRef name = StringRef.fromNullableString(typeDeclaration.getName());
		StringRef parentQName = StringRef.fromNullableString(typeDeclaration.getPresentableParentQName());
		StringRef vmQName = StringRef.fromNullableString(typeDeclaration.getVmQName());
		int modifierMask = MemberStub.getModifierMask(typeDeclaration);
		int otherModifierMask = CSharpTypeStub.getOtherModifiers(typeDeclaration);
		return new CSharpTypeStub(stubElement, name, parentQName, vmQName, modifierMask, otherModifierMask);
	}

	@Override
	public void serialize(@NotNull CSharpTypeStub stub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(stub.getName());
		stubOutputStream.writeName(stub.getParentQName());
		stubOutputStream.writeName(stub.getVmQName());
		stubOutputStream.writeInt(stub.getModifierMask());
		stubOutputStream.writeInt(stub.getOtherModifierMask());
	}

	@NotNull
	@Override
	public CSharpTypeStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		StringRef name = stubInputStream.readName();
		StringRef parentQName = stubInputStream.readName();
		StringRef vmQName = stubInputStream.readName();
		int modifierMask = stubInputStream.readInt();
		int otherModifierMask = stubInputStream.readInt();
		return new CSharpTypeStub(stubElement, name, parentQName, vmQName, modifierMask, otherModifierMask);
	}

	@Override
	public void indexStub(@NotNull CSharpTypeStub stub, @NotNull IndexSink indexSink)
	{
		String name = stub.getName();
		if(!StringUtil.isEmpty(name))
		{
			indexSink.occurrence(CSharpIndexKeys.TYPE_INDEX, name);

			val parentQName = stub.getParentQName();

			indexSink.occurrence(CSharpIndexKeys.MEMBER_BY_NAMESPACE_QNAME_INDEX, DotNetNamespaceUtil.getIndexableNamespace(parentQName));

			if(!StringUtil.isEmpty(parentQName) && !stub.isNested())
			{
				QualifiedName parent = QualifiedName.fromDottedString(parentQName);
				do
				{
					indexSink.occurrence(CSharpIndexKeys.MEMBER_BY_ALL_NAMESPACE_QNAME_INDEX, DotNetNamespaceUtil.getIndexableNamespace(parent));
				}
				while((parent = parent.getParent()) != null);
			}

			indexSink.occurrence(CSharpIndexKeys.TYPE_BY_QNAME_INDEX, getNameWithNamespaceForIndexing(parentQName, name));

			indexSink.occurrence(CSharpIndexKeys.TYPE_BY_VMQNAME_INDEX, stub.getVmQName());
		}
	}

	@NotNull
	public static String getNameWithNamespaceForIndexing(@Nullable String namespace, @NotNull String name)
	{
		if(StringUtil.isEmpty(namespace))
		{
			return name;
		}
		return namespace + "." + name;
	}
}
