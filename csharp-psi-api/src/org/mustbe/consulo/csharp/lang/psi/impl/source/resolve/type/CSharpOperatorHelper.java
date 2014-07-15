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

import java.util.List;

import org.consulo.lombok.annotations.ProjectService;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 15.03.14
 */
@ProjectService
public abstract class CSharpOperatorHelper
{
	@NotNull
	public abstract List<DotNetNamedElement> getStubMembers();

	@NotNull
	public static IElementType mergeTwiceOperatorIfNeed(PsiElement element)
	{
		IElementType elementType = element.getNode().getElementType();
		if(elementType == CSharpTokens.LT || elementType == CSharpTokens.GT)
		{
			PsiElement nextSibling = element.getNextSibling();
			if(nextSibling != null)
			{
				IElementType elementType1 = nextSibling.getNode().getElementType();
				if(elementType == CSharpTokens.LT && elementType1 == CSharpTokens.LT)
				{
					return CSharpTokens.LTLT;
				}
				else if(elementType == CSharpTokens.GT && elementType1 == CSharpTokens.GT)
				{
					return CSharpTokens.GTGT;
				}
			}
		}

		return elementType;
	}
}
