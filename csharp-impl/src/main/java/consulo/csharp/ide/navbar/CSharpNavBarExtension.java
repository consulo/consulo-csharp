/*
 * Copyright 2013-2020 consulo.io
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

package consulo.csharp.ide.navbar;

import com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension;
import com.intellij.ide.ui.UISettings;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.impl.source.CSharpPsiUtilImpl;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetNamespaceDeclaration;
import consulo.dotnet.psi.DotNetQualifiedElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-06-21
 */
public class CSharpNavBarExtension extends StructureAwareNavBarModelExtension
{
	@Nonnull
	@Override
	protected Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}

	@Nullable
	@Override
	@RequiredReadAction
	public String getPresentableText(Object o)
	{
		return getPresentableText(o, false);
	}

	@Nullable
	@Override
	@RequiredReadAction
	public String getPresentableText(Object o, boolean forPopup)
	{
		if(o instanceof DotNetQualifiedElement)
		{
			DotNetQualifiedElement qualifiedElement = (DotNetQualifiedElement) o;
			if(qualifiedElement instanceof DotNetNamespaceDeclaration)
			{
				return qualifiedElement.getPresentableQName();
			}

			if(qualifiedElement instanceof DotNetLikeMethodDeclaration)
			{
				return qualifiedElement.getName();
			}
			return qualifiedElement.getName();
		}

		return null;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getParent(@Nonnull PsiElement psiElement)
	{
		if(psiElement instanceof DotNetNamedElement)
		{
			PsiFile containingFile = psiElement.getContainingFile();
			if(containingFile instanceof CSharpFile)
			{
				DotNetNamedElement element = CSharpPsiUtilImpl.findSingleElement((CSharpFile) containingFile);
				if(psiElement.isEquivalentTo(element))
				{
					return containingFile.getParent();
				}
			}
		}
		return super.getParent(psiElement);
	}

	@Nullable
	@Override
	@RequiredReadAction
	public PsiElement adjustElement(PsiElement psiElement)
	{
		if(psiElement instanceof CSharpFile)
		{
			if(!UISettings.getInstance().getShowMembersInNavigationBar())
			{
				return psiElement;
			}

			DotNetNamedElement element = CSharpPsiUtilImpl.findSingleElement((CSharpFile) psiElement);
			if(element != null)
			{
				return element;
			}
		}

		if(!UISettings.getInstance().getShowMembersInNavigationBar())
		{
			return psiElement.getContainingFile();
		}

		return super.adjustElement(psiElement);
	}
}
