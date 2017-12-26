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

package consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.BitUtil;
import com.intellij.util.io.StringRef;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.source.CSharpTypeDeclarationImpl;
import consulo.csharp.lang.psi.impl.stub.CSharpTypeDeclStub;
import consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import consulo.dotnet.lang.psi.impl.stub.DotNetNamespaceStubUtil;

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

	@NotNull
	@Override
	public CSharpTypeDeclaration createElement(@NotNull ASTNode astNode)
	{
		return new CSharpTypeDeclarationImpl(astNode);
	}

	@Override
	public CSharpTypeDeclaration createPsi(@NotNull CSharpTypeDeclStub stub)
	{
		return new CSharpTypeDeclarationImpl(stub);
	}

	@RequiredReadAction
	@Override
	public CSharpTypeDeclStub createStub(@NotNull CSharpTypeDeclaration typeDeclaration, StubElement stubElement)
	{
		String parentQName = typeDeclaration.getPresentableParentQName();
		String vmQName = typeDeclaration.getVmQName();
		int otherModifierMask = CSharpTypeDeclStub.getOtherModifiers(typeDeclaration);
		return new CSharpTypeDeclStub(stubElement, parentQName, vmQName, otherModifierMask);
	}

	@Override
	public void serialize(@NotNull CSharpTypeDeclStub stub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(stub.getParentQName());
		stubOutputStream.writeName(stub.getVmQName());
		stubOutputStream.writeInt(stub.getOtherModifierMask());
	}

	@NotNull
	@Override
	public CSharpTypeDeclStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		StringRef parentQName = stubInputStream.readName();
		StringRef vmQName = stubInputStream.readName();
		int otherModifierMask = stubInputStream.readInt();
		return new CSharpTypeDeclStub(stubElement, StringRef.toString(parentQName), StringRef.toString(vmQName), otherModifierMask);
	}

	@Override
	@RequiredReadAction
	public void indexStub(@NotNull CSharpTypeDeclStub stub, @NotNull IndexSink indexSink)
	{
		String name = getName(stub);
		if(!StringUtil.isEmpty(name))
		{
			indexSink.occurrence(CSharpIndexKeys.TYPE_INDEX, name);

			String parentQName = stub.getParentQName();
			if(!stub.isNested())
			{
				DotNetNamespaceStubUtil.indexStub(indexSink, CSharpIndexKeys.MEMBER_BY_NAMESPACE_QNAME_INDEX, CSharpIndexKeys.MEMBER_BY_ALL_NAMESPACE_QNAME_INDEX, parentQName, name);

				if(BitUtil.isSet(stub.getOtherModifierMask(), CSharpTypeDeclStub.HAVE_EXTENSIONS))
				{
					indexSink.occurrence(CSharpIndexKeys.TYPE_WITH_EXTENSION_METHODS_INDEX, DotNetNamespaceStubUtil.getIndexableNamespace(parentQName));
				}
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
