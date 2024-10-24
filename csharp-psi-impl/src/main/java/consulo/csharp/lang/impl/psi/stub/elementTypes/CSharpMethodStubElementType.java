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
import consulo.csharp.lang.impl.psi.source.CSharpMethodDeclarationImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpMethodDeclStub;
import consulo.csharp.lang.impl.psi.stub.CSharpMsilStubIndexer;
import consulo.csharp.lang.impl.psi.stub.CSharpTypeDeclStub;
import consulo.csharp.lang.impl.psi.stub.index.CSharpIndexKeys;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.dotnet.psi.impl.stub.DotNetNamespaceStubUtil;
import consulo.index.io.StringRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.IndexSink;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import consulo.util.lang.BitUtil;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import java.io.IOException;

/**
 * @author VISTALL
 * @since 18.12.13.
 */
public class CSharpMethodStubElementType extends CSharpAbstractStubElementType<CSharpMethodDeclStub, CSharpMethodDeclaration>
{
	public CSharpMethodStubElementType()
	{
		super("METHOD_DECLARATION");
	}

	@Nonnull
	@Override
	public CSharpMethodDeclaration createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpMethodDeclarationImpl(astNode);
	}

	@Override
	public CSharpMethodDeclaration createPsi(@Nonnull CSharpMethodDeclStub cSharpTypeStub)
	{
		return new CSharpMethodDeclarationImpl(cSharpTypeStub, this);
	}

	@Nonnull
	@RequiredReadAction
	@Override
	public CSharpMethodDeclStub createStub(@Nonnull CSharpMethodDeclaration methodDeclaration, StubElement stubElement)
	{
		String parentQName = methodDeclaration.getPresentableParentQName();
		int otherModifierMask = CSharpMethodDeclStub.getOtherModifierMask(methodDeclaration);
		int operatorIndex = CSharpMethodDeclStub.getOperatorIndex(methodDeclaration);
		return new CSharpMethodDeclStub(stubElement, parentQName, otherModifierMask, operatorIndex);
	}

	@Override
	public void serialize(@Nonnull CSharpMethodDeclStub stub, @Nonnull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(stub.getParentQName());
		stubOutputStream.writeVarInt(stub.getOtherModifierMask());
		stubOutputStream.writeVarInt(stub.getOperatorIndex());
	}

	@Nonnull
	@Override
	public CSharpMethodDeclStub deserialize(@Nonnull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		StringRef qname = stubInputStream.readName();
		int otherModifierMask = stubInputStream.readVarInt();
		int operatorIndex = stubInputStream.readVarInt();
		return new CSharpMethodDeclStub(stubElement, StringRef.toString(qname), otherModifierMask, operatorIndex);
	}

	@Override
	@RequiredReadAction
	public void indexStub(@Nonnull CSharpMethodDeclStub stub, @Nonnull IndexSink indexSink)
	{
		String name = getNameWithoutAt(stub);
		if(!StringUtil.isEmpty(name))
		{
			indexSink.occurrence(CSharpIndexKeys.METHOD_INDEX, name);

			if(!stub.isNested())
			{
				if(BitUtil.isSet(stub.getOtherModifierMask(), CSharpMethodDeclStub.DELEGATE_MASK))
				{
					String parentQName = stub.getParentQName();

					DotNetNamespaceStubUtil.indexStub(indexSink, CSharpIndexKeys.MEMBER_BY_NAMESPACE_QNAME_INDEX, CSharpIndexKeys.MEMBER_BY_ALL_NAMESPACE_QNAME_INDEX, parentQName, name);
				}
			}

			if(BitUtil.isSet(stub.getOtherModifierMask(), CSharpMethodDeclStub.EXTENSION_MASK))
			{
				indexExtensionMethod(indexSink, stub, name);
			}

			if(BitUtil.isSet(stub.getOtherModifierMask(), CSharpMethodDeclStub.DELEGATE_MASK))
			{
				indexSink.occurrence(CSharpIndexKeys.DELEGATE_METHOD_BY_NAME_INDEX, name);
			}
		}
	}

	private void indexExtensionMethod(IndexSink indexSink, CSharpMethodDeclStub stub, String name)
	{
		indexSink.occurrence(CSharpIndexKeys.EXTENSION_METHOD_BY_NAME_INDEX, name.hashCode());

		StubElement parentStub = stub.getParentStub();
		if(!(parentStub instanceof CSharpTypeDeclStub))
		{
			return;
		}

		CSharpTypeDeclStub typeStub = (CSharpTypeDeclStub) parentStub;
		if(typeStub.isNested())
		{
			return;
		}

		String namespace = typeStub.getParentQName();
		indexSink.occurrence(CSharpIndexKeys.EXTENSION_METHOD_BY_NAMESPACE, DotNetNamespaceStubUtil.getIndexableNamespace(StringUtil.notNullize(namespace)).hashCode());
	
		int indexKey = CSharpMsilStubIndexer.makeExtensionMethodIndexKey(namespace, name);
		indexSink.occurrence(CSharpIndexKeys.EXTENSION_METHOD_BY_NAMESPACE_AND_NAME_INDEX, indexKey);
	}
}
