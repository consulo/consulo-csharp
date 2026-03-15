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
import consulo.language.psi.ResolveResult;
import consulo.language.ast.IElementType;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiReference;
import consulo.language.util.IncorrectOperationException;

import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpReferenceExpressionImpl extends CSharpExpressionImpl implements CSharpReferenceExpressionEx
{
	public CSharpReferenceExpressionImpl(IElementType elementType)
	{
		super(elementType);
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
		return findPsiChildByType(CSharpReferenceExpressionImplUtil.ourReferenceElements);
	}

	@Override
	public void accept(CSharpElementVisitor visitor)
	{
		visitor.visitReferenceExpression(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetExpression getQualifier()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getReferenceName()
	{
		String referenceNameWithAt = getReferenceNameWithAt();
		return referenceNameWithAt == null ? null : CSharpPsiUtilImpl.getNameWithoutAt(referenceNameWithAt);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getReferenceNameWithAt()
	{
		PsiElement referenceElement = getReferenceElement();
		return referenceElement == null ? null : referenceElement.getText();
	}

	@Override
	public PsiElement getElement()
	{
		return this;
	}

	@Override
	@RequiredReadAction
	public TextRange getRangeInElement()
	{
		return CSharpReferenceExpressionImplUtil.getRangeInElement(this);
	}

	@Override
	@RequiredReadAction
	public ResolveResult[] multiResolve(final boolean incompleteCode)
	{
		return multiResolve(incompleteCode, true);
	}

	@RequiredReadAction
	@Override
	public ResolveResult[] multiResolve(final boolean incompleteCode, final boolean resolveFromParent)
	{
		return CSharpReferenceExpressionImplUtil.multiResolve(this, incompleteCode, resolveFromParent);
	}

	@RequiredReadAction
	@Override
	public ResolveResult[] multiResolveImpl(ResolveToKind kind, boolean resolveFromParent)
	{
		return CSharpReferenceExpressionImplUtil.multiResolveImpl(kind, CSharpReferenceExpressionImplUtil.findCallArgumentListOwner(kind, this), this, resolveFromParent);
	}

	@RequiredReadAction
	@Override
	public ResolveResult[] tryResolveFromQualifier(PsiElement element)
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
	public ResolveToKind kind()
	{
		return CSharpReferenceExpressionImplUtil.kind(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetTypeList getTypeArgumentList()
	{
		return findChildByClass(DotNetTypeList.class);
	}

	@RequiredReadAction
	@Override
	public DotNetTypeRef[] getTypeArgumentListRefs()
	{
		DotNetTypeList typeArgumentList = getTypeArgumentList();
		return typeArgumentList == null ? DotNetTypeRef.EMPTY_ARRAY : typeArgumentList.getTypeRefs();
	}

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
		PsiElement token = CSharpFileFactory.createReferenceToken(getProject(), s);
		element.replace(token);
		return this;
	}

	@RequiredWriteAction
	@Override
	public PsiElement bindToElement(PsiElement element) throws IncorrectOperationException
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
	@Override
	public boolean isGlobalElement()
	{
		PsiElement referenceElement = getReferenceElement();
		return referenceElement != null && referenceElement.getNode().getElementType() == CSharpSoftTokens.GLOBAL_KEYWORD;
	}

	@Nullable
	@Override
	@RequiredReadAction
	public PsiElement getMemberAccessElement()
	{
		return findPsiChildByType(CSharpReferenceExpressionImplUtil.ourAccessTokens);
	}

	@Override
	@RequiredReadAction
	public AccessType getMemberAccessType()
	{
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

	@Override
	@RequiredReadAction
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		return CSharpReferenceExpressionImplUtil.toTypeRef(this, resolveFromParent);
	}

	@RequiredReadAction
	@Override
	public DotNetTypeRef toTypeRefWithoutCaching(ResolveToKind kind, boolean resolveFromParent)
	{
		return CSharpReferenceExpressionImplUtil.toTypeRefWithoutCaching(this, kind, resolveFromParent);
	}
}
