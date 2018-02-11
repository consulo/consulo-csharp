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

package consulo.csharp.lang.psi.resolve;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class MemberByNameSelector extends UserDataHolderBase implements CSharpNamedResolveSelector
{
	private String myName;

	public MemberByNameSelector(@Nonnull String name)
	{
		myName = name;
	}

	@Nonnull
	public String getName()
	{
		return myName;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<PsiElement> doSelectElement(@Nonnull CSharpResolveContext context, boolean deep)
	{
		return context.findByName(myName, deep, this);
	}

	@Override
	public boolean isNameEqual(@Nonnull String name)
	{
		return Comparing.equal(myName, name);
	}
}
