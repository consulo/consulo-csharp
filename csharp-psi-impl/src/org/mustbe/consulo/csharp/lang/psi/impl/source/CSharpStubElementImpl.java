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
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.util.ArrayUtil2;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProviders;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.impl.PsiTreeDebugBuilder;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.TokenSet;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public abstract class CSharpStubElementImpl<S extends StubElement> extends StubBasedPsiElementBase<S> implements StubBasedPsiElement<S>
{
	public CSharpStubElementImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T getStubOrPsiChildByIndex(@NotNull TokenSet set, int index)
	{
		PsiElement[] children = getStubOrPsiChildren(set, PsiElement.ARRAY_FACTORY);
		return (T) ArrayUtil2.safeGet(children, index);
	}

	@NotNull
	@SuppressWarnings("unchecked")
	public <T> T getRequiredStubOrPsiChildByIndex(@NotNull TokenSet set, int index)
	{
		T element = getStubOrPsiChildByIndex(set, index);
		if(element == null)
		{
			throw new IllegalArgumentException("Element by set: " + set + " and index:" + index + " is not found. Tree:\n " + new
					PsiTreeDebugBuilder().psiToString(this, false, false));
		}
		return element;
	}

	@Override
	public void deleteChildInternal(@NotNull ASTNode child)
	{
		PsiFile containingFile = getContainingFile();
		DotNetNamedElement singleElement = CSharpPsiUtilImpl.findSingleElement((CSharpFileImpl) containingFile);
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
		return ItemPresentationProviders.getItemPresentation(this);
	}

	@Override
	public PsiElement getParent()
	{
		return getParentByStub();
	}

	public CSharpStubElementImpl(@NotNull S stub, @NotNull IStubElementType<? extends S, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@NotNull PsiElementVisitor visitor)
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

	public abstract void accept(@NotNull CSharpElementVisitor visitor);
}
