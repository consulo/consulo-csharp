package org.mustbe.consulo.csharp.lang.formatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpMacroLanguage;
import org.mustbe.consulo.csharp.lang.psi.CSharpTemplateTokens;
import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.TokenSet;

/**
 * @author VISTALL
 * @since 21.03.14
 */
public class CSharpMacroFormattingBlock extends AbstractBlock
{
	public CSharpMacroFormattingBlock(
			@NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment)
	{
		super(node, wrap, alignment);
	}

	@Override
	protected List<Block> buildChildren()
	{
		ASTNode thisNode = getNode();

		List<Block> blocks = new ArrayList<Block>();

		ParserDefinition parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(CSharpMacroLanguage.INSTANCE);
		TokenSet whitespaceTokens = parserDefinition.getWhitespaceTokens(CSharpMacroLanguage.INSTANCE.getVersions()[0]);


		ASTNode temp = thisNode.getFirstChildNode();
		while(temp != null)
		{

			if(!whitespaceTokens.contains(temp.getElementType()))
			{
				blocks.add(new CSharpMacroFormattingBlock(temp, myWrap, myAlignment));
			}

			temp = temp.getTreeNext();
		}


		return blocks.isEmpty() ? Collections.<Block>emptyList() : blocks;
	}

	@Override
	public Indent getIndent()
	{
		return Indent.getAbsoluteNoneIndent();
	}

	@Nullable
	@Override
	public Spacing getSpacing(
			@Nullable Block child1, @NotNull Block child2)
	{
		return null;
	}

	@Override
	public boolean isLeaf()
	{
		return getNode().getElementType() == CSharpTemplateTokens.OUTER_ELEMENT_TYPE;
	}
}
