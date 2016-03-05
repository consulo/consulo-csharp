/*
 * Copyright 2013-2016 must-be.org
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

package org.mustbe.consulo.csharp.ide.highlight;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.codeInspection.unusedUsing.BaseUnusedUsingVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingListChild;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.util.Consumer;

/**
 * @author VISTALL
 * @since 06.03.2016
 */
public class CSharpUsingHighlightUsagesHandler extends HighlightUsagesHandlerBase<PsiElement>
{
	public static class OurVisitor extends BaseUnusedUsingVisitor
	{
		private List<PsiElement> myElements = new LinkedList<PsiElement>();
		private final CSharpUsingListChild myListChild;

		public OurVisitor(CSharpUsingListChild listChild)
		{
			myElements.add(listChild);
			myListChild = listChild;
		}

		@NotNull
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
		selectionConsumer.consume(targets);
	}

	@Override
	@RequiredReadAction
	public void computeUsages(List<PsiElement> targets)
	{
		for(PsiElement target : targets)
		{
			addOccurrence(target);
		}
	}
}
