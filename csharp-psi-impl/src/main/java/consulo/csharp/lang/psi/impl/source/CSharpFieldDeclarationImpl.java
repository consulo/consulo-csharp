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
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpStubElements;
import consulo.csharp.lang.psi.impl.fragment.CSharpFragmentFactory;
import consulo.csharp.lang.psi.impl.fragment.CSharpFragmentFileImpl;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpConstantTypeRef;
import consulo.csharp.lang.psi.impl.stub.CSharpVariableDeclStub;
import consulo.dotnet.psi.*;
import consulo.dotnet.resolve.DotNetTypeRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

/**
 * @author VISTALL
 * @since 04.12.13.
 */
public class CSharpFieldDeclarationImpl extends CSharpStubVariableImpl<CSharpVariableDeclStub<DotNetFieldDeclaration>> implements CSharpFieldDeclaration
{
	private static class FieldConstantRef extends CSharpConstantTypeRef
	{
		public FieldConstantRef(CSharpConstantExpressionImpl element, @Nonnull DotNetTypeRef defaultTypeRef)
		{
			super(element, defaultTypeRef);
		}
	}

	private volatile WeakReference<DotNetExpression> myInitializerExpression;

	public CSharpFieldDeclarationImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpFieldDeclarationImpl(@Nonnull CSharpVariableDeclStub<DotNetFieldDeclaration> stub)
	{
		super(stub, CSharpStubElements.FIELD_DECLARATION);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitFieldDeclaration(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetType getType()
	{
		return CSharpStubVariableImplUtil.getType(this);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromInitializer)
	{
		DotNetTypeRef defaultTypeRef = super.toTypeRefImpl(resolveFromInitializer);
		if(isConstant())
		{
			DotNetExpression initializer = getInitializer();
			if(initializer instanceof CSharpConstantExpressionImpl)
			{
				return new CSharpConstantTypeRef((CSharpConstantExpressionImpl) initializer, defaultTypeRef);
			}
		}
		return defaultTypeRef;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return CSharpStubVariableImplUtil.getModifierList(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getConstantKeywordElement()
	{
		return CSharpStubVariableImplUtil.getConstantKeywordElement(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetExpression getInitializer()
	{
		CSharpVariableDeclStub<DotNetFieldDeclaration> stub = getStub();
		if(stub != null)
		{
			if(myInitializerExpression != null)
			{
				return myInitializerExpression.get();
			}

			String initializerText = stub.getInitializerText();
			if(initializerText != null)
			{
				CSharpFragmentFileImpl expressionFragment = CSharpFragmentFactory.createExpressionFragment(getProject(), initializerText, this);
				DotNetExpression value = (DotNetExpression) expressionFragment.getChildren()[0];
				myInitializerExpression = new WeakReference<>(value);
				return value;
			}

			return null;
		}

		myInitializerExpression = null;
		return findChildByClass(DotNetExpression.class);
	}

	@Override
	protected void clearUserData()
	{
		super.clearUserData();
		myInitializerExpression = null;
	}

	@RequiredReadAction
	@Override
	public boolean isConstant()
	{
		CSharpVariableDeclStub<DotNetFieldDeclaration> stub = getGreenStub();
		if(stub != null)
		{
			return stub.isConstant();
		}
		return CSharpStubVariableImplUtil.getConstantKeywordElement(this) != null;
	}
}
