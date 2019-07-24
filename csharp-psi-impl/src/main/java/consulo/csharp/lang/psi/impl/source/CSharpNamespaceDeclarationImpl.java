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
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import consulo.annotations.RequiredReadAction;
import consulo.annotations.RequiredWriteAction;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.stub.CSharpNamespaceDeclStub;
import consulo.dotnet.psi.*;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpNamespaceDeclarationImpl extends CSharpStubElementImpl<CSharpNamespaceDeclStub> implements CSharpNamespaceDeclaration
{
	public CSharpNamespaceDeclarationImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpNamespaceDeclarationImpl(@Nonnull CSharpNamespaceDeclStub stub)
	{
		super(stub, CSharpStubElements.NAMESPACE_DECLARATION);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitNamespaceDeclaration(this);
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(@Nonnull DotNetModifier modifier)
	{
		return false;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return null;
	}

	@Override
	@RequiredReadAction
	public String getName()
	{
		String qName = getPresentableQName();
		if(qName == null)
		{
			return null;
		}
		return StringUtil.getShortName(qName);
	}

	@RequiredWriteAction
	@Override
	public PsiElement setName(@NonNls @Nonnull String s) throws IncorrectOperationException
	{
		return null;
	}

	@RequiredReadAction
	@Override
	public PsiElement getLeftBrace()
	{
		return findChildByType(CSharpTokens.LBRACE);
	}

	@RequiredReadAction
	@Override
	public PsiElement getRightBrace()
	{
		return findChildByType(CSharpTokens.RBRACE);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetQualifiedElement[] getMembers()
	{
		return getStubOrPsiChildren(CSharpStubElements.QUALIFIED_MEMBERS, DotNetQualifiedElement.ARRAY_FACTORY);
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
		CSharpNamespaceDeclStub stub = getGreenStub();
		if(stub != null)
		{
			return stub.getReferenceTextRef();
		}
		CSharpReferenceExpression childByClass = findChildByClass(CSharpReferenceExpression.class);
		return childByClass != null ? StringUtil.strip(childByClass.getText(), CSharpReferenceExpression.DEFAULT_REF_FILTER) : null;
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public CSharpUsingListChild[] getUsingStatements()
	{
		return getStubOrPsiChildren(CSharpStubElements.USING_CHILDREN, CSharpUsingListChild.ARRAY_FACTORY);
	}
}
