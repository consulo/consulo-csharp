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

package org.mustbe.consulo.csharp.ide.highlight;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroDefine;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMacroBlockImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMacroBlockStartImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMacroBlockStopImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMacroReferenceExpressionImpl;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 * @author VISTALL
 * @since 06.02.14
 */
public class CSharpPreprocessorHighlightVisitor extends CSharpMacroElementVisitor implements HighlightVisitor
{
	private HighlightInfoHolder myHighlightInfoHolder;

	@Override
	public boolean suitableForFile(@NotNull PsiFile psiFile)
	{
		return psiFile instanceof CSharpFileImpl;
	}

	@Override
	public void visit(@NotNull PsiElement element)
	{
		element.accept(this);
	}

	@Override
	public boolean analyze(@NotNull PsiFile psiFile, boolean b, @NotNull HighlightInfoHolder highlightInfoHolder, @NotNull Runnable runnable)
	{
		myHighlightInfoHolder = highlightInfoHolder;
		runnable.run();
		return true;
	}

	@Override
	@RequiredReadAction
	public void visitMacroBlock(CSharpMacroBlockImpl block)
	{
		CSharpMacroBlockStartImpl startElement = block.getStartElement();
		CSharpMacroBlockStopImpl stopElement = block.getStopElement();

		if(startElement == null && stopElement != null)
		{
			PsiElement keywordElement = stopElement.getKeywordElement();
			if(keywordElement.getNode().getElementType() == CSharpMacroTokens.ENDREGION_KEYWORD)
			{
				myHighlightInfoHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.ERROR).range(keywordElement).descriptionAndTooltip("Required region start").create());
			}
		}
		else if(startElement != null && stopElement == null)
		{
			PsiElement keywordElement = startElement.getKeywordElement();
			if(keywordElement != null && keywordElement.getNode().getElementType() == CSharpMacroTokens.REGION_KEYWORD)
			{
				myHighlightInfoHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.ERROR).range(keywordElement).descriptionAndTooltip("Required region end").create());
			}
			else if(keywordElement == null)
			{
				myHighlightInfoHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.ERROR).range(startElement.getSharpElement()).descriptionAndTooltip("Expected directive name").create());
			}
		}
	}

	@Override
	public void visitReferenceExpression(CSharpMacroReferenceExpressionImpl expression)
	{
		PsiElement resolve = expression.resolve();
		if(resolve != null)
		{
			highlightNamed(resolve, expression.getElement());
		}
	}

	@Override
	public void visitMacroDefine(CSharpMacroDefine cSharpMacroDefine)
	{
		if(cSharpMacroDefine.isUnDef())
		{
			return;
		}
		highlightNamed(cSharpMacroDefine, cSharpMacroDefine.getNameIdentifier());
	}

	public void highlightNamed(@Nullable PsiElement element, @Nullable PsiElement target)
	{
		CSharpHighlightUtil.highlightNamed(myHighlightInfoHolder, element, target, null);
	}

	@NotNull
	@Override
	public HighlightVisitor clone()
	{
		return new CSharpPreprocessorHighlightVisitor();
	}

	@Override
	public int order()
	{
		return 0;
	}
}
