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

package consulo.csharp.lang.impl.psi.stub;

import jakarta.annotation.Nonnull;

import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubBase;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.language.psi.stub.StubElement;

/**
 * @author VISTALL
 * @since 05.12.14
 */
public class CSharpReferenceExpressionStub extends StubBase<CSharpReferenceExpression>
{
	private final String myReferenceText;
	private final int myKindIndex;
	private final int myMemberAccessTypeIndex;
	private final boolean myGlobal;

	public CSharpReferenceExpressionStub(StubElement parent, IStubElementType elementType, String referenceText, int kindIndex, int memberAccessType, boolean global)
	{
		super(parent, elementType);
		myGlobal = global;
		myReferenceText = referenceText;
		myKindIndex = kindIndex;
		myMemberAccessTypeIndex = memberAccessType;
	}

	public boolean isGlobal()
	{
		return myGlobal;
	}

	public String getReferenceText()
	{
		return myReferenceText;
	}

	public int getKindIndex()
	{
		return myKindIndex;
	}

	public int getMemberAccessTypeIndex()
	{
		return myMemberAccessTypeIndex;
	}

	@Nonnull
	public CSharpReferenceExpression.AccessType getMemberAccessType()
	{
		return CSharpReferenceExpression.AccessType.VALUES[myMemberAccessTypeIndex];
	}

	@Nonnull
	public CSharpReferenceExpression.ResolveToKind getKind()
	{
		return CSharpReferenceExpression.ResolveToKind.VALUES[myKindIndex];
	}
}
