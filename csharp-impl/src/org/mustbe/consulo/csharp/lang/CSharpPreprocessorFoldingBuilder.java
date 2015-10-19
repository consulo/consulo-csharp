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

import java.util.LinkedList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroRecursiveElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPreprocessorCloseTagImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPreprocessorOpenTagImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPreprocessorRegionBlockImpl;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.templateLanguages.OuterLanguageElement;

/**
 * @author VISTALL
 * @since 30.11.13.
 */
public class CSharpPreprocessorFoldingBuilder implements FoldingBuilder
{
	@NotNull
	@Override
	public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode astNode, @NotNull Document document)
	{
		final List<FoldingDescriptor> foldingList = new LinkedList<FoldingDescriptor>();

		PsiElement psi = astNode.getPsi();

		assert psi != null;

		psi.accept(new CSharpMacroRecursiveElementVisitor()
		{
			@Override
			@RequiredReadAction
			public void visitPreprocessorRegionBlock(CSharpPreprocessorRegionBlockImpl block)
			{
				super.visitPreprocessorRegionBlock(block);

				CSharpPreprocessorOpenTagImpl startElement = block.getOpenDirective();
				CSharpPreprocessorCloseTagImpl stopElement = block.getCloseDirective();
				if(startElement == null || stopElement == null)
				{
					return;
				}

				PsiElement keywordElement = startElement.getKeywordElement();
				if(keywordElement == null || keywordElement.getNode().getElementType() != CSharpMacroTokens.REGION_KEYWORD)
				{
					return;
				}

				PsiElement startElementKeywordElement = startElement.getSharpElement();
				int startOffset = startElementKeywordElement.getTextOffset();
				int endOffset = stopElement.getTextOffset() + stopElement.getTextLength();
				if((endOffset - startOffset) > 0)
				{
					// we need remove new lines from folding
					String text = stopElement.getText();
					String anotherString = StringUtil.trimEnd(text, '\n');
					endOffset -= text.length() - anotherString.length();

					foldingList.add(new FoldingDescriptor(block, new TextRange(startOffset, endOffset)));
				}
			}
		});
		return foldingList.toArray(new FoldingDescriptor[foldingList.size()]);
	}

	@Nullable
	@Override
	@RequiredReadAction
	public String getPlaceholderText(@NotNull ASTNode astNode)
	{
		PsiElement psi = astNode.getPsi();


		if(psi instanceof CSharpPreprocessorRegionBlockImpl)
		{
			CSharpPreprocessorOpenTagImpl startElement = ((CSharpPreprocessorRegionBlockImpl) psi).getOpenDirective();

			if(startElement != null)
			{
				return getTextWithoutOuterElements(startElement);
			}
			return "##";
		}
		return null;
	}

	@RequiredReadAction
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
