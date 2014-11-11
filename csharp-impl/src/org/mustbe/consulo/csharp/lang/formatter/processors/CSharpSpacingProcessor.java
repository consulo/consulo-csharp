package org.mustbe.consulo.csharp.lang.formatter.processors;

import org.jetbrains.annotations.Nullable;
import com.intellij.formatting.Block;
import com.intellij.formatting.Spacing;
import com.intellij.lang.ASTNode;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;

/**
 * @author VISTALL
 * @since 11.11.14
 */
public class CSharpSpacingProcessor
{
	private final ASTNode myNode;
	private final CommonCodeStyleSettings myCodeStyleSettings;

	public CSharpSpacingProcessor(ASTNode node, CommonCodeStyleSettings codeStyleSettings)
	{
		myNode = node;
		myCodeStyleSettings = codeStyleSettings;
	}

	@Nullable
	public Spacing getSpacing(Block child1, Block child2)
	{
		return null;
	}
}
