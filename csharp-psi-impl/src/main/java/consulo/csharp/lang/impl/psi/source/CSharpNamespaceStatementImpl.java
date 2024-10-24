/*
 * Copyright 2013-2023 consulo.io
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
import consulo.annotation.access.RequiredWriteAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.impl.psi.stub.CSharpNamespaceProviderStub;
import consulo.csharp.lang.psi.CSharpNamespaceStatement;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.dotnet.psi.DotNetNamespaceDeclaration;
import consulo.dotnet.psi.DotNetReferenceExpression;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2023-12-30
 */
public class CSharpNamespaceStatementImpl extends CSharpStubElementImpl<CSharpNamespaceProviderStub<CSharpNamespaceStatementImpl>> implements CSharpNamespaceStatement
{
	public CSharpNamespaceStatementImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpNamespaceStatementImpl(@Nonnull CSharpNamespaceProviderStub<CSharpNamespaceStatementImpl> stub,
										@Nonnull IStubElementType<? extends CSharpNamespaceProviderStub<CSharpNamespaceStatementImpl>, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitNamespaceStatement(this);
	}

	@Override
	public DotNetReferenceExpression getNamespaceReference()
	{
		return findChildByClass(DotNetReferenceExpression.class);
	}

	@Override
	@RequiredWriteAction
	public void setNamespace(@Nonnull String namespace)
	{
		PsiElement referenceToken = CSharpFileFactory.createExpression(getProject(), namespace);

		getNamespaceReference().replace(referenceToken);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		String fullQName = buildQualifiedName();
		if(fullQName == null)
		{
			return null;
		}
		return StringUtil.getPackageName(fullQName);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableQName()
	{
		return buildQualifiedName();
	}

	@RequiredReadAction
	private String buildQualifiedName()
	{
		String parentQualifiedName = null;
		PsiElement parent = getParent();
		if(parent instanceof DotNetNamespaceDeclaration)
		{
			parentQualifiedName = ((DotNetNamespaceDeclaration) parent).getPresentableQName();
		}

		String text = getReferenceText();
		if(!StringUtil.isEmpty(parentQualifiedName))
		{
			return parentQualifiedName + "." + text;
		}
		return text;
	}

	@Nullable
	@RequiredReadAction
	public String getReferenceText()
	{
		CSharpNamespaceProviderStub stub = getGreenStub();
		if(stub != null)
		{
			return stub.getReferenceTextRef();
		}
		CSharpReferenceExpression childByClass = findChildByClass(CSharpReferenceExpression.class);
		return childByClass != null ? StringUtil.strip(childByClass.getText(), CSharpReferenceExpression.DEFAULT_REF_FILTER) : null;
	}
}
