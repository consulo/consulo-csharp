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
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetAttributeUtil;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementWeigher;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class CSharpObsoleteWeigher extends LookupElementWeigher
{
	public enum Access
	{
		NORMAL,
		OBSOLETE
	}

	public CSharpObsoleteWeigher()
	{
		super("CSharpObsoleteWeigher");
	}

	@Override
	@RequiredReadAction
	public Comparable weigh(@NotNull LookupElement lookupElement)
	{
		PsiElement psiElement = lookupElement.getPsiElement();
		if(psiElement == null)
		{
			return null;
		}

		if(DotNetAttributeUtil.hasAttribute(psiElement, DotNetTypes.System.ObsoleteAttribute))
		{
			return Access.OBSOLETE;
		}
		return Access.NORMAL;
	}
}
