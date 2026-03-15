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

package consulo.csharp.lang.impl.psi.source.resolve;

import consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import consulo.csharp.lang.impl.psi.CSharpContextUtil;
import consulo.csharp.lang.psi.CSharpQualifiedNonReference;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import consulo.language.psi.PsiElement;

import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 06.01.15
 */
public class CSharpResolveOptions
{
	public static CSharpResolveOptions build()
	{
		return new CSharpResolveOptions();
	}

	private CSharpReferenceExpression.ResolveToKind myKind;
	@Nullable
	private CSharpResolveSelector mySelector;
	private PsiElement myElement;
	@Nullable
	private CSharpCallArgumentListOwner myCallArgumentListOwner;

	private boolean myCompletion;

	private CSharpContextUtil.ContextType myCompletionContextType;

	private boolean myResolveFromParent;

	private CSharpResolveOptions()
	{
	}

	public CSharpResolveOptions(CSharpReferenceExpression.ResolveToKind kind,
			@Nullable CSharpResolveSelector selector,
			PsiElement element,
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

	public CSharpResolveOptions completion()
	{
		myCompletion = true;
		return this;
	}

	public CSharpResolveOptions completion(CSharpContextUtil.ContextType contextType)
	{
		myCompletion = true;
		myCompletionContextType = contextType;
		return this;
	}

	public CSharpResolveOptions resolveFromParent()
	{
		myResolveFromParent = true;
		return this;
	}

	public CSharpResolveOptions element(PsiElement element)
	{
		myElement = element;
		return this;
	}

	public CSharpResolveOptions kind(CSharpReferenceExpression.ResolveToKind kind)
	{
		myKind = kind;
		return this;
	}

	public CSharpReferenceExpression.ResolveToKind getKind()
	{
		return myKind;
	}

	@Nullable
	public CSharpResolveSelector getSelector()
	{
		return mySelector;
	}

	public PsiElement getElement()
	{
		return myElement;
	}

	public CSharpContextUtil.@Nullable ContextType getCompletionContextType()
	{
		return myCompletionContextType;
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

	public boolean isCompletion()
	{
		return myCompletion;
	}

	public boolean isResolveFromParent()
	{
		return myResolveFromParent;
	}
}
