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

package consulo.csharp.lang.psi.impl.source.resolve.type;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.impl.fragment.CSharpFragmentFactory;
import consulo.csharp.lang.psi.impl.fragment.CSharpFragmentFileImpl;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.resolve.DotNetTypeResolveResult;

import javax.annotation.Nonnull;

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
		super(owner.getProject(), owner.getResolveScope());
		myText = text;
		myOwner = owner;
	}

	@Nonnull
	//@Lazy
	private DotNetType getType()
	{
		CSharpFragmentFileImpl typeFragment = CSharpFragmentFactory.createTypeFragment(myOwner.getProject(), myText, myOwner);
		typeFragment.forceResolveScope(myOwner.getResolveScope());

		DotNetType dotNetType = PsiTreeUtil.getChildOfType(typeFragment, DotNetType.class);
		assert dotNetType != null : myText;
		return dotNetType;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		return getType().toTypeRef().resolve();
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String getVmQName()
	{
		return getType().toTypeRef().getVmQName();
	}
}
