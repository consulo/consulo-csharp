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

package consulo.csharp.impl.ide.presentation;

import consulo.component.util.Iconable;
import consulo.csharp.impl.ide.CSharpElementPresentationUtil;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.dotnet.psi.*;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.navigation.ItemPresentation;
import consulo.navigation.ItemPresentationProvider;
import consulo.navigation.NavigationItem;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.image.Image;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 21.12.13.
 */
public abstract class CSharpElementPresentationProviderBase<T extends DotNetQualifiedElement & NavigationItem> implements ItemPresentationProvider<T>
{
	public static class ItemPresentationImpl implements ItemPresentation
	{
		private final DotNetQualifiedElement myDeclaration;

		public ItemPresentationImpl(DotNetQualifiedElement declaration)
		{
			myDeclaration = declaration;
		}

		@Nullable
		@Override
		@RequiredUIAccess
		public String getPresentableText()
		{
			if(myDeclaration instanceof DotNetTypeDeclaration)
			{
				return DotNetElementPresentationUtil.formatTypeWithGenericParameters((DotNetTypeDeclaration) myDeclaration);
			}
			else if(myDeclaration instanceof DotNetMethodDeclaration)
			{
				if(myDeclaration instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) myDeclaration).isDelegate())
				{
					return DotNetElementPresentationUtil.formatTypeWithGenericParameters((CSharpMethodDeclaration) myDeclaration);
				}
				return CSharpElementPresentationUtil.formatMethod((DotNetLikeMethodDeclaration) myDeclaration, 0);
			}
			else if(myDeclaration instanceof CSharpConstructorDeclaration)
			{
				return CSharpElementPresentationUtil.formatMethod((CSharpConstructorDeclaration) myDeclaration, 0);
			}
			else if(myDeclaration instanceof CSharpIndexMethodDeclaration)
			{
				return CSharpElementPresentationUtil.formatMethod((DotNetLikeMethodDeclaration) myDeclaration, 0);
			}
			return myDeclaration.getName();
		}

		@Nullable
		@Override
		@RequiredUIAccess
		public String getLocationString()
		{
			String presentableParentQName = myDeclaration.getPresentableParentQName();
			if(StringUtil.isEmpty(presentableParentQName))
			{
				return null;
			}
			return "(" + presentableParentQName + ")";
		}

		@Nullable
		@Override
		public Image getIcon()
		{
			return IconDescriptorUpdaters.getIcon(myDeclaration, Iconable.ICON_FLAG_VISIBILITY);
		}
	}

	@Nonnull
	@Override
	public ItemPresentation getPresentation(T qualifiedElement)
	{
		return new ItemPresentationImpl(qualifiedElement);
	}
}
