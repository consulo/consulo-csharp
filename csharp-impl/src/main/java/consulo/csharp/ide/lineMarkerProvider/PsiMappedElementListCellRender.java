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

package consulo.csharp.ide.lineMarkerProvider;

import consulo.language.editor.ui.PsiElementListCellRenderer;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.SymbolPresentationUtil;
import consulo.ui.image.Image;

import java.util.function.Function;

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
		return 0;
	}

	@Override
	protected Image getIcon(PsiElement element)
	{
		PsiElement map = myMap.apply(element);
		if(map != null)
		{
			return super.getIcon(map);
		}
		return super.getIcon(element);
	}

	@Override
	public String getElementText(PsiElement element)
	{
		PsiElement map = myMap.apply(element);
		if(map != null)
		{
			return SymbolPresentationUtil.getSymbolPresentableText(map);
		}
		return SymbolPresentationUtil.getSymbolPresentableText(element);
	}

	@Override
	public String getContainerText(PsiElement element, final String name)
	{
		PsiElement map = myMap.apply(element);
		if(map != null)
		{
			return SymbolPresentationUtil.getSymbolContainerText(map);
		}
		return SymbolPresentationUtil.getSymbolContainerText(element);
	}
}
