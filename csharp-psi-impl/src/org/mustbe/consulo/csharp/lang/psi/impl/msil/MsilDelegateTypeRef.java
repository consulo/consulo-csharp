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

package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.msil.MsilHelper;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilDelegateTypeRef extends DotNetTypeRef.Delegate
{
	public MsilDelegateTypeRef(DotNetTypeRef typeRef)
	{
		super(typeRef);
	}

	@NotNull
	@Override
	public String getPresentableText()
	{
		return MsilHelper.cutGenericMarker(super.getPresentableText());
	}

	@NotNull
	@Override
	public String getQualifiedText()
	{
		return MsilHelper.cutGenericMarker(super.getQualifiedText());
	}

	@Nullable
	@Override
	public PsiElement resolve(@NotNull PsiElement scope)
	{
		PsiElement resolve = super.resolve(scope);
		if(resolve == null)
		{
			return null;
		}
		return MsilToCSharpUtil.wrap(resolve);
	}
}
