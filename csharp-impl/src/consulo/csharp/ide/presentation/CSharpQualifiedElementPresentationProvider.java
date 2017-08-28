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

package consulo.csharp.ide.presentation;

import javax.swing.Icon;

import org.jetbrains.annotations.Nullable;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProvider;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import consulo.annotations.RequiredDispatchThread;
import consulo.csharp.ide.CSharpElementPresentationUtil;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.dotnet.ide.DotNetElementPresentationUtil;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetMethodDeclaration;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.ide.IconDescriptorUpdaters;

/**
 * @author VISTALL
 * @since 21.12.13.
 */
public class CSharpQualifiedElementPresentationProvider implements ItemPresentationProvider<NavigationItem>
{
	public static class It implements ItemPresentation
	{
		private final DotNetQualifiedElement myDeclaration;

		public It(DotNetQualifiedElement declaration)
		{
			myDeclaration = declaration;
		}

		@Nullable
		@Override
		@RequiredDispatchThread
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
		@RequiredDispatchThread
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
		public Icon getIcon(boolean b)
		{
			return IconDescriptorUpdaters.getIcon(myDeclaration, Iconable.ICON_FLAG_VISIBILITY);
		}
	}

	@Override
	public ItemPresentation getPresentation(NavigationItem qualifiedElement)
	{
		return new It((DotNetQualifiedElement)qualifiedElement);
	}
}
