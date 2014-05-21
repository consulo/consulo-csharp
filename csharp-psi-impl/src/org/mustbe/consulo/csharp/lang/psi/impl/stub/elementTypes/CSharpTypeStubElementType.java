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
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpNamespaceHelper;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeDeclarationImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpTypeStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.MemberStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
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
	public CSharpTypeDeclaration createPsi(@NotNull ASTNode astNode)
	{
		return new CSharpTypeDeclarationImpl(astNode);
	}

	@Override
	public CSharpTypeDeclaration createPsi(@NotNull CSharpTypeStub cSharpTypeStub)
	{
		return new CSharpTypeDeclarationImpl(cSharpTypeStub);
	}

	@Override
	public CSharpTypeStub createStub(@NotNull CSharpTypeDeclaration cSharpTypeDeclaration, StubElement stubElement)
	{
		StringRef name = StringRef.fromNullableString(cSharpTypeDeclaration.getName());
		StringRef parentQName = StringRef.fromNullableString(cSharpTypeDeclaration.getPresentableParentQName());
		int modifierMask = MemberStub.getModifierMask(cSharpTypeDeclaration);
		int otherModifierMask = CSharpTypeStub.getOtherModifiers(cSharpTypeDeclaration);
		return new CSharpTypeStub(stubElement, name, parentQName, modifierMask, otherModifierMask);
	}

	@Override
	public void serialize(@NotNull CSharpTypeStub cSharpTypeStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(cSharpTypeStub.getName());
		stubOutputStream.writeName(cSharpTypeStub.getParentQName());
		stubOutputStream.writeInt(cSharpTypeStub.getModifierMask());
		stubOutputStream.writeInt(cSharpTypeStub.getOtherModifierMask());
	}

	@NotNull
	@Override
	public CSharpTypeStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		StringRef name = stubInputStream.readName();
		StringRef parentQName = stubInputStream.readName();
		int modifierMask = stubInputStream.readInt();
		int otherModifierMask = stubInputStream.readInt();
		return new CSharpTypeStub(stubElement, name, parentQName, modifierMask, otherModifierMask);
	}

	@Override
	public void indexStub(@NotNull CSharpTypeStub cSharpTypeStub, @NotNull IndexSink indexSink)
	{
		String name = cSharpTypeStub.getName();
		if(!StringUtil.isEmpty(name))
		{
			indexSink.occurrence(CSharpIndexKeys.TYPE_INDEX, name);

			val parentQName = cSharpTypeStub.getParentQName();

			indexSink.occurrence(CSharpIndexKeys.MEMBER_BY_NAMESPACE_QNAME_INDEX, CSharpNamespaceHelper.getNamespaceForIndexing(parentQName));

			indexSink.occurrence(CSharpIndexKeys.TYPE_BY_QNAME_INDEX, CSharpNamespaceHelper.getNameWithNamespaceForIndexing(parentQName, name));
		}
	}
}
