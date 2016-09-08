/*
 * Copyright 2013-2015 must-be.org
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

package consulo.csharp.lang.doc.ide.highlight;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocTag;
import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocTokenType;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import consulo.annotations.RequiredReadAction;
import consulo.codeInsight.TargetElementUtil;

/**
 * @author VISTALL
 * @since 03.03.2015
 */
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
			CSharpDocTag docTag = PsiTreeUtil.getParentOfType(target, CSharpDocTag.class);
			if(docTag == null)
			{
				return null;
			}
			return new CSharpDocTagHighlightUsagesHandler(editor, file, docTag);
		}
		return null;
	}
}
