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

package consulo.csharp.lang.psi.impl.source;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpStaticTypeRef;
import consulo.csharp.lang.psi.impl.stub.CSharpMethodDeclStub;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 09.01.14
 */
public class CSharpConversionMethodDeclarationImpl extends CSharpStubLikeMethodDeclarationImpl<CSharpMethodDeclStub> implements
		CSharpConversionMethodDeclaration, CSharpSimpleLikeMethodAsElement
{
	public CSharpConversionMethodDeclarationImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpConversionMethodDeclarationImpl(@Nonnull CSharpMethodDeclStub stub)
	{
		super(stub, CSharpStubElements.CONVERSION_METHOD_DECLARATION);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return getReturnType();
	}

	@RequiredReadAction
	@Override
	public String getName()
	{
		DotNetType returnType = getReturnType();
		if(returnType == null)
		{
			return null;
		}

		return CSharpTypeRefPresentationUtil.buildText(getReturnTypeRef());
	}

	@Override
	public boolean isImplicit()
	{
		return getConversionTypeRef() == CSharpStaticTypeRef.IMPLICIT;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetType getReturnType()
	{
		return getStubOrPsiChildByIndex(CSharpStubElements.TYPE_SET, 1);
	}

	@Nonnull
	@Override
	public DotNetTypeRef getConversionTypeRef()
	{
		DotNetType conversionType = getConversionType();
		return conversionType == null ? DotNetTypeRef.ERROR_TYPE : conversionType.toTypeRef();
	}

	@Nullable
	@Override
	public DotNetType getConversionType()
	{
		return getStubOrPsiChildByIndex(CSharpStubElements.TYPE_SET, 0);
	}

	@Nullable
	@Override
	public PsiElement getOperatorElement()
	{
		return findChildByType(CSharpTokens.OPERATOR_KEYWORD);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitConversionMethodDeclaration(this);
	}
}
