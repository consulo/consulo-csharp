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
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.cache.CSharpResolveCache;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.lazy.CSharpLazyGenericWrapperTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.lazy.CSharpLazyReferenceTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpEmptyStub;
import org.mustbe.consulo.dotnet.psi.DotNetUserType;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpUserTypeImpl extends CSharpStubElementImpl<CSharpEmptyStub<CSharpUserTypeImpl>> implements DotNetUserType
{
	public static class OurResolver extends CSharpResolveCache.TypeRefResolver<CSharpUserTypeImpl>
	{
		public static final OurResolver INSTANCE = new OurResolver();

		@NotNull
		@Override
		public DotNetTypeRef resolveTypeRef(@NotNull CSharpUserTypeImpl element, boolean resolveFromParent)
		{
			CSharpReferenceExpression referenceExpression = element.getReferenceExpression();

			DotNetTypeRef[] typeArgumentListRefs = referenceExpression.getTypeArgumentListRefs();

			CSharpLazyReferenceTypeRef referenceTypeRef = new CSharpLazyReferenceTypeRef(referenceExpression);
			if(typeArgumentListRefs.length > 0)
			{
				return new CSharpLazyGenericWrapperTypeRef(element, referenceTypeRef, typeArgumentListRefs);
			}
			return referenceTypeRef;
		}
	}

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
	public DotNetTypeRef toTypeRef()
	{
		return CSharpResolveCache.getInstance(getProject()).resolveTypeRef(this, OurResolver.INSTANCE, true);
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
		CSharpReferenceExpression referenceExpression = getReferenceExpression();
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
