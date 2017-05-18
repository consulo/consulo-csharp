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

package consulo.csharp.lang.formatter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.formatting.ASTBlock;
import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.templateLanguages.BlockWithParent;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.SmartList;
import com.intellij.util.codeInsight.CommentUtilCore;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.codeStyle.CSharpCodeStyleSettings;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.formatter.processors.CSharpIndentProcessor;
import consulo.csharp.lang.formatter.processors.CSharpSpacingProcessor;
import consulo.csharp.lang.formatter.processors.CSharpWrappingProcessor;
import consulo.csharp.lang.psi.CSharpElements;
import consulo.csharp.lang.psi.CSharpTokenSets;
import consulo.csharp.lang.psi.CSharpTokens;

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
		if((!(child1 instanceof ASTBlock) || !(child2 instanceof ASTBlock)))
		{
			return null;
		}
		return mySpacingProcessor.getSpacing((ASTBlock) child1, (ASTBlock) child2);
	}

	@Override
	public boolean isLeaf()
	{
		return getNode().getFirstChildNode() == null || getNode().getElementType() == NON_ACTIVE_SYMBOL;
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

		List<ASTNode> rawNodes = new ArrayList<>();

		for(ASTNode childNode = getNode().getFirstChildNode(); childNode != null; childNode = childNode.getTreeNext())
		{
			rawNodes.add(childNode);
		}
		rawNodes.addAll(myAdditionalNodes);

		Deque<ASTNode> nodes = new ArrayDeque<>();

		List<ASTNode> whitespaces = null;

		List<ASTNode> disabledNodes = null;

		for(ASTNode rawNode : rawNodes)
		{
			// if its whitespace node - add it to tempWhitespaceHolder or to disabled block
			if(FormatterUtil.containsWhiteSpacesOnly(rawNode))
			{
				if(disabledNodes != null)
				{
					disabledNodes.add(rawNode);
				}
				else
				{
					if(whitespaces == null)
					{
						whitespaces = new SmartList<>();
					}

					whitespaces.add(rawNode);
				}

				continue;
			}

			if(disabledNodes != null && CommentUtilCore.isComment(rawNode))
			{
				disabledNodes.add(rawNode);
				continue;
			}

			if(rawNode.getElementType() == CSharpTokens.NON_ACTIVE_SYMBOL)
			{
				if(disabledNodes == null)
				{
					disabledNodes = new ArrayList<>();
				}

				disabledNodes.add(rawNode);
			}
			else
			{
				if(disabledNodes != null)
				{
					ASTNode node = disabledNodes.get(disabledNodes.size() - 1);
					if(StringUtil.containsLineBreak(node.getChars()))
					{
						disabledNodes.remove(disabledNodes.remove(disabledNodes.size() - 1));
					}
					nodes.add(new CSharpDisabledBlock(disabledNodes));
				}

				// drop disable nodes
				// drop whitespaces if defined
				disabledNodes = null;
				whitespaces = null;

				nodes.add(rawNode);
			}
		}

		if(disabledNodes != null)
		{
			nodes.add(new CSharpDisabledBlock(disabledNodes));

			disabledNodes = null;
			whitespaces = null;
		}

		List<Block> children = new ArrayList<>();
		ASTNode next;
		while((next = nodes.poll()) != null)
		{
			BlockWithParent blockWithParent = null;
			if(next instanceof CSharpDisabledBlock)
			{
				blockWithParent = new CSharpDisabledFormattingBlock(next);
			}
			else
			{
				final CSharpFormattingBlock childBlock = new CSharpFormattingBlock(next, null, null, mySettings);
				blockWithParent = childBlock;

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
			}

			blockWithParent.setParent(this);
			children.add((Block) blockWithParent);
		}
		return children;
	}

	public void addAdditionalNode(ASTNode node)
	{
		if(myAdditionalNodes.isEmpty())
		{
			myAdditionalNodes = new ArrayList<>(5);
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
