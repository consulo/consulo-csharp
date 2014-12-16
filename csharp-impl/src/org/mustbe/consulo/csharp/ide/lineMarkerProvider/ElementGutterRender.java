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

package org.mustbe.consulo.csharp.ide.lineMarkerProvider;

import javax.swing.Icon;

import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.presentation.java.SymbolPresentationUtil;

/**
 * @author VISTALL
 * @since 16.12.14
 */
public class ElementGutterRender extends PsiElementListCellRenderer<PsiElement>
{
	@Override
	protected int getIconFlags()
	{
		return Iconable.ICON_FLAG_VISIBILITY;
	}

	@Override
	protected Icon getIcon(PsiElement element)
	{
		PsiElement parent = element.getParent();
		if(parent instanceof CSharpTypeDeclaration)
		{
			return super.getIcon(parent);
		}
		return super.getIcon(element);
	}

	@Override
	public String getElementText(PsiElement element)
	{
		PsiElement parent = element.getParent();
		if(parent instanceof CSharpTypeDeclaration)
		{
			return SymbolPresentationUtil.getSymbolPresentableText(parent);
		}
		return SymbolPresentationUtil.getSymbolPresentableText(element);
	}

	@Override
	public String getContainerText(PsiElement element, final String name)
	{
		PsiElement parent = element.getParent();
		if(parent instanceof CSharpTypeDeclaration)
		{
			return SymbolPresentationUtil.getSymbolContainerText(parent);
		}
		return SymbolPresentationUtil.getSymbolContainerText(element);
	}
}
