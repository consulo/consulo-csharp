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

package consulo.csharp.lang.doc.impl.ide.highlight;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.csharp.lang.doc.impl.psi.CSharpDocTagImpl;
import consulo.csharp.lang.doc.impl.psi.CSharpDocTokenType;
import consulo.language.editor.TargetElementUtil;
import consulo.language.editor.highlight.usage.HighlightUsagesHandlerBase;
import consulo.language.editor.highlight.usage.HighlightUsagesHandlerFactory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 03.03.2015
 */
@ExtensionImpl
public class CSharpDocHighlightUsagesHandlerFactory implements HighlightUsagesHandlerFactory
{
	@Nullable
	@Override
	@RequiredReadAction
	public HighlightUsagesHandlerBase createHighlightUsagesHandler(Editor editor, PsiFile file)
	{
		int offset = TargetElementUtil.adjustOffset(file, editor.getDocument(), editor.getCaretModel().getOffset());
		PsiElement target = file.findElementAt(offset);
		if(target != null && target.getNode().getElementType() == CSharpDocTokenType.XML_NAME)
		{
			CSharpDocTagImpl docTag = PsiTreeUtil.getParentOfType(target, CSharpDocTagImpl.class);
			if(docTag == null)
			{
				return null;
			}
			return new CSharpDocTagHighlightUsagesHandler(editor, file, docTag);
		}
		return null;
	}
}
