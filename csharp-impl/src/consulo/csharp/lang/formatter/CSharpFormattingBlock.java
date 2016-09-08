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

package consulo.csharp.lang.formatter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.codeStyle.CSharpCodeStyleSettings;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.formatter.processors.CSharpIndentProcessor;
import consulo.csharp.lang.formatter.processors.CSharpSpacingProcessor;
import consulo.csharp.lang.formatter.processors.CSharpWrappingProcessor;
import consulo.csharp.lang.psi.CSharpElements;
import consulo.csharp.lang.psi.CSharpTokenSets;
import consulo.csharp.lang.psi.CSharpTokens;
import com.intellij.formatting.ASTBlock;
import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.templateLanguages.BlockWithParent;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public class CSharpFormattingBlock extends AbstractBlock implements CSharpElements, CSharpTokens, CSharpTokenSets, BlockWithParent
{
	private final CSharpWrappingProcessor myWrappingProcessor;
	private final CSharpIndentProcessor myIndentProcessor;
	private final CSharpSpacingProcessor mySpacingProcessor;
	private CodeStyleSettings mySettings;

	private List<ASTNode> myAdditionalNodes = Collections.emptyList();
	private CSharpFormattingBlock myParent;

	public CSharpFormattingBlock(@NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment, @NotNull CodeStyleSettings settings)
	{
		super(node, wrap, alignment);
		mySettings = settings;
		CommonCodeStyleSettings commonSettings = settings.getCommonSettings(CSharpLanguage.INSTANCE);
		CSharpCodeStyleSettings customSettings = settings.getCustomSettings(CSharpCodeStyleSettings.class);

		myWrappingProcessor = new CSharpWrappingProcessor(node, commonSettings, customSettings);
		myIndentProcessor = new CSharpIndentProcessor(this, commonSettings, customSettings);
		mySpacingProcessor = new CSharpSpacingProcessor(this, commonSettings, customSettings);
	}

	@Nullable
	@Override
	public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2)
	{
		return mySpacingProcessor.getSpacing((ASTBlock) child1, (ASTBlock) child2);
	}

	@Override
	public boolean isLeaf()
	{
		return getNode().getFirstChildNode() == null;
	}

	@NotNull
	@Override
	public TextRange getTextRange()
	{
		TextRange textRange = super.getTextRange();
		if(!myAdditionalNodes.isEmpty())
		{
			return new TextRange(textRange.getStartOffset(), ContainerUtil.getLastItem(myAdditionalNodes).getTextRange().getEndOffset());
		}
		return textRange;
	}

	@Override
	protected List<Block> buildChildren()
	{
		if(isLeaf())
		{
			return EMPTY;
		}

		List<Block> children = new ArrayList<Block>(5);
		Deque<ASTNode> nodes = new ArrayDeque<ASTNode>(5);

		for(ASTNode childNode = getNode().getFirstChildNode(); childNode != null; childNode = childNode.getTreeNext())
		{
			if(FormatterUtil.containsWhiteSpacesOnly(childNode))
			{
				continue;
			}

			nodes.add(childNode);
		}
		nodes.addAll(myAdditionalNodes);

		ASTNode next;
		while((next = nodes.poll()) != null)
		{
			final CSharpFormattingBlock childBlock = new CSharpFormattingBlock(next, null, null, mySettings);
			childBlock.setParent(this);

			IElementType elementType = next.getElementType();
			if(elementType == SWITCH_LABEL_STATEMENT)
			{
				ASTNode someNextElement;
				while((someNextElement = nodes.poll()) != null)
				{
					IElementType someNextElementType = someNextElement.getElementType();
					if(someNextElementType == SWITCH_LABEL_STATEMENT || someNextElementType == RBRACE)
					{
						nodes.addFirst(someNextElement);
						break;
					}
					childBlock.addAdditionalNode(someNextElement);
				}
			}

			children.add(childBlock);
		}
		return children;
	}

	public void addAdditionalNode(ASTNode node)
	{
		if(myAdditionalNodes.isEmpty())
		{
			myAdditionalNodes = new ArrayList<ASTNode>(5);
		}
		myAdditionalNodes.add(node);
	}

	@Nullable
	@Override
	public Wrap getWrap()
	{
		return myWrappingProcessor.getWrap();
	}

	@Override
	@RequiredReadAction
	public Indent getIndent()
	{
		return myIndentProcessor.getIndent();
	}

	@Nullable
	@Override
	protected Indent getChildIndent()
	{
		return myIndentProcessor.getChildIndent();
	}

	@Override
	public CSharpFormattingBlock getParent()
	{
		return myParent;
	}

	@Override
	public void setParent(BlockWithParent newParent)
	{
		myParent = (CSharpFormattingBlock) newParent;
	}

	@NotNull
	@RequiredReadAction
	public IElementType getElementType()
	{
		IElementType elementType = getNode().getElementType();
		assert elementType != null : getNode().getText();
		return elementType;
	}
}
