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

package consulo.csharp.lang.impl.psi.source;

import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.ResolveResult;
import consulo.language.ast.IElementType;
import consulo.language.util.IncorrectOperationException;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.impl.psi.stub.CSharpReferenceExpressionStub;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.IStubElementType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 05.12.2014
 */
public class CSharpStubReferenceExpressionImpl extends CSharpStubElementImpl<CSharpReferenceExpressionStub> implements CSharpReferenceExpressionEx
{
	public CSharpStubReferenceExpressionImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpStubReferenceExpressionImpl(@Nonnull CSharpReferenceExpressionStub stub, @Nonnull IStubElementType<? extends CSharpReferenceExpressionStub, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public PsiReference getReference()
	{
		return this;
	}

	@RequiredReadAction
	@Override
	@Nullable
	public PsiElement getReferenceElement()
	{
		return findChildByType(CSharpReferenceExpressionImplUtil.ourReferenceElements);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitReferenceExpression(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetExpression getQualifier()
	{
		return getStubOrPsiChild(CSharpStubElements.REFERENCE_EXPRESSION);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getReferenceName()
	{
		String referenceNameWithAt = getReferenceNameWithAt();
		return referenceNameWithAt == null ? null : CSharpPsiUtilImpl.getNameWithoutAt(referenceNameWithAt);
	}

	@Nullable
	@Override
	@RequiredReadAction
	public String getReferenceNameWithAt()
	{
		CSharpReferenceExpressionStub stub = getGreenStub();
		if(stub != null)
		{
			return stub.getReferenceText();
		}

		PsiElement referenceElement = getReferenceElement();
		return referenceElement == null ? null : referenceElement.getText();
	}

	@Override
	public PsiElement getElement()
	{
		return this;
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public TextRange getRangeInElement()
	{
		return CSharpReferenceExpressionImplUtil.getRangeInElement(this);
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public ResolveResult[] multiResolve(final boolean incompleteCode)
	{
		return multiResolve(incompleteCode, true);
	}

	@RequiredReadAction
	@Override
	@Nonnull
	public ResolveResult[] multiResolve(final boolean incompleteCode, final boolean resolveFromParent)
	{
		return CSharpReferenceExpressionImplUtil.multiResolve(this, incompleteCode, resolveFromParent);
	}

	@RequiredReadAction
	@Override
	@Nonnull
	public ResolveResult[] multiResolveImpl(ResolveToKind kind, boolean resolveFromParent)
	{
		return CSharpReferenceExpressionImplUtil.multiResolveImpl(kind, CSharpReferenceExpressionImplUtil.findCallArgumentListOwner(kind, this), this, resolveFromParent);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public ResolveResult[] tryResolveFromQualifier(@Nonnull PsiElement element)
	{
		return CSharpReferenceExpressionImplUtil.tryResolveFromQualifier(this, element);
	}

	@Nullable
	@Override
	@RequiredReadAction
	public PsiElement resolve()
	{
		return CSharpResolveUtil.findFirstValidElement(multiResolve(false));
	}

	@RequiredReadAction
	@Override
	@Nonnull
	public ResolveToKind kind()
	{
		CSharpReferenceExpressionStub stub = getGreenStub();
		if(stub != null)
		{
			return stub.getKind();
		}
		return CSharpReferenceExpressionImplUtil.kind(this);
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public String getCanonicalText()
	{
		return getText();
	}

	@Override
	@RequiredWriteAction
	public PsiElement handleElementRename(String s) throws IncorrectOperationException
	{
		PsiElement element = getReferenceElement();

		assert element != null;

		PsiElement newIdentifier = CSharpFileFactory.createReferenceToken(getProject(), s);

		element.replace(newIdentifier);
		return this;
	}

	@RequiredWriteAction
	@Override
	public PsiElement bindToElement(@Nonnull PsiElement element) throws IncorrectOperationException
	{
		return CSharpReferenceExpressionImplUtil.bindToElement(this, element);
	}

	@Override
	@RequiredReadAction
	public boolean isReferenceTo(PsiElement element)
	{
		return CSharpReferenceExpressionImplUtil.isReferenceTo(this, element);
	}

	@Override
	@RequiredReadAction
	public boolean isSoft()
	{
		return CSharpReferenceExpressionImplUtil.isSoft(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetTypeList getTypeArgumentList()
	{
		return getStubOrPsiChild(CSharpStubElements.TYPE_ARGUMENTS);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef[] getTypeArgumentListRefs()
	{
		DotNetTypeList typeArgumentList = getTypeArgumentList();
		return typeArgumentList == null ? DotNetTypeRef.EMPTY_ARRAY : typeArgumentList.getTypeRefs();
	}

	@RequiredReadAction
	@Override
	public boolean isGlobalElement()
	{
		CSharpReferenceExpressionStub stub = getGreenStub();
		if(stub != null)
		{
			return stub.isGlobal();
		}
		PsiElement referenceElement = getReferenceElement();
		return referenceElement != null && referenceElement.getNode().getElementType() == CSharpSoftTokens.GLOBAL_KEYWORD;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getMemberAccessElement()
	{
		return findChildByType(CSharpReferenceExpressionImplUtil.ourAccessTokens);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public AccessType getMemberAccessType()
	{
		CSharpReferenceExpressionStub stub = getGreenStub();
		if(stub != null)
		{
			return stub.getMemberAccessType();
		}

		PsiElement childByType = getMemberAccessElement();
		if(childByType == null)
		{
			return AccessType.NONE;
		}
		IElementType elementType = childByType.getNode().getElementType();
		if(elementType == CSharpTokens.ARROW)
		{
			return AccessType.ARROW;
		}
		else if(elementType == CSharpTokens.COLONCOLON)
		{
			return AccessType.COLONCOLON;
		}
		else if(elementType == CSharpTokens.NULLABE_CALL)
		{
			return AccessType.NULLABLE_CALL;
		}
		else if(elementType == CSharpTokens.PLUS)
		{
			return AccessType.NESTED_TYPE;
		}
		return AccessType.DOT;
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public DotNetTypeRef toTypeRef(boolean resolveFromParent)
	{
		return CSharpReferenceExpressionImplUtil.toTypeRef(this, resolveFromParent);
	}

	@RequiredReadAction
	@Override
	@Nonnull
	public DotNetTypeRef toTypeRefWithoutCaching(ResolveToKind kind, boolean resolveFromParent)
	{
		return CSharpReferenceExpressionImplUtil.toTypeRefWithoutCaching(this, kind, resolveFromParent);
	}
}
