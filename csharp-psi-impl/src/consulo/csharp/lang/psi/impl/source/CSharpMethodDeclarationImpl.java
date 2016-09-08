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

package consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpGenericConstraintList;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpStubElements;
import consulo.csharp.lang.psi.CSharpTokenSets;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpOperatorNameHelper;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import consulo.csharp.lang.psi.impl.stub.CSharpMethodDeclStub;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.BitUtil;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpMethodDeclarationImpl extends CSharpLikeMethodDeclarationImpl<CSharpMethodDeclStub> implements CSharpMethodDeclaration
{
	public CSharpMethodDeclarationImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpMethodDeclarationImpl(@NotNull CSharpMethodDeclStub stub, @NotNull IStubElementType<? extends CSharpMethodDeclStub, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitMethodDeclaration(this);
	}

	@RequiredReadAction
	@Override
	@Nullable
	public PsiElement getNameIdentifier()
	{
		if(isOperator())
		{
			return findChildByFilter(CSharpTokenSets.OVERLOADING_OPERATORS);
		}
		return getStubOrPsiChild(CSharpStubElements.IDENTIFIER);
	}

	@RequiredReadAction
	@Override
	public String getName()
	{
		if(isOperator())
		{
			IElementType operatorElementType = getOperatorElementType();
			if(operatorElementType == null)
			{
				return "<error-operator>";
			}
			return CSharpOperatorNameHelper.getOperatorName(operatorElementType);
		}
		return super.getName();
	}

	@Override
	public boolean isDelegate()
	{
		CSharpMethodDeclStub stub = getStub();
		if(stub != null)
		{
			return BitUtil.isSet(stub.getOtherModifierMask(), CSharpMethodDeclStub.DELEGATE_MASK);
		}
		return findChildByType(CSharpTokens.DELEGATE_KEYWORD) != null;
	}

	@Override
	public boolean isOperator()
	{
		CSharpMethodDeclStub stub = getStub();
		if(stub != null)
		{
			return stub.getOperator() != null;
		}
		return findChildByType(CSharpTokens.OPERATOR_KEYWORD) != null;
	}

	@Override
	public boolean isExtension()
	{
		CSharpMethodDeclStub stub = getStub();
		if(stub != null)
		{
			return BitUtil.isSet(stub.getOtherModifierMask(), CSharpMethodDeclStub.EXTENSION_MASK);
		}
		return CSharpMethodImplUtil.isExtensionMethod(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public IElementType getOperatorElementType()
	{
		CSharpMethodDeclStub stub = getStub();
		if(stub != null)
		{
			return  stub.getOperator();
		}
		PsiElement element = findChildByType(CSharpTokenSets.OVERLOADING_OPERATORS);
		return PsiUtilCore.getElementType(element);
	}

	@Override
	public boolean isEquivalentTo(PsiElement another)
	{
		return CSharpLikeMethodDeclarationImplUtil.isEquivalentTo(this, another);
	}

	@Nullable
	@Override
	public CSharpGenericConstraintList getGenericConstraintList()
	{
		return getStubOrPsiChild(CSharpStubElements.GENERIC_CONSTRAINT_LIST);
	}

	@NotNull
	@Override
	public CSharpGenericConstraint[] getGenericConstraints()
	{
		CSharpGenericConstraintList genericConstraintList = getGenericConstraintList();
		return genericConstraintList == null ? CSharpGenericConstraint.EMPTY_ARRAY : genericConstraintList.getGenericConstraints();
	}

	@Nullable
	@Override
	public DotNetType getTypeForImplement()
	{
		return getStubOrPsiChildByIndex(CSharpStubElements.TYPE_SET, 1);
	}

	@NotNull
	@Override
	public DotNetTypeRef getTypeRefForImplement()
	{
		DotNetType typeForImplement = getTypeForImplement();
		if(typeForImplement == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}
		else
		{
			return typeForImplement.toTypeRef();
		}
	}
}
