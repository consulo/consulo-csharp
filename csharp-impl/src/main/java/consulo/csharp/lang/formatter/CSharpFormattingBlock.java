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

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.codeStyle.CSharpCodeStyleSettings;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.formatter.processors.CSharpIndentProcessor;
import consulo.csharp.lang.formatter.processors.CSharpSpacingSettings;
import consulo.csharp.lang.formatter.processors.CSharpWrappingProcessor;
import consulo.csharp.lang.impl.psi.CSharpElements;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.codeStyle.*;
import consulo.language.codeStyle.template.BlockWithParent;
import consulo.language.util.CommentUtilCore;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.SmartList;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public class CSharpFormattingBlock extends AbstractBlock implements CSharpElements, CSharpTokens, CSharpTokenSets, BlockWithParent
{
	private final CSharpWrappingProcessor myWrappingProcessor;
	private final CSharpIndentProcessor myIndentProcessor;
	private final CSharpSpacingSettings mySpacingSettings;
	private final CodeStyleSettings mySettings;

	private List<ASTNode> myAdditionalNodes = Collections.emptyList();
	private CSharpFormattingBlock myParent;

	public CSharpFormattingBlock(@Nonnull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment, @Nonnull CodeStyleSettings settings, @Nonnull CSharpSpacingSettings spacingSettings)
	{
		super(node, wrap, alignment);
		mySettings = settings;
		CommonCodeStyleSettings commonSettings = settings.getCommonSettings(CSharpLanguage.INSTANCE);
		CSharpCodeStyleSettings customSettings = settings.getCustomSettings(CSharpCodeStyleSettings.class);

		myWrappingProcessor = new CSharpWrappingProcessor(node, commonSettings, customSettings);
		myIndentProcessor = new CSharpIndentProcessor(this, commonSettings, customSettings);
		mySpacingSettings = spacingSettings;
	}

	@Nullable
	@Override
	public Spacing getSpacing(@Nullable Block child1, @Nonnull Block child2)
	{
		if((!(child1 instanceof ASTBlock) || !(child2 instanceof ASTBlock)))
		{
			return null;
		}
		return mySpacingSettings.getSpacing(this, (ASTBlock) child1, (ASTBlock) child2);
	}

	@Override
	public boolean isLeaf()
	{
		return getNode().getFirstChildNode() == null || getNode().getElementType() == NON_ACTIVE_SYMBOL;
	}

	@Nonnull
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
	@RequiredReadAction
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
			if(!(rawNode instanceof CSharpDisabledBlock) && FormatterUtil.containsWhiteSpacesOnly(rawNode))
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
					if(!(node instanceof CSharpDisabledBlock) && StringUtil.containsLineBreak(node.getChars()))
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
				final CSharpFormattingBlock childBlock = new CSharpFormattingBlock(next, null, null, mySettings, mySpacingSettings);
				blockWithParent = childBlock;

				IElementType elementType = next.getElementType();
				if(elementType == CASE_OR_DEFAULT_STATEMENT || elementType == CASE_PATTERN_STATEMENT)
				{
					ASTNode someNextElement;
					while((someNextElement = nodes.poll()) != null)
					{
						IElementType someNextElementType = someNextElement.getElementType();
						if(someNextElementType == CASE_OR_DEFAULT_STATEMENT || someNextElementType == CASE_PATTERN_STATEMENT || someNextElementType == RBRACE)
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

	@Nonnull
	@RequiredReadAction
	public IElementType getElementType()
	{
		IElementType elementType = getNode().getElementType();
		assert elementType != null : getNode().getText();
		return elementType;
	}
}
