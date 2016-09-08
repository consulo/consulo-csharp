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

package org.mustbe.consulo.csharp.ide.completion.weigher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementWeigher;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 17.11.2015
 */
public class CSharpByGenericParameterWeigher extends LookupElementWeigher
{
	public CSharpByGenericParameterWeigher()
	{
		super("CSharpByGenericParameterWeigher");
	}

	@Nullable
	@Override
	@RequiredReadAction
	public Comparable weigh(@NotNull LookupElement element)
	{
		PsiElement psiElement = element.getPsiElement();
		if(psiElement instanceof DotNetGenericParameterListOwner && psiElement.getLanguage() == CSharpLanguage.INSTANCE)
		{
			return ((DotNetGenericParameterListOwner) psiElement).getGenericParametersCount();
		}
		return 0;
	}
}
