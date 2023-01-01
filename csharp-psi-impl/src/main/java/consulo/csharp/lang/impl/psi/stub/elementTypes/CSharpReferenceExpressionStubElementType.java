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

import java.io.IOException;

import javax.annotation.Nonnull;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.impl.psi.source.CSharpStubReferenceExpressionImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpReferenceExpressionStub;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.index.io.StringRef;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;

/**
 * @author VISTALL
 * @since 05.12.14
 */
public class CSharpReferenceExpressionStubElementType extends CSharpAbstractStubElementType<CSharpReferenceExpressionStub, CSharpReferenceExpression>
{
	public CSharpReferenceExpressionStubElementType()
	{
		super("REFERENCE_NAME");
	}

	@Nonnull
	@Override
	public PsiElement createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpStubReferenceExpressionImpl(astNode);
	}

	@Override
	public CSharpReferenceExpression createPsi(@Nonnull CSharpReferenceExpressionStub stub)
	{
		return new CSharpStubReferenceExpressionImpl(stub, this);
	}

	@RequiredReadAction
	@Override
	public CSharpReferenceExpressionStub createStub(@Nonnull CSharpReferenceExpression psi, StubElement parentStub)
	{
		String referenceName = psi.getReferenceNameWithAt();
		CSharpReferenceExpression.ResolveToKind kind = psi.kind();
		boolean global = psi.isGlobalElement();
		CSharpReferenceExpression.AccessType memberAccessType = psi.getMemberAccessType();
		return new CSharpReferenceExpressionStub(parentStub, this, referenceName, kind.ordinal(), memberAccessType.ordinal(), global);
	}

	@Override
	public void serialize(@Nonnull CSharpReferenceExpressionStub stub, @Nonnull StubOutputStream dataStream) throws IOException
	{
		dataStream.writeName(stub.getReferenceText());
		dataStream.writeVarInt(stub.getKindIndex());
		dataStream.writeVarInt(stub.getMemberAccessTypeIndex());
		dataStream.writeBoolean(stub.isGlobal());
	}

	@Nonnull
	@Override
	public CSharpReferenceExpressionStub deserialize(@Nonnull StubInputStream dataStream, StubElement parentStub) throws IOException
	{
		StringRef referenceText = dataStream.readName();
		int kind = dataStream.readVarInt();
		int memberAccessType = dataStream.readVarInt();
		boolean global = dataStream.readBoolean();
		return new CSharpReferenceExpressionStub(parentStub, this, StringRef.toString(referenceText), kind, memberAccessType, global);
	}
}
