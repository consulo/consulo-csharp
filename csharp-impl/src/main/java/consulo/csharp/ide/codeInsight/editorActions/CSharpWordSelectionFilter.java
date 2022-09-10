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

package consulo.csharp.ide.codeInsight.editorActions;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.language.editor.action.WordSelectionerFilter;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;

/**
 * @author VISTALL
 * @since 14-Nov-17
 */
@ExtensionImpl
public class CSharpWordSelectionFilter implements WordSelectionerFilter
{
	@Override
	public boolean canSelect(PsiElement element)
	{
		if(PsiUtilCore.getElementType(element) == CSharpTokens.IDENTIFIER)
		{
			return false;
		}
		return true;
	}
}
