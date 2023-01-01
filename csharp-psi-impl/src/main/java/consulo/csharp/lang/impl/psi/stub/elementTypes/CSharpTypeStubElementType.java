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

package consulo.csharp.lang.impl.psi.stub.elementTypes;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.source.CSharpTypeDeclarationImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpTypeDeclStub;
import consulo.csharp.lang.impl.psi.stub.index.CSharpIndexKeys;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.impl.stub.DotNetNamespaceStubUtil;
import consulo.index.io.StringRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.IndexSink;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public class CSharpTypeStubElementType extends CSharpAbstractStubElementType<CSharpTypeDeclStub, CSharpTypeDeclaration>
{
	public CSharpTypeStubElementType()
	{
		super("TYPE_DECLARATION");
	}

	@Nonnull
	@Override
	public CSharpTypeDeclaration createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpTypeDeclarationImpl(astNode);
	}

	@Override
	public CSharpTypeDeclaration createPsi(@Nonnull CSharpTypeDeclStub stub)
	{
		return new CSharpTypeDeclarationImpl(stub);
	}

	@RequiredReadAction
	@Override
	public CSharpTypeDeclStub createStub(@Nonnull CSharpTypeDeclaration typeDeclaration, StubElement stubElement)
	{
		String parentQName = typeDeclaration.getPresentableParentQName();
		String vmQName = typeDeclaration.getVmQName();
		int otherModifierMask = CSharpTypeDeclStub.getOtherModifiers(typeDeclaration);
		return new CSharpTypeDeclStub(stubElement, parentQName, vmQName, otherModifierMask);
	}

	@Override
	public void serialize(@Nonnull CSharpTypeDeclStub stub, @Nonnull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(stub.getParentQName());
		stubOutputStream.writeName(stub.getVmQName());
		stubOutputStream.writeVarInt(stub.getOtherModifierMask());
	}

	@Nonnull
	@Override
	public CSharpTypeDeclStub deserialize(@Nonnull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		StringRef parentQName = stubInputStream.readName();
		StringRef vmQName = stubInputStream.readName();
		int otherModifierMask = stubInputStream.readVarInt();
		return new CSharpTypeDeclStub(stubElement, StringRef.toString(parentQName), StringRef.toString(vmQName), otherModifierMask);
	}

	@Override
	@RequiredReadAction
	public void indexStub(@Nonnull CSharpTypeDeclStub stub, @Nonnull IndexSink indexSink)
	{
		String name = getNameWithoutAt(stub);
		if(!StringUtil.isEmpty(name))
		{
			indexSink.occurrence(CSharpIndexKeys.TYPE_INDEX, name);

			String parentQName = stub.getParentQName();
			if(!stub.isNested())
			{
				DotNetNamespaceStubUtil.indexStub(indexSink, CSharpIndexKeys.MEMBER_BY_NAMESPACE_QNAME_INDEX, CSharpIndexKeys.MEMBER_BY_ALL_NAMESPACE_QNAME_INDEX, parentQName, name);
			}

			indexSink.occurrence(CSharpIndexKeys.TYPE_BY_VMQNAME_INDEX, stub.getVmQName().hashCode());
		}
	}
}
