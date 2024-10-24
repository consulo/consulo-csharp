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

import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.util.ArrayUtil2;
import consulo.language.ast.ASTNode;
import consulo.language.ast.TokenSet;
import consulo.language.impl.DebugUtil;
import consulo.language.impl.psi.stub.StubBasedPsiElementBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFile;
import consulo.language.psi.StubBasedPsiElement;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubElement;
import consulo.navigation.ItemPresentation;
import consulo.navigation.ItemPresentationProvider;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public abstract class CSharpStubElementImpl<S extends StubElement> extends StubBasedPsiElementBase<S> implements StubBasedPsiElement<S>
{
	public CSharpStubElementImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T getStubOrPsiChildByIndex(@Nonnull TokenSet set, int index)
	{
		PsiElement[] children = getStubOrPsiChildren(set, PsiElement.ARRAY_FACTORY);
		return (T) ArrayUtil2.safeGet(children, index);
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	public <T> T getRequiredStubOrPsiChildByIndex(@Nonnull TokenSet set, int index)
	{
		T element = getStubOrPsiChildByIndex(set, index);
		if(element == null)
		{
			throw new IllegalArgumentException("Element by set: " + set + " and index:" + index + " is not found. Tree:\n " + DebugUtil.psiToString(this, true));
		}
		return element;
	}

	@Override
	public void deleteChildInternal(@Nonnull ASTNode child)
	{
		PsiFile containingFile = getContainingFile();
		DotNetNamedElement singleElement = CSharpPsiUtilImpl.findSingleElementNoNameCheck((CSharpFileImpl) containingFile);
		if(singleElement != null && singleElement == child.getPsi())
		{
			containingFile.delete();
		}
		else
		{
			super.deleteChildInternal(child);
		}
	}

	@Override
	public ItemPresentation getPresentation()
	{
		return ItemPresentationProvider.getItemPresentation(this);
	}

	public CSharpStubElementImpl(@Nonnull S stub, @Nonnull IStubElementType<? extends S, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@Nonnull PsiElementVisitor visitor)
	{
		if(visitor instanceof CSharpElementVisitor)
		{
			accept((CSharpElementVisitor) visitor);
		}
		else
		{
			super.accept(visitor);
		}
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [" + getNode().getElementType() + "]";
	}

	public abstract void accept(@Nonnull CSharpElementVisitor visitor);
}
