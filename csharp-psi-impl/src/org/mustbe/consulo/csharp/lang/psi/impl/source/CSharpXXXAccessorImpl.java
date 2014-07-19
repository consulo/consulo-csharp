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
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpPseudoMethod;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightLocalVariableBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpNativeTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpXXXAccessorStub;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
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
public class CSharpXXXAccessorImpl extends CSharpStubMemberImpl<CSharpXXXAccessorStub> implements DotNetXXXAccessor, CSharpPseudoMethod
{
	public CSharpXXXAccessorImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpXXXAccessorImpl(@NotNull CSharpXXXAccessorStub stub)
	{
		super(stub, CSharpStubElements.XXX_ACCESSOR);
	}

	@NotNull
	@Override
	public DotNetTypeRef[] getParameterTypeRefs()
	{
		return DotNetTypeRef.EMPTY_ARRAY;
	}

	@NotNull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		if(getAccessorKind() == Kind.GET)
		{
			CSharpPropertyDeclaration propertyDeclaration = PsiTreeUtil.getParentOfType(this, CSharpPropertyDeclaration.class);
			if(propertyDeclaration == null)
			{
				return CSharpNativeTypeRef.VOID;
			}
			return propertyDeclaration.toTypeRef(false);
		}
		return CSharpNativeTypeRef.VOID;
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
		if(getAccessorKind() == Kind.SET)
		{
			PsiElement element = PsiTreeUtil.getParentOfType(this, CSharpPropertyDeclaration.class, CSharpArrayMethodDeclaration.class);
			if(element == null)
			{
				return true;
			}

			DotNetTypeRef typeRef = DotNetTypeRef.ERROR_TYPE;
			if(element instanceof CSharpPropertyDeclaration)
			{
				typeRef = ((CSharpPropertyDeclaration) element).toTypeRef(false);
			}
			else if(element instanceof CSharpArrayMethodDeclaration)
			{
				typeRef = ((CSharpArrayMethodDeclaration) element).getReturnTypeRef();
			}

			CSharpLightLocalVariableBuilder builder = new CSharpLightLocalVariableBuilder(element).withName(VALUE).withParent(this)
					.withTypeRef(typeRef);
			builder.putUserData(CSharpResolveUtil.ACCESSOR_VALUE_VARIABLE, Boolean.TRUE);

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

	@Nullable
	@Override
	public PsiElement getCodeBlock()
	{
		return findChildByClass(DotNetStatement.class);
	}

	@Nullable
	@Override
	public PsiElement getAccessorElement()
	{
		return getNameIdentifier();
	}

	@Nullable
	@Override
	public Kind getAccessorKind()
	{
		CSharpXXXAccessorStub stub = getStub();
		if(stub != null)
		{
			return stub.getAccessorType();
		}
		IElementType elementType = getNameIdentifier().getNode().getElementType();
		if(elementType == CSharpSoftTokens.GET_KEYWORD)
		{
			return Kind.GET;
		}
		else if(elementType == CSharpSoftTokens.SET_KEYWORD)
		{
			return Kind.SET;
		}
		else if(elementType == CSharpSoftTokens.ADD_KEYWORD)
		{
			return Kind.ADD;
		}
		else if(elementType == CSharpSoftTokens.REMOVE_KEYWORD)
		{
			return Kind.REMOVE;
		}
		return null;
	}
}
