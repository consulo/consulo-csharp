/*
 * Copyright 2013-2016 must-be.org
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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpUserType;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefUtil;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 19.01.2016
 */
public class CS0673 extends CompilerCheck<CSharpUserType>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpUserType element)
	{
		PsiElement parent = element;

		while(true)
		{
			if(parent instanceof DotNetType || parent instanceof DotNetTypeList)
			{
				boolean typeList = parent instanceof DotNetTypeList;
				parent = parent.getParent();
				if(typeList && parent instanceof CSharpReferenceExpression)
				{
					parent = parent.getParent();
				}
			}
			else
			{
				break;
			}
		}

		if(parent instanceof DotNetLikeMethodDeclaration || parent instanceof DotNetVariable)
		{
			if(DotNetTypeRefUtil.isVmQNameEqual(element.toTypeRef(), element, DotNetTypes.System.Void))
			{
				return newBuilder(element);
			}
		}
		return null;
	}
}
