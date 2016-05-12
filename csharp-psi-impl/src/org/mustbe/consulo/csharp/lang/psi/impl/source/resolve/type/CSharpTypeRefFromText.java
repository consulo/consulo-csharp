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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.fragment.CSharpFragmentFactory;
import org.mustbe.consulo.csharp.lang.psi.impl.fragment.CSharpFragmentFileImpl;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpTypeRefFromText extends DotNetTypeRefWithCachedResult
{
	private final String myText;
	private final PsiElement myOwner;

	public CSharpTypeRefFromText(final String text, final PsiElement owner)
	{
		myText = text;
		myOwner = owner;
	}

	@NotNull
	//@LazyInstance
	private DotNetType getType()
	{
		CSharpFragmentFileImpl typeFragment = CSharpFragmentFactory.createTypeFragment(myOwner.getProject(), myText, myOwner);
		typeFragment.forceResolveScope(myOwner.getResolveScope());

		DotNetType dotNetType = PsiTreeUtil.getChildOfType(typeFragment, DotNetType.class);
		assert dotNetType != null : myText;
		return dotNetType;
	}

	@RequiredReadAction
	@NotNull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		return getType().toTypeRef().resolve();
	}

	@RequiredReadAction
	@NotNull
	@Override
	public String toString()
	{
		return getType().toTypeRef().toString();
	}
}
