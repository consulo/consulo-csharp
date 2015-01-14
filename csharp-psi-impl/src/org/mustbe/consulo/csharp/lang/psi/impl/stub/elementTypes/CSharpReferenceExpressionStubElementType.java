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
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpStubReferenceExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpReferenceExpressionStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;

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

	@NotNull
	@Override
	public PsiElement createElement(@NotNull ASTNode astNode)
	{
		return new CSharpStubReferenceExpressionImpl(astNode);
	}

	@Override
	public CSharpReferenceExpression createPsi(@NotNull CSharpReferenceExpressionStub stub)
	{
		return new CSharpStubReferenceExpressionImpl(stub, this);
	}

	@Override
	public CSharpReferenceExpressionStub createStub(@NotNull CSharpReferenceExpression psi, StubElement parentStub)
	{
		String referenceName = psi.getReferenceNameWithAt();
		CSharpReferenceExpression.ResolveToKind kind = psi.kind();
		boolean global = psi.isGlobalElement();
		return new CSharpReferenceExpressionStub(parentStub, this, referenceName, kind.ordinal(), global);
	}

	@Override
	public void serialize(@NotNull CSharpReferenceExpressionStub stub, @NotNull StubOutputStream dataStream) throws IOException
	{
		dataStream.writeName(stub.getReferenceText());
		dataStream.writeVarInt(stub.getKindIndex());
		dataStream.writeBoolean(stub.isGlobal());
	}

	@NotNull
	@Override
	public CSharpReferenceExpressionStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException
	{
		StringRef referenceText = dataStream.readName();
		int kind = dataStream.readVarInt();
		boolean global = dataStream.readBoolean();
		return new CSharpReferenceExpressionStub(parentStub, this, referenceText, kind, global);
	}
}
