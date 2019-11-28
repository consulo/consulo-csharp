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
import com.intellij.openapi.util.text.CharFilter;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.stub.CSharpWithStringValueStub;
import consulo.dotnet.psi.DotNetReferenceExpression;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.resolve.DotNetPsiSearcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpUsingNamespaceStatementImpl extends CSharpStubElementImpl<CSharpWithStringValueStub<CSharpUsingNamespaceStatement>> implements CSharpUsingNamespaceStatement
{
	public CSharpUsingNamespaceStatementImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpUsingNamespaceStatementImpl(@Nonnull CSharpWithStringValueStub<CSharpUsingNamespaceStatement> stub)
	{
		super(stub, CSharpStubElements.USING_NAMESPACE_STATEMENT);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public PsiElement getUsingKeywordElement()
	{
		return findNotNullChildByType(CSharpTokens.USING_KEYWORD);
	}

	@RequiredReadAction
	@Override
	@Nullable
	public String getReferenceText()
	{
		CSharpWithStringValueStub<CSharpUsingNamespaceStatement> stub = getGreenStub();
		if(stub != null)
		{
			return stub.getReferenceText();
		}

		DotNetReferenceExpression namespaceReference = getNamespaceReference();
		return namespaceReference == null ? null : StringUtil.strip(namespaceReference.getText(), CharFilter.NOT_WHITESPACE_FILTER);
	}

	@RequiredReadAction
	@Override
	@Nullable
	public DotNetNamespaceAsElement resolve()
	{
		return CachedValuesManager.getCachedValue(this, () -> CachedValueProvider.Result.create(resolveInner(), PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT));
	}

	@Nullable
	@RequiredReadAction
	private DotNetNamespaceAsElement resolveInner()
	{
		String referenceText = getReferenceText();
		if(referenceText == null)
		{
			return null;
		}

		final String qName = StringUtil.strip(referenceText, CSharpReferenceExpression.DEFAULT_REF_FILTER);

		PsiElement parent = getParent();
		DotNetPsiSearcher psiSearcher = DotNetPsiSearcher.getInstance(getProject());
		if(parent instanceof CSharpNamespaceDeclaration)
		{
			String newNamespaceName = ((CSharpNamespaceDeclaration) parent).getPresentableQName() + "." + qName;
			DotNetNamespaceAsElement namespace = psiSearcher.findNamespace(newNamespaceName, getResolveScope());
			if(namespace != null)
			{
				return namespace;
			}
		}

		return psiSearcher.findNamespace(qName, getResolveScope());
	}

	@Override
	public DotNetReferenceExpression getNamespaceReference()
	{
		return findChildByClass(DotNetReferenceExpression.class);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitUsingNamespaceStatement(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getReferenceElement()
	{
		return getNamespaceReference();
	}
}
