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

package consulo.csharp.ide.breadcrumbs;

import org.jetbrains.annotations.NotNull;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.csharp.lang.psi.impl.source.CSharpAnonymousMethodExpression;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetNamespaceDeclaration;
import consulo.dotnet.psi.DotNetQualifiedElement;

/**
 * @author VISTALL
 * @since 04-Nov-17
 */
public class CSharpBreadcrumbsProvider implements BreadcrumbsProvider
{
	@NotNull
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}

	@Override
	@RequiredReadAction
	public boolean acceptElement(@NotNull PsiElement psiElement)
	{
		return psiElement instanceof DotNetQualifiedElement && ((DotNetQualifiedElement) psiElement).getName() != null|| psiElement instanceof CSharpAnonymousMethodExpression;
	}

	@NotNull
	@Override
	@RequiredReadAction
	public String getElementInfo(@NotNull PsiElement psiElement)
	{
		if(psiElement instanceof CSharpAnonymousMethodExpression)
		{
			CSharpSimpleParameterInfo[] parameterInfos = ((CSharpAnonymousMethodExpression) psiElement).getParameterInfos();

			StringBuilder builder = new StringBuilder();
			if(parameterInfos.length == 1)
			{
				builder.append(parameterInfos[0].getNotNullName());
			}
			else
			{
				builder.append("(");
				for(int i = 0; i < parameterInfos.length; i++)
				{
					CSharpSimpleParameterInfo parameterInfo = parameterInfos[i];
					if(i != 0)
					{
						builder.append(",");
					}
					builder.append(parameterInfo.getNotNullName());
				}
				builder.append(")");
			}
			builder.append(" => {...}");
			return builder.toString();
		}
		DotNetQualifiedElement qualifiedElement = (DotNetQualifiedElement) psiElement;
		if(qualifiedElement instanceof DotNetNamespaceDeclaration)
		{
			return qualifiedElement.getPresentableQName();
		}

		if(qualifiedElement instanceof DotNetLikeMethodDeclaration)
		{
			return qualifiedElement.getName() + "()";
		}
		return qualifiedElement.getName();
	}
}
