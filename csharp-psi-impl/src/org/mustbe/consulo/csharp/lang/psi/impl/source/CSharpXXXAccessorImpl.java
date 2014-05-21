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
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightLocalVariableBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpXXXAccessorStub;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 04.12.13.
 */
public class CSharpXXXAccessorImpl extends CSharpStubMemberImpl<CSharpXXXAccessorStub> implements DotNetXXXAccessor
{
	public CSharpXXXAccessorImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpXXXAccessorImpl(@NotNull CSharpXXXAccessorStub stub)
	{
		super(stub, CSharpStubElements.XXX_ACCESSOR);
	}

	@Override
	@Nullable
	public DotNetModifierList getModifierList()
	{
		return findChildByClass(DotNetModifierList.class);
	}

	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		CSharpXXXAccessorStub stub = getStub();
		if(stub != null)
		{
			return stub.hasModifier(modifier);
		}
		DotNetModifierList modifierList = getModifierList();
		return modifierList != null && modifierList.hasModifier(modifier);
	}

	@NotNull
	@Override
	public PsiElement getNameIdentifier()
	{
		return findNotNullChildByType(CSharpTokenSets.XXX_ACCESSOR_START);
	}

	@Override
	public boolean processDeclarations(
			@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place)
	{
		if(getAccessorType() == CSharpSoftTokens.SET_KEYWORD)
		{
			CSharpPropertyDeclaration propertyDeclaration = PsiTreeUtil.getParentOfType(this, CSharpPropertyDeclaration.class);
			if(propertyDeclaration == null)
			{
				return true;
			}

			CSharpLightLocalVariableBuilder builder = new CSharpLightLocalVariableBuilder(propertyDeclaration).withName(VALUE).withParent(this)
					.withTypeRef(propertyDeclaration.toTypeRef(true));

			if(!processor.execute(builder, state))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitXXXAccessor(this);
	}

	@NotNull
	@Override
	public IElementType getAccessorType()
	{
		CSharpXXXAccessorStub stub = getStub();
		if(stub != null)
		{
			return stub.getAccessorType();
		}
		return getNameIdentifier().getNode().getElementType();
	}

	@Nullable
	@Override
	public PsiElement getCodeBlock()
	{
		return findChildByClass(DotNetStatement.class);
	}
}
