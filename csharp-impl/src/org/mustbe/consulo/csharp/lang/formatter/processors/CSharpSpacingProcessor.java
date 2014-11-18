package org.mustbe.consulo.csharp.lang.formatter.processors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.formatter.CSharpFormattingBlock;
import org.mustbe.consulo.csharp.lang.psi.CSharpElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import com.intellij.formatting.ASTBlock;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 11.11.14
 */
public class CSharpSpacingProcessor implements CSharpTokens, CSharpElements
{
	private final CSharpFormattingBlock myParent;
	private final CommonCodeStyleSettings myCodeStyleSettings;

	private SpacingBuilder myBuilder;

	public CSharpSpacingProcessor(CSharpFormattingBlock parent, CommonCodeStyleSettings codeStyleSettings)
	{
		myParent = parent;
		myCodeStyleSettings = codeStyleSettings;

		myBuilder = new SpacingBuilder(codeStyleSettings);
		myBuilder.between(CSharpTokens.IF_KEYWORD, CSharpTokens.LPAR).spaces(codeStyleSettings.SPACE_BEFORE_IF_PARENTHESES ? 1 : 0);
		myBuilder.between(CSharpTokens.FOR_KEYWORD, CSharpTokens.LPAR).spaces(codeStyleSettings.SPACE_BEFORE_FOR_PARENTHESES ? 1 : 0);
		myBuilder.between(CSharpTokens.FOREACH_KEYWORD, CSharpTokens.LPAR).spaces(codeStyleSettings.SPACE_BEFORE_FOR_PARENTHESES ? 1 : 0);
		myBuilder.between(CSharpTokens.WHILE_KEYWORD, CSharpTokens.LPAR).spaces(codeStyleSettings.SPACE_BEFORE_WHILE_PARENTHESES ? 1 : 0);
		myBuilder.between(CSharpTokens.SWITCH_KEYWORD, CSharpTokens.LPAR).spaces(codeStyleSettings.SPACE_BEFORE_SWITCH_PARENTHESES ? 1 : 0);
		myBuilder.between(CSharpTokens.CATCH_KEYWORD, CSharpTokens.LPAR).spaces(codeStyleSettings.SPACE_BEFORE_CATCH_PARENTHESES ? 1 : 0);
	}

	@Nullable
	public Spacing getSpacing(@Nullable ASTBlock child1, @NotNull ASTBlock child2)
	{
		Spacing selfSpacingImpl = getSelfSpacingImpl(child1, child2);
		if(selfSpacingImpl != null)
		{
			return selfSpacingImpl;
		}

		return myBuilder.getSpacing(myParent, child1, child2);
	}

	private Spacing getSelfSpacingImpl(@Nullable ASTBlock child1, @NotNull ASTBlock child2)
	{
		IElementType elementType = myParent.getNode().getElementType();
		IElementType child2ElementType = child2.getNode().getElementType();
		if(child2ElementType == LBRACE || child2ElementType == BLOCK_STATEMENT)
		{
			int braceStyle = myCodeStyleSettings.BRACE_STYLE;
			if(elementType == TYPE_DECLARATION)
			{
				braceStyle = myCodeStyleSettings.CLASS_BRACE_STYLE;
			}
			else if(elementType == METHOD_DECLARATION ||
					elementType == CONSTRUCTOR_DECLARATION ||
					elementType == ARRAY_METHOD_DECLARATION)
			{
				braceStyle = myCodeStyleSettings.METHOD_BRACE_STYLE;
			}

			switch(braceStyle)
			{
				case CommonCodeStyleSettings.END_OF_LINE:
					return Spacing.createSpacing(1, 1, 0, myCodeStyleSettings.KEEP_LINE_BREAKS, myCodeStyleSettings.KEEP_BLANK_LINES_BEFORE_RBRACE);
			}
		}
		return null;
	}
}
