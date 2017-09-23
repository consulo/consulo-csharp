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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import consulo.annotations.RequiredReadAction;
import consulo.annotations.RequiredWriteAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.CSharpReferenceExpressionEx;
import consulo.csharp.lang.psi.CSharpSoftTokens;
import consulo.csharp.lang.psi.CSharpStubElements;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.impl.stub.CSharpReferenceExpressionStub;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 05.12.2014
 */
public class CSharpStubReferenceExpressionImpl extends CSharpStubElementImpl<CSharpReferenceExpressionStub> implements CSharpReferenceExpressionEx
{
	private static final Logger LOGGER = Logger.getInstance(CSharpStubReferenceExpressionImpl.class);

	public CSharpStubReferenceExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpStubReferenceExpressionImpl(@NotNull CSharpReferenceExpressionStub stub, @NotNull IStubElementType<? extends CSharpReferenceExpressionStub, ?> nodeType)
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
	public void accept(@NotNull CSharpElementVisitor visitor)
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
		CSharpReferenceExpressionStub stub = getStub();
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

	@Override
	@RequiredReadAction
	public TextRange getRangeInElement()
	{
		return CSharpReferenceExpressionImplUtil.getRangeInElement(this);
	}

	@NotNull
	@Override
	@RequiredReadAction
	public ResolveResult[] multiResolve(final boolean incompleteCode)
	{
		return multiResolve(incompleteCode, true);
	}

	@RequiredReadAction
	@Override
	@NotNull
	public ResolveResult[] multiResolve(final boolean incompleteCode, final boolean resolveFromParent)
	{
		return CSharpReferenceExpressionImplUtil.multiResolve(this, incompleteCode, resolveFromParent);
	}

	@RequiredReadAction
	@Override
	@NotNull
	public ResolveResult[] multiResolveImpl(ResolveToKind kind, boolean resolveFromParent)
	{
		return CSharpReferenceExpressionImplUtil.multiResolveImpl(kind, CSharpReferenceExpressionImplUtil.findCallArgumentListOwner(kind, this), this, resolveFromParent);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public ResolveResult[] tryResolveFromQualifier(@NotNull PsiElement element)
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
	@NotNull
	public ResolveToKind kind()
	{
		CSharpReferenceExpressionStub stub = getStub();
		if(stub != null)
		{
			return stub.getKind();
		}
		return CSharpReferenceExpressionImplUtil.kind(this);
	}

	@NotNull
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

	@Override
	public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException
	{
		return this;
	}

	@Override
	@RequiredReadAction
	public boolean isReferenceTo(PsiElement element)
	{
		return CSharpReferenceExpressionImplUtil.isReferenceTo(this, element);
	}

	@NotNull
	@Override
	public Object[] getVariants()
	{
		return ArrayUtil.EMPTY_OBJECT_ARRAY;
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
	@NotNull
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
		CSharpReferenceExpressionStub stub = getStub();
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
	@NotNull
	@Override
	public AccessType getMemberAccessType()
	{
		CSharpReferenceExpressionStub stub = getStub();
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

	@NotNull
	@Override
	@RequiredReadAction
	public DotNetTypeRef toTypeRef(boolean resolveFromParent)
	{
		return CSharpReferenceExpressionImplUtil.toTypeRef(this, resolveFromParent);
	}

	@RequiredReadAction
	@Override
	@NotNull
	public DotNetTypeRef toTypeRefWithoutCaching(ResolveToKind kind, boolean resolveFromParent)
	{
		return CSharpReferenceExpressionImplUtil.toTypeRefWithoutCaching(this, kind, resolveFromParent);
	}
}
