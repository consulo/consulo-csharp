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

package consulo.csharp.ide.highlight.check.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpUserType;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.language.psi.PsiElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 19.01.2016
 */
public class CS0673 extends CompilerCheck<CSharpUserType>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpUserType element)
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
			if(DotNetTypeRefUtil.isVmQNameEqual(element.toTypeRef(), DotNetTypes.System.Void))
			{
				return newBuilder(element);
			}
		}
		return null;
	}
}
