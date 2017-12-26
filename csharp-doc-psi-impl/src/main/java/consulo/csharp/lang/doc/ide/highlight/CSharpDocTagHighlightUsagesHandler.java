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

package consulo.csharp.lang.doc.ide.highlight;

import java.util.List;

import consulo.csharp.lang.doc.psi.CSharpDocTagImpl;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.Consumer;

/**
 * @author VISTALL
 * @since 03.03.2015
 */
public class CSharpDocTagHighlightUsagesHandler extends HighlightUsagesHandlerBase<PsiElement>
{
	private final CSharpDocTagImpl myDocTag;

	public CSharpDocTagHighlightUsagesHandler(Editor editor, PsiFile file, CSharpDocTagImpl docTag)
	{
		super(editor, file);
		myDocTag = docTag;
	}

	@Override
	public List<PsiElement> getTargets()
	{
		return myDocTag.getNameElements();
	}

	@Override
	protected void selectTargets(List<PsiElement> targets, Consumer<List<PsiElement>> selectionConsumer)
	{
		selectionConsumer.consume(targets);
	}

	@Override
	public void computeUsages(List<PsiElement> targets)
	{
		for(PsiElement target : targets)
		{
			addOccurrence(target);
		}
	}
}
