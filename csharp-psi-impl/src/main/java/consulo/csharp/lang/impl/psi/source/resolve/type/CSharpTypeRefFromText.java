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

package consulo.csharp.lang.impl.psi.source.resolve.type;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.fragment.CSharpFragmentFactory;
import consulo.csharp.lang.impl.psi.fragment.CSharpFragmentFileImpl;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.lazy.LazyValue;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpTypeRefFromText extends DotNetTypeRefWithCachedResult
{
	private final Supplier<DotNetType> myDelegate;

	public CSharpTypeRefFromText(final String text, final PsiElement owner)
	{
		super(owner.getProject(), owner.getResolveScope());
		myDelegate = LazyValue.notNull(() -> getType(text, owner));
	}

	@Nonnull
	private static DotNetType getType(String text, PsiElement owner)
	{
		CSharpFragmentFileImpl typeFragment = CSharpFragmentFactory.createTypeFragment(owner.getProject(), text, owner);
		typeFragment.forceResolveScope(owner.getResolveScope());

		DotNetType dotNetType = PsiTreeUtil.getChildOfType(typeFragment, DotNetType.class);
		assert dotNetType != null : text;
		return dotNetType;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		return myDelegate.get().toTypeRef().resolve();
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String getVmQName()
	{
		return myDelegate.get().toTypeRef().getVmQName();
	}
}
