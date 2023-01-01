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

package consulo.csharp.impl.ide.highlight;

import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.Editor;
import consulo.csharp.lang.impl.ide.codeInspection.unusedUsing.BaseUnusedUsingVisitor;
import consulo.csharp.lang.psi.CSharpUsingListChild;
import consulo.document.util.TextRange;
import consulo.language.editor.highlight.usage.HighlightUsagesHandlerBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiRecursiveElementVisitor;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 06.03.2016
 */
public class CSharpUsingHighlightUsagesHandler extends HighlightUsagesHandlerBase<PsiElement>
{
	public static class OurVisitor extends BaseUnusedUsingVisitor
	{
		private List<PsiElement> myElements = new LinkedList<>();
		private final CSharpUsingListChild myListChild;

		public OurVisitor(CSharpUsingListChild listChild)
		{
			myListChild = listChild;
		}

		@Nonnull
		@Override
		protected Collection<? extends CSharpUsingListChild> getStatements()
		{
			return Collections.singletonList(myListChild);
		}

		@Override
		protected void putElement(CSharpUsingListChild child, PsiElement targetElement)
		{
			if(child.isEquivalentTo(myListChild))
			{
				myElements.add(targetElement);
			}
		}

		public List<PsiElement> getElements()
		{
			return myElements;
		}
	}

	private CSharpUsingListChild myListChild;

	public CSharpUsingHighlightUsagesHandler(Editor editor, PsiFile file, CSharpUsingListChild listChild)
	{
		super(editor, file);
		myListChild = listChild;
	}

	@RequiredReadAction
	@Override
	public List<PsiElement> getTargets()
	{
		final OurVisitor visitor = new OurVisitor(myListChild);
		myFile.accept(new PsiRecursiveElementVisitor()
		{
			@Override
			public void visitElement(PsiElement element)
			{
				element.accept(visitor);
				super.visitElement(element);
			}
		});
		return visitor.getElements();
	}

	@Override
	protected void selectTargets(List<PsiElement> targets, Consumer<List<PsiElement>> selectionConsumer)
	{
		selectionConsumer.accept(targets);
	}

	@Override
	@RequiredReadAction
	public void computeUsages(List<PsiElement> targets)
	{
		for(PsiElement target : targets)
		{
			myWriteUsages.add(target.getTextRange());
		}

		// we need ignored inner elements before using keyword(for example region directive)
		int textOffset = myListChild.getUsingKeywordElement().getTextOffset();
		myReadUsages.add(new TextRange(textOffset, myListChild.getTextRange().getEndOffset()));
	}
}
