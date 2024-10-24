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

package consulo.csharp.impl.ide.navbar;

import consulo.annotation.component.ExtensionImpl;
import consulo.fileEditor.structureView.tree.NodeProvider;
import consulo.ide.navigationToolbar.StructureAwareNavBarModelExtension;
import consulo.application.ui.UISettings;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.structureView.CSharpLambdaNodeProvider;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.impl.psi.source.CSharpDelegateExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpPsiUtilImpl;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetNamespaceDeclaration;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.language.Language;
import jakarta.annotation.Nonnull;

import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-06-21
 */
@ExtensionImpl
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

	@Nonnull
	@Override
	protected List<NodeProvider<?>> getApplicableNodeProviders()
	{
		return Collections.singletonList(CSharpLambdaNodeProvider.INSTANCE);
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
		else if(o instanceof CSharpDelegateExpressionImpl)
		{
			return "Delegate";
		}
		else if(o instanceof CSharpLambdaExpressionImpl)
		{
			return "Lambda";
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
