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
import org.mustbe.consulo.csharp.ide.reflactoring.CSharpRefactoringUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpVariableStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.typeStub.CSharpStubTypeInfoUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpParameterImpl extends CSharpStubElementImpl<CSharpVariableStub<DotNetParameter>> implements DotNetParameter
{
	public CSharpParameterImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpParameterImpl(@NotNull CSharpVariableStub<DotNetParameter> stub)
	{
		super(stub, CSharpStubElements.PARAMETER);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitParameter(this);
	}

	@Override
	public boolean isConstant()
	{
		return false;
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromInitializer)
	{
		CSharpVariableStub<?> stub = getStub();
		if(stub != null)
		{
			return CSharpStubTypeInfoUtil.toTypeRef(stub.getTypeInfo(), this);
		}

		DotNetType type = getType();
		DotNetTypeRef typeRef = type.toTypeRef();
		if(hasModifier(CSharpModifier.REF))
		{
			return new CSharpRefTypeRef(CSharpRefTypeRef.Type.ref, typeRef);
		}
		else if(hasModifier(CSharpModifier.OUT))
		{
			return new CSharpRefTypeRef(CSharpRefTypeRef.Type.out, typeRef);
		}
		return typeRef;
	}

	@NotNull
	@Override
	public DotNetType getType()
	{
		return findNotNullChildByClass(DotNetType.class);
	}

	@Nullable
	@Override
	public DotNetExpression getInitializer()
	{
		return findChildByClass(DotNetExpression.class);
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
		CSharpVariableStub<?> stub = getStub();
		if(stub != null)
		{
			return stub.hasModifier(modifier);
		}
		DotNetModifierList modifierList = getModifierList();
		return modifierList != null && modifierList.hasModifier(modifier);
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return findChildByType(CSharpTokens.IDENTIFIER);
	}

	@Override
	public int getTextOffset()
	{
		PsiElement nameIdentifier = getNameIdentifier();
		return nameIdentifier == null ? super.getTextOffset() : nameIdentifier.getTextOffset();
	}

	@Override
	public String getName()
	{
		CSharpVariableStub<?> stub = getStub();
		if(stub != null)
		{
			return stub.getName();
		}
		return CSharpPsiUtilImpl.getNameWithoutAt(this);
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		CSharpRefactoringUtil.replaceNameIdentifier(this, s);
		return this;
	}

	@NotNull
	@Override
	public SearchScope getUseScope()
	{
		PsiElement parent = getParent();
		if(parent instanceof CSharpParameterListImpl)
		{
			return new LocalSearchScope(parent.getParent());
		}
		return super.getUseScope();
	}

	@NotNull
	@Override
	public DotNetLikeMethodDeclaration getMethod()
	{
		return getStubOrPsiParentOfType(DotNetLikeMethodDeclaration.class);
	}
}
