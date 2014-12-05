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
import org.mustbe.consulo.csharp.ide.CSharpLookupElementBuilder;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.cache.CSharpResolveCache;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
@Logger
public class CSharpReferenceExpressionImpl extends CSharpElementImpl implements CSharpReferenceExpression, PsiPolyVariantReference,
		CSharpQualifiedNonReference
{
	private static class OurResolver implements CSharpResolveCache.PolyVariantResolver<CSharpReferenceExpressionImpl>
	{
		private static final OurResolver INSTANCE = new OurResolver();

		@NotNull
		@Override
		public ResolveResult[] resolve(@NotNull CSharpReferenceExpressionImpl ref, boolean incompleteCode, boolean resolveFromParent)
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

	public CSharpReferenceExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
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
		return findChildByClass(DotNetExpression.class);
	}

	@Nullable
	@Override
	public String getReferenceName()
	{
		PsiElement referenceElement = getReferenceElement();
		return referenceElement == null ? null : CSharpPsiUtilImpl.getNameWithoutAt(referenceElement.getText());
	}

	@Override
	public PsiElement getElement()
	{
		return this;
	}

	@Override
	public TextRange getRangeInElement()
	{
		PsiElement referenceElement = getReferenceElement();
		if(referenceElement == null)
		{
			return TextRange.EMPTY_RANGE;
		}

		PsiElement qualifier = getQualifier();
		int startOffset = qualifier != null ? qualifier.getTextLength() + 1 : 0;
		return new TextRange(startOffset, referenceElement.getTextLength() + startOffset);
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(final boolean incompleteCode)
	{
		return multiResolve(incompleteCode, true);
	}

	@NotNull
	public ResolveResult[] multiResolve(final boolean incompleteCode, final boolean resolveFromParent)
	{
		return CSharpResolveCache.getInstance(getProject()).resolveWithCaching(this, OurResolver.INSTANCE, true, incompleteCode, resolveFromParent);
	}

	@NotNull
	public ResolveResult[] multiResolveImpl(ResolveToKind kind, boolean resolveFromParent)
	{
		CSharpCallArgumentListOwner p = null;
		PsiElement parent = getParent();

		if(kind == ResolveToKind.CONSTRUCTOR || kind == ResolveToKind.PARAMETER)
		{
			p = PsiTreeUtil.getParentOfType(this, CSharpCallArgumentListOwner.class);
		}
		else if(parent instanceof CSharpCallArgumentListOwner)
		{
			p = (CSharpCallArgumentListOwner) parent;
		}
		return CSharpReferenceExpressionImplUtil.multiResolve0(kind, p, this, resolveFromParent);
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
		ResolveToKind kind = kind();
		if(kind != ResolveToKind.LABEL && kind != ResolveToKind.QUALIFIED_NAMESPACE && kind != ResolveToKind.SOFT_QUALIFIED_NAMESPACE)
		{
			kind = ResolveToKind.ANY_MEMBER;
		}
		ResolveResult[] psiElements = CSharpReferenceExpressionImplUtil.collectResults(kind, null, this, null, true, true);
		return CSharpLookupElementBuilder.getInstance(getProject()).buildToLookupElements(this, psiElements);
	}

	@Override
	public boolean isSoft()
	{
		return kind() == ResolveToKind.SOFT_QUALIFIED_NAMESPACE;
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromParent)
	{
		ResolveResult[] resolveResults = multiResolve(false, resolveFromParent);
		if(resolveResults.length == 0)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		ResolveResult resolveResult = CSharpResolveUtil.findFirstValidResult(resolveResults);
		if(resolveResult == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}
		return CSharpReferenceExpressionImplUtil.toTypeRef(resolveResult);
	}

	@NotNull
	public DotNetTypeRef toTypeRefWithoutCaching(ResolveToKind kind, boolean resolveFromParent)
	{
		ResolveResult[] resolveResults = multiResolveImpl(kind, resolveFromParent);
		if(resolveResults.length == 0)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		ResolveResult firstValidResult = CSharpResolveUtil.findFirstValidResult(resolveResults);
		if(firstValidResult == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}
		return CSharpReferenceExpressionImplUtil.toTypeRef(firstValidResult);
	}

}
