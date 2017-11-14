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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.util.IncorrectOperationException;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.refactoring.CSharpRefactoringUtil;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpStubElements;
import consulo.csharp.lang.psi.CSharpTupleVariable;
import consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.CSharpTupleTypeDeclaration;
import consulo.csharp.lang.psi.impl.stub.CSharpVariableDeclStub;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 26-Nov-16.
 */
public class CSharpStubTupleVariableImpl extends CSharpStubElementImpl<CSharpVariableDeclStub<?>> implements CSharpTupleVariable
{
	private static final CSharpTypeRefCacher<CSharpStubTupleVariableImpl> ourCacheSystem = new CSharpTypeRefCacher<CSharpStubTupleVariableImpl>
			(false)
	{
		@RequiredReadAction
		@NotNull
		@Override
		protected DotNetTypeRef toTypeRefImpl(CSharpStubTupleVariableImpl element, boolean resolveFromParentOrInitializer)
		{
			DotNetType type = element.getType();
			return type == null ? DotNetTypeRef.ERROR_TYPE : type.toTypeRef();
		}
	};

	public CSharpStubTupleVariableImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpStubTupleVariableImpl(@NotNull CSharpVariableDeclStub<?> stub,
			@NotNull IStubElementType<? extends CSharpVariableDeclStub<?>, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitTupleVariable(this);
	}

	@RequiredReadAction
	@Override
	public boolean isConstant()
	{
		return false;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getConstantKeywordElement()
	{
		return null;
	}

	@RequiredReadAction
	@Override
	@Nullable
	public DotNetType getType()
	{
		return getStubOrPsiChildByIndex(CSharpStubElements.TYPE_SET, 0);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetExpression getInitializer()
	{
		return null;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromInitializer)
	{
		return ourCacheSystem.toTypeRef(this, resolveFromInitializer);
	}

	@Override
	@Nullable
	@RequiredReadAction
	public PsiElement getNameIdentifier()
	{
		return getStubOrPsiChild(CSharpStubElements.IDENTIFIER);
	}

	@RequiredReadAction
	@Override
	public int getTextOffset()
	{
		PsiElement nameIdentifier = getNameIdentifier();
		return nameIdentifier != null ? nameIdentifier.getTextOffset() : super.getTextOffset();
	}

	@Override
	@RequiredReadAction
	public String getName()
	{
		return CSharpPsiUtilImpl.getNameWithoutAt(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getNameWithAt()
	{
		return CSharpPsiUtilImpl.getNameWithAt(this);
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		CSharpRefactoringUtil.replaceNameIdentifier(this, s);
		return this;
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(@NotNull DotNetModifier dotNetModifier)
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
	public boolean isEquivalentTo(PsiElement another)
	{
		PsiElement tupleElement = another.getOriginalElement().getUserData(CSharpTupleTypeDeclaration.TUPLE_ELEMENT);
		if(tupleElement != null && super.isEquivalentTo(tupleElement))
		{
			return true;
		}
		return super.isEquivalentTo(another);
	}
}
