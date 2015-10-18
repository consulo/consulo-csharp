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

package org.mustbe.consulo.csharp.lang;

import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroRecursiveElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMacroBlockImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMacroBlockStartImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMacroBlockStopImpl;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.templateLanguages.OuterLanguageElement;
import lombok.val;

/**
 * @author VISTALL
 * @since 30.11.13.
 */
public class CSharpMacroFoldingBuilder implements FoldingBuilder
{
	@NotNull
	@Override
	public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode astNode, @NotNull Document document)
	{
		val foldingList = new ArrayList<FoldingDescriptor>();

		PsiElement psi = astNode.getPsi();

		psi.accept(new CSharpMacroRecursiveElementVisitor()
		{
			@Override
			public void visitMacroBlock(CSharpMacroBlockImpl block)
			{
				super.visitMacroBlock(block);

				CSharpMacroBlockStartImpl startElement = block.getStartElement();
				CSharpMacroBlockStopImpl stopElement = block.getStopElement();
				if(startElement == null || stopElement == null)
				{
					return;
				}

				if(startElement.getKeywordElement().getNode().getElementType() != CSharpMacroTokens.REGION_KEYWORD)
				{
					return;
				}

				PsiElement stopElementStopElement = stopElement.getStopElement();
				if(stopElementStopElement == null)
				{
					return;
				}

				PsiElement startElementKeywordElement = startElement.getKeywordElement();
				int textOffset = startElementKeywordElement.getTextOffset();
				int textOffset1 = stopElementStopElement.getTextOffset();
				if((textOffset1 - textOffset) > 0)
				{
					foldingList.add(new FoldingDescriptor(block, new TextRange(textOffset, textOffset1)));
				}
			}
		});
		return foldingList.toArray(new FoldingDescriptor[foldingList.size()]);
	}

	@Nullable
	@Override
	public String getPlaceholderText(@NotNull ASTNode astNode)
	{
		PsiElement psi = astNode.getPsi();


		if(psi instanceof CSharpMacroBlockImpl)
		{
			CSharpMacroBlockStartImpl startElement = ((CSharpMacroBlockImpl) psi).getStartElement();

			if(startElement != null)
			{
				return getTextWithoutOuterElements(startElement);
			}
			return "##";
		}
		return null;
	}

	private String getTextWithoutOuterElements(PsiElement element)
	{
		StringBuilder builder = new StringBuilder();
		PsiElement it = element.getFirstChild();
		while(it != null)
		{
			if(!(it instanceof OuterLanguageElement))
			{
				builder.append(it.getText());
			}
			it = it.getNextSibling();
		}
		return builder.toString().trim();
	}

	@Override
	public boolean isCollapsedByDefault(@NotNull ASTNode astNode)
	{
		PsiElement psi = astNode.getPsi();


		return false;
	}
}
