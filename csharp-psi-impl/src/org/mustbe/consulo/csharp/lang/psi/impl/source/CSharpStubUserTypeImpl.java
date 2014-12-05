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
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpUserType;
import org.mustbe.consulo.csharp.lang.psi.impl.fragment.CSharpFragmentFactory;
import org.mustbe.consulo.csharp.lang.psi.impl.fragment.CSharpFragmentFileImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpReferenceTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpWithStringValueStub;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceExpression;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpStubUserTypeImpl extends CSharpStubTypeElementImpl<CSharpWithStringValueStub<CSharpUserType>> implements CSharpUserType
{
	public CSharpStubUserTypeImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpStubUserTypeImpl(@NotNull CSharpWithStringValueStub<CSharpUserType> stub,
			@NotNull IStubElementType<? extends CSharpWithStringValueStub<CSharpUserType>, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitUserType(this);
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRefImpl()
	{
		CSharpReferenceExpression referenceExpression = getReferenceExpressionByStub();
		if(referenceExpression == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}
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
		CSharpWithStringValueStub<CSharpUserType> stub = getStub();
		if(stub != null)
		{
			//noinspection ConstantConditions
			return stub.getReferenceText();
		}
		DotNetReferenceExpression referenceExpression = getReferenceExpression();
		return referenceExpression.getText();
	}

	@NotNull
	@Override
	public CSharpReferenceExpression getReferenceExpression()
	{
		return findNotNullChildByClass(CSharpReferenceExpression.class);
	}

	@Nullable
	private CSharpReferenceExpression getReferenceExpressionByStub()
	{
		CSharpWithStringValueStub<CSharpUserType> stub = getStub();
		if(stub != null)
		{
			String referenceText = stub.getReferenceText();
			if(referenceText == null)
			{
				return null;
			}

			CSharpFragmentFileImpl typeFragment = CSharpFragmentFactory.createTypeFragment(getProject(), referenceText, this);
			CSharpUserType dotNetType = (CSharpUserType) PsiTreeUtil.getChildOfType(typeFragment, DotNetType.class);
			assert dotNetType != null;
			return dotNetType.getReferenceExpression();
		}
		return getReferenceExpression();
	}
}
