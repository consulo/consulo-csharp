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

import java.util.List;

import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpressionEx;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.cache.CSharpResolveCache;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpReferenceExpressionStub;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 05.12.2014
 */
@Logger
public class CSharpStubReferenceExpressionImpl extends CSharpStubElementImpl<CSharpReferenceExpressionStub> implements CSharpReferenceExpressionEx
{
	private static class OurResolver implements CSharpResolveCache.PolyVariantResolver<CSharpStubReferenceExpressionImpl>
	{
		private static final OurResolver INSTANCE = new OurResolver();

		@NotNull
		@Override
		public ResolveResult[] resolve(@NotNull CSharpStubReferenceExpressionImpl ref, boolean incompleteCode, boolean resolveFromParent)
		{
			if(!incompleteCode)
			{
				return ref.multiResolveImpl(ref.kind(), resolveFromParent);
			}
			else
			{
				ResolveResult[] resolveResults = ref.multiResolve(false, resolveFromParent);

				List<ResolveResult> filter = new SmartList<ResolveResult>();
				for(ResolveResult resolveResult : resolveResults)
				{
					if(resolveResult.isValidResult())
					{
						filter.add(resolveResult);
					}
				}
				return ContainerUtil.toArray(filter, ResolveResult.EMPTY_ARRAY);
			}
		}
	}

	public CSharpStubReferenceExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpStubReferenceExpressionImpl(@NotNull CSharpReferenceExpressionStub stub,
			@NotNull IStubElementType<? extends CSharpReferenceExpressionStub, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public PsiReference getReference()
	{
		return this;
	}

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

	@Nullable
	@Override
	public PsiElement getQualifier()
	{
		return getStubOrPsiChild(CSharpStubElements.REFERENCE_EXPRESSION);
	}

	@Nullable
	@Override
	public String getReferenceName()
	{
		String referenceNameWithAt = getReferenceNameWithAt();
		return referenceNameWithAt == null ? null : CSharpPsiUtilImpl.getNameWithoutAt(referenceNameWithAt);
	}

	@Nullable
	@Override
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
	public TextRange getRangeInElement()
	{
		return CSharpReferenceExpressionImplUtil.getRangeInElement(this);
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(final boolean incompleteCode)
	{
		return multiResolve(incompleteCode, true);
	}

	@Override
	@NotNull
	public ResolveResult[] multiResolve(final boolean incompleteCode, final boolean resolveFromParent)
	{
		if(!isValid())
		{
			return ResolveResult.EMPTY_ARRAY;
		}
		return CSharpResolveCache.getInstance(getProject()).resolveWithCaching(this, OurResolver.INSTANCE, true, incompleteCode, resolveFromParent);
	}

	@Override
	@NotNull
	public ResolveResult[] multiResolveImpl(ResolveToKind kind, boolean resolveFromParent)
	{
		return CSharpReferenceExpressionImplUtil.multiResolveImpl(kind, CSharpReferenceExpressionImplUtil.findCallArgumentListOwner(kind, this),
				this, resolveFromParent);
	}

	@NotNull
	@Override
	public ResolveResult[] tryResolveFromQualifier(@NotNull PsiElement element)
	{
		return CSharpReferenceExpressionImplUtil.tryResolveFromQualifier(this, element);
	}

	@Nullable
	@Override
	public PsiElement resolve()
	{
		return CSharpResolveUtil.findFirstValidElement(multiResolve(false));
	}

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
	public String getCanonicalText()
	{
		return getText();
	}

	@Override
	public PsiElement handleElementRename(String s) throws IncorrectOperationException
	{
		PsiElement element = getReferenceElement();

		PsiElement newIdentifier = CSharpFileFactory.createIdentifier(getProject(), s);

		element.replace(newIdentifier);
		return this;
	}

	@Override
	public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException
	{
		return this;
	}

	@Override
	public boolean isReferenceTo(PsiElement element)
	{
		PsiElement resolve = resolve();
		if(element instanceof DotNetNamespaceAsElement && resolve instanceof DotNetNamespaceAsElement)
		{
			return Comparing.equal(((DotNetNamespaceAsElement) resolve).getPresentableQName(), ((DotNetNamespaceAsElement) element)
					.getPresentableQName());
		}
		return element.getManager().areElementsEquivalent(element, resolve);
	}

	@NotNull
	@Override
	public Object[] getVariants()
	{
		return ArrayUtil.EMPTY_OBJECT_ARRAY;
	}

	@Override
	public boolean isSoft()
	{
		return kind() == ResolveToKind.SOFT_QUALIFIED_NAMESPACE;
	}

	@Nullable
	@Override
	public DotNetTypeList getTypeArgumentList()
	{
		return getStubOrPsiChild(CSharpStubElements.TYPE_ARGUMENTS);
	}

	@NotNull
	@Override
	public DotNetTypeRef[] getTypeArgumentListRefs()
	{
		DotNetTypeList typeArgumentList = getTypeArgumentList();
		return typeArgumentList == null ? DotNetTypeRef.EMPTY_ARRAY : typeArgumentList.getTypeRefs();
	}

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

	@Nullable
	@Override
	public PsiElement getMemberAccessElement()
	{
		return findChildByType(CSharpReferenceExpressionImplUtil.ourAccessTokens);
	}

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
		return AccessType.DOT;
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromParent)
	{
		return CSharpReferenceExpressionImplUtil.toTypeRef(this, resolveFromParent);
	}

	@Override
	@NotNull
	public DotNetTypeRef toTypeRefWithoutCaching(ResolveToKind kind, boolean resolveFromParent)
	{
		return CSharpReferenceExpressionImplUtil.toTypeRefWithoutCaching(this, kind, resolveFromParent);
	}
}
