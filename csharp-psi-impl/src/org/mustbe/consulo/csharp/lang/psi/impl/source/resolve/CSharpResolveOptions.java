/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpQualifiedNonReference;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;

/**
 * @author VISTALL
 * @since 06.01.15
 */
public class CSharpResolveOptions
{
	@NotNull
	public static CSharpResolveOptions build()
	{
		return new CSharpResolveOptions();
	}

	@NotNull
	private CSharpReferenceExpression.ResolveToKind myKind;
	@Nullable
	private CSharpResolveSelector mySelector;
	@NotNull
	private PsiElement myElement;
	@Nullable
	private CSharpCallArgumentListOwner myCallArgumentListOwner;

	private boolean myCompletion;
	private boolean myResolveFromParent;

	private ResolveResult[] myAdditionalElements = ResolveResult.EMPTY_ARRAY;

	private CSharpResolveOptions()
	{
	}

	public CSharpResolveOptions(@NotNull CSharpReferenceExpression.ResolveToKind kind,
			@Nullable CSharpResolveSelector selector,
			@NotNull PsiElement element,
			@Nullable CSharpCallArgumentListOwner callArgumentListOwner,
			final boolean completion,
			boolean resolveFromParent)
	{

		myKind = kind;
		mySelector = selector;
		myElement = element;
		myCallArgumentListOwner = callArgumentListOwner;
		myCompletion = completion;
		myResolveFromParent = resolveFromParent;
	}

	@NotNull
	public CSharpResolveOptions additionalElements(ResolveResult[] additionalElements)
	{
		myAdditionalElements = additionalElements;
		return this;
	}

	@NotNull
	public CSharpResolveOptions completion()
	{
		myCompletion = true;
		return this;
	}

	@NotNull
	public CSharpResolveOptions element(@NotNull PsiElement element)
	{
		myElement = element;
		return this;
	}

	@NotNull
	public CSharpResolveOptions kind(CSharpReferenceExpression.ResolveToKind kind)
	{
		myKind = kind;
		return this;
	}

	@NotNull
	public CSharpReferenceExpression.ResolveToKind getKind()
	{
		return myKind;
	}

	@Nullable
	public CSharpResolveSelector getSelector()
	{
		return mySelector;
	}

	@NotNull
	public PsiElement getElement()
	{
		return myElement;
	}

	@Nullable
	public PsiElement getQualifier()
	{
		if(myElement instanceof CSharpQualifiedNonReference)
		{
			return ((CSharpQualifiedNonReference) myElement).getQualifier();
		}
		return null;
	}

	@Nullable
	public CSharpCallArgumentListOwner getCallArgumentListOwner()
	{
		return myCallArgumentListOwner;
	}

	@NotNull
	public ResolveResult[] getAdditionalElements()
	{
		return myAdditionalElements;
	}

	public boolean isCompletion()
	{
		return myCompletion;
	}

	public boolean isResolveFromParent()
	{
		return myResolveFromParent;
	}
}
