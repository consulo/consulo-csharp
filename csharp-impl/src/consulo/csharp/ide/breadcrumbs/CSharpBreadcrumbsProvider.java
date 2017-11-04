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
import consulo.csharp.lang.CSharpLanguage;
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
	public boolean acceptElement(@NotNull PsiElement psiElement)
	{
		return psiElement instanceof DotNetQualifiedElement;
	}

	@NotNull
	@Override
	public String getElementInfo(@NotNull PsiElement psiElement)
	{
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
