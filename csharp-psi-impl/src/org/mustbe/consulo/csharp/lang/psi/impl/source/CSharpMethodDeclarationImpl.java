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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraint;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintList;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpMethodStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.BitUtil;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpMethodDeclarationImpl extends CSharpLikeMethodDeclarationImpl implements CSharpMethodDeclaration
{
	public CSharpMethodDeclarationImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpMethodDeclarationImpl(@NotNull CSharpMethodStub stub, @NotNull IStubElementType<? extends CSharpMethodStub, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitMethodDeclaration(this);
	}

	@Override
	@Nullable
	public PsiElement getNameIdentifier()
	{
		if(isOperator())
		{
			return findChildByFilter(CSharpTokenSets.OVERLOADING_OPERATORS);
		}
		return findChildByType(CSharpTokens.IDENTIFIER);
	}

	@Override
	public boolean isDelegate()
	{
		CSharpMethodStub stub = getStub();
		if(stub != null)
		{
			return BitUtil.isSet(stub.getOtherModifierMask(), CSharpMethodStub.DELEGATE_MASK);
		}
		return findChildByType(CSharpTokens.DELEGATE_KEYWORD) != null;
	}

	@Override
	public boolean isOperator()
	{
		CSharpMethodStub stub = getStub();
		if(stub != null)
		{
			return stub.getOperator() != null;
		}
		return findChildByType(CSharpTokens.OPERATOR_KEYWORD) != null;
	}

	@Nullable
	@Override
	public IElementType getOperatorElementType()
	{
		CSharpMethodStub stub = getStub();
		if(stub != null)
		{
			return  stub.getOperator();
		}
		PsiElement childByType = findChildByType(CSharpTokenSets.OVERLOADING_OPERATORS);
		return childByType == null ? null : childByType.getNode().getElementType();
	}

	@Nullable
	@Override
	public CSharpGenericConstraintList getGenericConstraintList()
	{
		return findChildByClass(CSharpGenericConstraintList.class);
	}

	@NotNull
	@Override
	public CSharpGenericConstraint[] getGenericConstraints()
	{
		CSharpGenericConstraintList genericConstraintList = getGenericConstraintList();
		return genericConstraintList == null ? CSharpGenericConstraint.EMPTY_ARRAY : genericConstraintList.getGenericConstraints();
	}
}
