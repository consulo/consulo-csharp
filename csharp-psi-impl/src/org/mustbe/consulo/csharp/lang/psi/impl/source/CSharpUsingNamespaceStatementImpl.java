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

import org.consulo.lombok.annotations.ArrayFactoryFields;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpNamespaceAsElement;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpNamespaceHelper;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpUsingNamespaceStatementStub;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceExpression;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.CharFilter;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
@ArrayFactoryFields
public class CSharpUsingNamespaceStatementImpl extends CSharpStubElementImpl<CSharpUsingNamespaceStatementStub> implements CSharpUsingListChild
{
	public CSharpUsingNamespaceStatementImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpUsingNamespaceStatementImpl(@NotNull CSharpUsingNamespaceStatementStub stub)
	{
		super(stub, CSharpStubElements.USING_NAMESPACE_STATEMENT);
	}

	@Nullable
	public String getReferenceText()
	{
		CSharpUsingNamespaceStatementStub stub = getStub();
		if(stub != null)
		{
			return stub.getReferenceText();
		}

		DotNetReferenceExpression namespaceReference = getNamespaceReference();
		return namespaceReference == null ? null : namespaceReference.getText();
	}

	@Nullable
	public CSharpNamespaceAsElement resolve()
	{
		String referenceText = getReferenceText();
		if(referenceText == null)
		{
			return null;
		}
		String qName = StringUtil.strip(referenceText, CharFilter.NOT_WHITESPACE_FILTER);
		return CSharpNamespaceHelper.getNamespaceElementIfFind(getProject(), qName, getResolveScope());
	}

	public DotNetReferenceExpression getNamespaceReference()
	{
		return findChildByClass(DotNetReferenceExpression.class);
	}

	@Override
	public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement
			place)
	{
		CSharpNamespaceAsElement resolve = resolve();

		return resolve == null || resolve.processDeclarations(processor, state, lastParent, place);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitUsingNamespaceStatement(this);
	}

	@Nullable
	@Override
	public PsiElement getReferenceElement()
	{
		return getNamespaceReference();
	}
}
