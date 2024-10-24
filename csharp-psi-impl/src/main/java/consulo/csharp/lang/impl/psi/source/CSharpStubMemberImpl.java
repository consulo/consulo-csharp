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

package consulo.csharp.lang.impl.psi.source;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.ide.refactoring.CSharpRefactoringUtil;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.impl.psi.stub.MemberStub;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.CSharpNamedElement;
import consulo.csharp.lang.psi.CSharpNamespaceStatement;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.language.ast.ASTNode;
import consulo.language.psi.*;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.util.IncorrectOperationException;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public abstract class CSharpStubMemberImpl<S extends MemberStub<?>> extends CSharpStubElementImpl<S> implements PsiNameIdentifierOwner,
		DotNetModifierListOwner, DotNetQualifiedElement, ContributedReferenceHost, CSharpNamedElement
{
	public CSharpStubMemberImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpStubMemberImpl(@Nonnull S stub, @Nonnull IStubElementType<? extends S, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Nonnull
	@Override
	public PsiReference[] getReferences()
	{
		return PsiReferenceService.getService().getContributedReferences(this);
	}

	@RequiredReadAction
	@Override
	@Nullable
	public DotNetModifierList getModifierList()
	{
		return getStubOrPsiChild(CSharpStubElements.MODIFIER_LIST);
	}

	@Override
	@RequiredReadAction
	public boolean hasModifier(@Nonnull DotNetModifier modifier)
	{
		DotNetModifierList modifierList = getModifierList();
		return modifierList != null && modifierList.hasModifier(modifier);
	}

	@RequiredReadAction
	public void addModifier(@Nonnull DotNetModifier modifier)
	{
		DotNetModifierList modifierList = getModifierList();
		if(modifierList != null)
		{
			modifierList.addModifier(modifier);
		}
	}

	@Override
	@Nullable
	@RequiredReadAction
	public PsiElement getNameIdentifier()
	{
		return getStubOrPsiChild(CSharpStubElements.IDENTIFIER);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableQName()
	{
		String parentQName = getPresentableParentQName();
		if(StringUtil.isEmpty(parentQName))
		{
			return getName();
		}
		return parentQName + "." + getName();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		S stub = getGreenStub();
		if(stub != null)
		{
			return stub.getParentQName();
		}
		PsiElement parent = getParent();
		if(parent instanceof DotNetQualifiedElement)
		{
			return ((DotNetQualifiedElement) parent).getPresentableQName();
		}
		else if(parent instanceof CSharpFile file)
		{
			CSharpNamespaceStatement namespaceStatement = file.getNamespaceStatement();
			if(namespaceStatement != null)
			{
				return namespaceStatement.getPresentableQName();
			}
		}
		return "";
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
	public PsiElement setName(@Nonnull String s) throws IncorrectOperationException
	{
		CSharpRefactoringUtil.replaceNameIdentifier(this, s);
		return this;
	}
}
