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
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.impl.source.CSharpMethodDeclarationImpl;
import consulo.csharp.lang.psi.impl.stub.CSharpMethodDeclStub;
import consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import consulo.dotnet.lang.psi.impl.stub.DotNetNamespaceStubUtil;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.BitUtil;
import com.intellij.util.io.StringRef;

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

	@NotNull
	@Override
	public CSharpMethodDeclaration createElement(@NotNull ASTNode astNode)
	{
		return new CSharpMethodDeclarationImpl(astNode);
	}

	@Override
	public CSharpMethodDeclaration createPsi(@NotNull CSharpMethodDeclStub cSharpTypeStub)
	{
		return new CSharpMethodDeclarationImpl(cSharpTypeStub, this);
	}

	@RequiredReadAction
	@Override
	public CSharpMethodDeclStub createStub(@NotNull CSharpMethodDeclaration methodDeclaration, StubElement stubElement)
	{
		StringRef parentQName = StringRef.fromNullableString(methodDeclaration.getPresentableParentQName());
		int otherModifierMask = CSharpMethodDeclStub.getOtherModifierMask(methodDeclaration);
		int operatorIndex = CSharpMethodDeclStub.getOperatorIndex(methodDeclaration);
		return new CSharpMethodDeclStub(stubElement, parentQName, otherModifierMask, operatorIndex);
	}

	@Override
	public void serialize(@NotNull CSharpMethodDeclStub stub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(stub.getParentQName());
		stubOutputStream.writeInt(stub.getOtherModifierMask());
		stubOutputStream.writeInt(stub.getOperatorIndex());
	}

	@NotNull
	@Override
	public CSharpMethodDeclStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		StringRef qname = stubInputStream.readName();
		int otherModifierMask = stubInputStream.readInt();
		int operatorIndex = stubInputStream.readInt();
		return new CSharpMethodDeclStub(stubElement, qname, otherModifierMask, operatorIndex);
	}

	@Override
	@RequiredReadAction
	public void indexStub(@NotNull CSharpMethodDeclStub stub, @NotNull IndexSink indexSink)
	{
		String name = getName(stub);
		if(!StringUtil.isEmpty(name))
		{
			indexSink.occurrence(CSharpIndexKeys.METHOD_INDEX, name);

			if(!stub.isNested())
			{
				if(BitUtil.isSet(stub.getOtherModifierMask(), CSharpMethodDeclStub.DELEGATE_MASK))
				{
					String parentQName = stub.getParentQName();

					DotNetNamespaceStubUtil.indexStub(indexSink, CSharpIndexKeys.MEMBER_BY_NAMESPACE_QNAME_INDEX,
							CSharpIndexKeys.MEMBER_BY_ALL_NAMESPACE_QNAME_INDEX, parentQName, name);
				}
			}

			if(BitUtil.isSet(stub.getOtherModifierMask(), CSharpMethodDeclStub.EXTENSION_MASK))
			{
				indexSink.occurrence(CSharpIndexKeys.EXTENSION_METHOD_BY_NAME_INDEX, name);
			}

			if(BitUtil.isSet(stub.getOtherModifierMask(), CSharpMethodDeclStub.DELEGATE_MASK))
			{
				indexSink.occurrence(CSharpIndexKeys.DELEGATE_METHOD_BY_NAME_INDEX, name);
			}
		}
	}
}
