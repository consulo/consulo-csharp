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

package org.mustbe.consulo.csharp.ide.presentation;

import javax.swing.Icon;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.ide.DotNetElementPresentationUtil;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.ide.IconDescriptorUpdaters;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProvider;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.util.Iconable;

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
		public String getPresentableText()
		{
			if(myDeclaration instanceof DotNetTypeDeclaration)
			{
				return DotNetElementPresentationUtil.formatTypeWithGenericParameters((DotNetTypeDeclaration) myDeclaration);
			}
			//FIXME [VISTALL] use org.mustbe.consulo.csharp.ide.projectView.CSharpQElementTreeNode.getPresentableText() ?
			return myDeclaration.getName();
		}

		@Nullable
		@Override
		public String getLocationString()
		{
			String presentableParentQName = myDeclaration.getPresentableParentQName();
			if(StringUtils.isEmpty(presentableParentQName))
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
