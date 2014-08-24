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
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpNamespaceHelper;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpNamespaceDeclarationImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpNamespaceStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public class CSharpNamespaceStubElementType extends CSharpAbstractStubElementType<CSharpNamespaceStub, CSharpNamespaceDeclarationImpl>
{
	public CSharpNamespaceStubElementType()
	{
		super("NAMESPACE_DECLARATION");
	}

	@Override
	public CSharpNamespaceDeclarationImpl createPsi(@NotNull CSharpNamespaceStub cSharpNamespaceStub)
	{
		return new CSharpNamespaceDeclarationImpl(cSharpNamespaceStub);
	}

	@Override
	public CSharpNamespaceDeclarationImpl createElement(@NotNull ASTNode astNode)
	{
		return new CSharpNamespaceDeclarationImpl(astNode);
	}

	@Override
	public CSharpNamespaceStub createStub(@NotNull CSharpNamespaceDeclarationImpl cSharpNamespaceDeclaration, StubElement stubElement)
	{
		StringRef name = StringRef.fromNullableString(cSharpNamespaceDeclaration.getName());
		StringRef parentQName = StringRef.fromNullableString(cSharpNamespaceDeclaration.getPresentableParentQName());
		return new CSharpNamespaceStub(stubElement, name, parentQName);
	}

	@Override
	public void serialize(@NotNull CSharpNamespaceStub cSharpNamespaceStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(cSharpNamespaceStub.getName());
		stubOutputStream.writeName(cSharpNamespaceStub.getParentQName());
	}

	@NotNull
	@Override
	public CSharpNamespaceStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		StringRef qname = stubInputStream.readName();
		StringRef parentQName = stubInputStream.readName();
		return new CSharpNamespaceStub(stubElement, qname, parentQName);
	}

	@Override
	public void indexStub(@NotNull CSharpNamespaceStub cSharpNamespaceStub, @NotNull IndexSink indexSink)
	{
		String name = cSharpNamespaceStub.getName();
		if(!StringUtil.isEmpty(name))
		{
			String parentQName = cSharpNamespaceStub.getParentQName();

			indexSink.occurrence(CSharpIndexKeys.NAMESPACE_BY_QNAME_INDEX, CSharpNamespaceHelper.getNameWithNamespaceForIndexing(parentQName, name));

			indexSink.occurrence(CSharpIndexKeys.MEMBER_BY_NAMESPACE_QNAME_INDEX, CSharpNamespaceHelper.getNamespaceForIndexing(parentQName));
		}
	}
}
