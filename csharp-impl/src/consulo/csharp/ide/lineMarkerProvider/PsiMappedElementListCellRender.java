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

package consulo.csharp.ide.lineMarkerProvider;

import javax.swing.Icon;

import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.presentation.java.SymbolPresentationUtil;
import com.intellij.util.Function;

/**
 * @author VISTALL
 * @since 16.12.14
 */
public class PsiMappedElementListCellRender extends PsiElementListCellRenderer<PsiElement>
{
	private Function<PsiElement, PsiElement> myMap;

	public PsiMappedElementListCellRender(Function<PsiElement, PsiElement> map)
	{
		myMap = map;
	}

	@Override
	protected int getIconFlags()
	{
		return Iconable.ICON_FLAG_VISIBILITY;
	}

	@Override
	protected Icon getIcon(PsiElement element)
	{
		PsiElement map = myMap.fun(element);
		if(map != null)
		{
			return super.getIcon(map);
		}
		return super.getIcon(element);
	}

	@Override
	public String getElementText(PsiElement element)
	{
		PsiElement map = myMap.fun(element);
		if(map != null)
		{
			return SymbolPresentationUtil.getSymbolPresentableText(map);
		}
		return SymbolPresentationUtil.getSymbolPresentableText(element);
	}

	@Override
	public String getContainerText(PsiElement element, final String name)
	{
		PsiElement map = myMap.fun(element);
		if(map != null)
		{
			return SymbolPresentationUtil.getSymbolContainerText(map);
		}
		return SymbolPresentationUtil.getSymbolContainerText(element);
	}
}
