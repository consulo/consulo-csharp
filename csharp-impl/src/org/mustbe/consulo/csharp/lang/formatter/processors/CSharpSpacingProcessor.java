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

		myBuilder.between(CSharpTokens.IF_KEYWORD, CSharpTokens.LPAR).spaceIf(codeStyleSettings.SPACE_BEFORE_IF_PARENTHESES);
		myBuilder.between(CSharpTokens.FOR_KEYWORD, CSharpTokens.LPAR).spaceIf(codeStyleSettings.SPACE_BEFORE_FOR_PARENTHESES);
		myBuilder.between(CSharpTokens.FOREACH_KEYWORD, CSharpTokens.LPAR).spaceIf(codeStyleSettings.SPACE_BEFORE_FOR_PARENTHESES);
		myBuilder.between(CSharpTokens.WHILE_KEYWORD, CSharpTokens.LPAR).spaceIf(codeStyleSettings.SPACE_BEFORE_WHILE_PARENTHESES);
		myBuilder.between(CSharpTokens.SWITCH_KEYWORD, CSharpTokens.LPAR).spaceIf(codeStyleSettings.SPACE_BEFORE_SWITCH_PARENTHESES);
		myBuilder.between(CSharpTokens.CATCH_KEYWORD, CSharpTokens.LPAR).spaceIf(codeStyleSettings.SPACE_BEFORE_CATCH_PARENTHESES);

		myBuilder.beforeInside(LBRACE, TYPE_DECLARATION).spaceIf(codeStyleSettings.SPACE_BEFORE_CLASS_LBRACE);
		myBuilder.beforeInside(LBRACE, PROPERTY_DECLARATION).spaceIf(codeStyleSettings.SPACE_BEFORE_METHOD_LBRACE);
		myBuilder.beforeInside(LBRACE, EVENT_DECLARATION).spaceIf(codeStyleSettings.SPACE_BEFORE_METHOD_LBRACE);
		myBuilder.beforeInside(LBRACE, ARRAY_METHOD_DECLARATION).spaceIf(codeStyleSettings.SPACE_BEFORE_METHOD_LBRACE);
		myBuilder.beforeInside(LBRACE, NAMESPACE_DECLARATION).spaceIf(codeStyleSettings.SPACE_BEFORE_CLASS_LBRACE);

		myBuilder.beforeInside(BLOCK_STATEMENT, METHOD_DECLARATION).spaceIf(codeStyleSettings.SPACE_BEFORE_METHOD_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, CONSTRUCTOR_DECLARATION).spaceIf(codeStyleSettings.SPACE_BEFORE_METHOD_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, CONVERSION_METHOD_DECLARATION).spaceIf(codeStyleSettings.SPACE_BEFORE_METHOD_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, XXX_ACCESSOR).spaceIf(codeStyleSettings.SPACE_BEFORE_METHOD_LBRACE);

		myBuilder.beforeInside(BLOCK_STATEMENT, SWITCH_STATEMENT).spaceIf(codeStyleSettings.SPACE_BEFORE_SWITCH_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, FOR_STATEMENT).spaceIf(codeStyleSettings.SPACE_BEFORE_FOR_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, FOREACH_STATEMENT).spaceIf(codeStyleSettings.SPACE_BEFORE_FOR_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, WHILE_STATEMENT).spaceIf(codeStyleSettings.SPACE_BEFORE_WHILE_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, TRY_STATEMENT).spaceIf(codeStyleSettings.SPACE_BEFORE_TRY_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, CATCH_STATEMENT).spaceIf(codeStyleSettings.SPACE_BEFORE_CATCH_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, FINALLY_STATEMENT).spaceIf(codeStyleSettings.SPACE_BEFORE_FINALLY_LBRACE);

		//TODO [VISTALL] new config
		myBuilder.beforeInside(BLOCK_STATEMENT, UNSAFE_STATEMENT).spaceIf(codeStyleSettings.SPACE_BEFORE_WHILE_LBRACE);

		myBuilder.before(CSharpTokens.ELSE_KEYWORD).spaceIf(codeStyleSettings.SPACE_BEFORE_ELSE_KEYWORD);
		myBuilder.betweenInside(CSharpTokens.ELSE_KEYWORD, CSharpElements.BLOCK_STATEMENT, CSharpElements.IF_STATEMENT).spaceIf(codeStyleSettings
				.SPACE_BEFORE_ELSE_LBRACE);

		// need be after else declaration
		myBuilder.beforeInside(BLOCK_STATEMENT, IF_STATEMENT).spaceIf(codeStyleSettings.SPACE_BEFORE_IF_LBRACE);
	}

	@Nullable
	public Spacing getSpacing(@Nullable ASTBlock child1, @NotNull ASTBlock child2)
	{
		return myBuilder.getSpacing(myParent, child1, child2);
	}
}
