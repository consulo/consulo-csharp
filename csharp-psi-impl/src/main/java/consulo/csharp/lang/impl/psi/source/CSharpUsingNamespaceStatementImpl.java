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

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.impl.psi.stub.CSharpUsingNamespaceStub;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.psi.DotNetReferenceExpression;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.psi.resolve.DotNetPsiSearcher;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.language.psi.util.QualifiedName;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpUsingNamespaceStatementImpl extends CSharpStubElementImpl<CSharpUsingNamespaceStub> implements CSharpUsingNamespaceStatement
{
	public static final String GLOBAL_PREFIX = "global::";

	public CSharpUsingNamespaceStatementImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpUsingNamespaceStatementImpl(@Nonnull CSharpUsingNamespaceStub stub)
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
		CSharpUsingNamespaceStub stub = getGreenStub();
		if(stub != null)
		{
			return stub.getReferenceText();
		}

		DotNetReferenceExpression namespaceReference = getNamespaceReference();
		return namespaceReference == null ? null : StringUtil.strip(namespaceReference.getText(), CSharpReferenceExpression.DEFAULT_REF_FILTER);
	}

	@RequiredReadAction
	@Override
	@Nullable
	public DotNetNamespaceAsElement resolve()
	{
		return LanguageCachedValueUtil.getCachedValue(this, () -> CachedValueProvider.Result.create(resolveInner(), PsiModificationTracker.MODIFICATION_COUNT));
	}

	@RequiredReadAction
	@Override
	public boolean isGlobal()
	{
		CSharpUsingNamespaceStub stub = getStub();
		if(stub != null)
		{
			return stub.isGlobal();
		}
		return findChildByType(CSharpSoftTokens.GLOBAL_KEYWORD) != null;
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

		DotNetPsiSearcher psiSearcher = DotNetPsiSearcher.getInstance(getProject());

		String qName = StringUtil.strip(referenceText, CSharpReferenceExpression.DEFAULT_REF_FILTER);
		boolean isGlobal = qName.startsWith(GLOBAL_PREFIX);
		if(isGlobal)
		{
			qName = StringUtil.trimStart(qName, GLOBAL_PREFIX);
			return psiSearcher.findNamespace(qName, getResolveScope());
		}

		PsiElement parent = getParent();
		if(parent instanceof CSharpNamespaceDeclaration)
		{
			DotNetNamespaceAsElement namespace = tryResolveRelativeNamespace((CSharpNamespaceDeclaration) parent, qName, psiSearcher);
			if(namespace != null)
			{
				return namespace;
			}
		}

		return psiSearcher.findNamespace(qName, getResolveScope());
	}

	@Nullable
	@RequiredReadAction
	private DotNetNamespaceAsElement tryResolveRelativeNamespace(@Nonnull CSharpNamespaceDeclaration parent, @Nonnull String text, @Nonnull DotNetPsiSearcher psiSearcher)
	{
		DotNetReferenceExpression namespaceReference = parent.getNamespaceReference();
		if(namespaceReference == null)
		{
			return null;
		}

		QualifiedName target = QualifiedName.fromDottedString(StringUtil.strip(namespaceReference.getText(), CSharpReferenceExpression.DEFAULT_REF_FILTER));

		while(true)
		{
			DotNetNamespaceAsElement namespace = psiSearcher.findNamespace(target.append(text).join("."), getResolveScope());
			if(namespace != null)
			{
				return namespace;
			}

			target = target.getParent();

			if(target == null || target == QualifiedName.ROOT)
			{
				break;
			}
		}

		return null;
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
