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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpReferenceTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpEmptyStub;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceExpression;
import org.mustbe.consulo.dotnet.psi.DotNetUserType;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpUserTypeImpl extends CSharpStubTypeElementImpl<CSharpEmptyStub<CSharpUserTypeImpl>> implements DotNetUserType
{
	public CSharpUserTypeImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpUserTypeImpl(@NotNull CSharpEmptyStub<CSharpUserTypeImpl> stub,
			@NotNull IStubElementType<? extends CSharpEmptyStub<CSharpUserTypeImpl>, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitReferenceType(this);
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRefImpl()
	{
		CSharpReferenceExpression referenceExpression = getReferenceExpression();
		return new CSharpReferenceTypeRef(referenceExpression);
	}

	@NotNull
	@Override
	public DotNetPsiSearcher.TypeResoleKind getTypeResoleKind()
	{
		return DotNetPsiSearcher.TypeResoleKind.UNKNOWN;
	}

	@NotNull
	@Override
	public String getReferenceText()
	{
		DotNetReferenceExpression referenceExpression = getReferenceExpression();
		return referenceExpression.getReferenceName();
	}

	@NotNull
	@Override
	public CSharpReferenceExpression getReferenceExpression()
	{
		return getRequiredStubOrPsiChild(CSharpStubElements.REFERENCE_EXPRESSION);
	}

	@NotNull
	public DotNetTypeRef[] getArgumentTypeRefs()
	{
		CSharpReferenceExpression referenceExpression = getReferenceExpression();
		return referenceExpression.getTypeArgumentListRefs();
	}
}
