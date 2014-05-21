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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpNamespaceDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpNamespaceStub;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetNamespaceDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceExpression;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpNamespaceDeclarationImpl extends CSharpStubElementImpl<CSharpNamespaceStub> implements CSharpNamespaceDeclaration
{
	public CSharpNamespaceDeclarationImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpNamespaceDeclarationImpl(@NotNull CSharpNamespaceStub stub)
	{
		super(stub, CSharpStubElements.NAMESPACE_DECLARATION);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitNamespaceDeclaration(this);
	}

	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		DotNetModifierList modifierList = getModifierList();
		return modifierList != null && modifierList.hasModifier(modifier);
	}

	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return findChildByClass(DotNetModifierList.class);
	}

	@Override
	public String getName()
	{
		CSharpNamespaceStub stub = getStub();
		if(stub != null)
		{
			return stub.getName();
		}
		String presentableQName0 = getQNameFromDecl();

		if(StringUtil.isEmpty(presentableQName0))
		{
			return null;
		}
		else
		{
			return StringUtil.getShortName(presentableQName0);
		}
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public PsiElement getLeftBrace()
	{
		return findChildByType(CSharpTokens.LBRACE);
	}

	@Override
	public PsiElement getRightBrace()
	{
		return findChildByType(CSharpTokens.RBRACE);
	}

	@NotNull
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

	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		CSharpNamespaceStub stub = getStub();
		if(stub != null)
		{
			return stub.getParentQName();
		}

		String fullQName = null;
		PsiElement parent = getParent();
		if(parent instanceof DotNetNamespaceDeclaration)
		{
			fullQName = ((DotNetNamespaceDeclaration) parent).getPresentableQName() + getQNameFromDecl();
		}
		else
		{
			fullQName = getQNameFromDecl();
		}

		if(fullQName == null)
		{
			return null;
		}

		return StringUtil.getPackageName(fullQName);
	}

	@Nullable
	@Override
	public String getPresentableQName()
	{
		String str = getPresentableParentQName();
		if(!StringUtil.isEmpty(str))
		{
			return str + "." + getName();
		}
		return getName();
	}

	@Nullable
	public String getQNameFromDecl()
	{
		CSharpReferenceExpressionImpl childByClass = findChildByClass(CSharpReferenceExpressionImpl.class);
		return childByClass != null ? childByClass.getText() : null;
	}

	@Override
	public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent,
			@NotNull PsiElement place)
	{
		return CSharpResolveUtil.walkChildren(processor, this, false, place, state);
	}
}
