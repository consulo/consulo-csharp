package org.mustbe.consulo.csharp.lang.formatter.processors;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.codeStyle.CSharpCodeStyleSettings;
import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocTokenType;
import org.mustbe.consulo.csharp.lang.formatter.CSharpFormattingBlock;
import org.mustbe.consulo.csharp.lang.psi.CSharpElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpOperatorReferenceImpl;
import com.intellij.formatting.ASTBlock;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * @author VISTALL
 * @since 11.11.14
 */
public class CSharpSpacingProcessor implements CSharpTokens, CSharpElements
{
	private static class OperatorReferenceSpacingBuilder
	{
		private final CommonCodeStyleSettings myCommonSettings;
		private final TokenSet myTokenSet;
		private final boolean myCondition;

		OperatorReferenceSpacingBuilder(CommonCodeStyleSettings commonSettings, IElementType[] types, boolean condition)
		{
			myCommonSettings = commonSettings;
			myTokenSet = TokenSet.create(types);
			myCondition = condition;
		}

		public boolean match(@Nullable ASTBlock child1, @NotNull ASTBlock child2)
		{
			CSharpOperatorReferenceImpl operatorReference = findOperatorReference(child1, child2);
			return operatorReference != null && myTokenSet.contains(operatorReference.getOperatorElementType());
		}

		@Nullable
		private static CSharpOperatorReferenceImpl findOperatorReference(@Nullable ASTBlock child1, @NotNull ASTBlock child2)
		{
			if(child1 != null)
			{
				PsiElement psi = child1.getNode().getPsi();
				if(psi instanceof CSharpOperatorReferenceImpl)
				{
					return (CSharpOperatorReferenceImpl) psi;
				}
			}
			PsiElement psi = child2.getNode().getPsi();
			if(psi instanceof CSharpOperatorReferenceImpl)
			{
				return (CSharpOperatorReferenceImpl) psi;
			}
			return null;
		}

		@NotNull
		public Spacing createSpacing()
		{
			int count = myCondition ? 1 : 0;
			return Spacing.createSpacing(count, count, 0, myCommonSettings.KEEP_LINE_BREAKS, myCommonSettings.KEEP_BLANK_LINES_IN_CODE);
		}
	}

	private final CSharpFormattingBlock myParent;
	private final CommonCodeStyleSettings myCommonSettings;

	private SpacingBuilder myBuilder;
	private List<OperatorReferenceSpacingBuilder> myOperatorReferenceSpacingBuilders = new ArrayList<OperatorReferenceSpacingBuilder>();

	public CSharpSpacingProcessor(CSharpFormattingBlock parent, CommonCodeStyleSettings commonSettings, CSharpCodeStyleSettings customSettings)
	{
		myParent = parent;
		myCommonSettings = commonSettings;

		myBuilder = new SpacingBuilder(commonSettings);

		myBuilder.between(CSharpTokens.IF_KEYWORD, CSharpTokens.LPAR).spaceIf(commonSettings.SPACE_BEFORE_IF_PARENTHESES);
		myBuilder.between(CSharpTokens.FOR_KEYWORD, CSharpTokens.LPAR).spaceIf(commonSettings.SPACE_BEFORE_FOR_PARENTHESES);
		myBuilder.between(CSharpTokens.FOREACH_KEYWORD, CSharpTokens.LPAR).spaceIf(customSettings.SPACE_BEFORE_FOREACH_PARENTHESES);
		myBuilder.between(CSharpTokens.WHILE_KEYWORD, CSharpTokens.LPAR).spaceIf(commonSettings.SPACE_BEFORE_WHILE_PARENTHESES);
		myBuilder.between(CSharpTokens.SWITCH_KEYWORD, CSharpTokens.LPAR).spaceIf(commonSettings.SPACE_BEFORE_SWITCH_PARENTHESES);
		myBuilder.between(CSharpTokens.CATCH_KEYWORD, CSharpTokens.LPAR).spaceIf(commonSettings.SPACE_BEFORE_CATCH_PARENTHESES);
		myBuilder.between(CSharpTokens.USING_KEYWORD, CSharpTokens.LPAR).spaceIf(customSettings.SPACE_BEFORE_USING_PARENTHESES);
		myBuilder.between(CSharpTokens.LOCK_KEYWORD, CSharpTokens.LPAR).spaceIf(customSettings.SPACE_BEFORE_LOCK_PARENTHESES);
		myBuilder.between(CSharpTokens.FIXED_KEYWORD, CSharpTokens.LPAR).spaceIf(customSettings.SPACE_BEFORE_FIXED_PARENTHESES);

		myBuilder.afterInside(CSharpTokens.LBRACE, CSharpElements.ARRAY_INITIALIZER_COMPOSITE_VALUE).spaces(0);
		myBuilder.beforeInside(CSharpTokens.RBRACE, CSharpElements.ARRAY_INITIALIZER_COMPOSITE_VALUE).spaces(0);

		myBuilder.beforeInside(LBRACE, TYPE_DECLARATION).spaceIf(commonSettings.SPACE_BEFORE_CLASS_LBRACE);
		myBuilder.beforeInside(LBRACE, PROPERTY_DECLARATION).spaceIf(customSettings.SPACE_BEFORE_PROPERTY_LBRACE);
		myBuilder.beforeInside(LBRACE, EVENT_DECLARATION).spaceIf(customSettings.SPACE_BEFORE_EVENT_LBRACE);
		myBuilder.beforeInside(LBRACE, ARRAY_METHOD_DECLARATION).spaceIf(customSettings.SPACE_BEFORE_INDEX_METHOD_LBRACE);
		myBuilder.beforeInside(LBRACE, NAMESPACE_DECLARATION).spaceIf(customSettings.SPACE_BEFORE_NAMESPACE_LBRACE);

		myBuilder.beforeInside(BLOCK_STATEMENT, METHOD_DECLARATION).spaceIf(commonSettings.SPACE_BEFORE_METHOD_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, CONSTRUCTOR_DECLARATION).spaceIf(commonSettings.SPACE_BEFORE_METHOD_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, CONVERSION_METHOD_DECLARATION).spaceIf(commonSettings.SPACE_BEFORE_METHOD_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, XXX_ACCESSOR).spaceIf(commonSettings.SPACE_BEFORE_METHOD_LBRACE);

		myBuilder.beforeInside(BLOCK_STATEMENT, SWITCH_STATEMENT).spaceIf(commonSettings.SPACE_BEFORE_SWITCH_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, FOR_STATEMENT).spaceIf(commonSettings.SPACE_BEFORE_FOR_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, FOREACH_STATEMENT).spaceIf(customSettings.SPACE_BEFORE_FOREACH_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, WHILE_STATEMENT).spaceIf(commonSettings.SPACE_BEFORE_WHILE_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, TRY_STATEMENT).spaceIf(commonSettings.SPACE_BEFORE_TRY_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, CATCH_STATEMENT).spaceIf(commonSettings.SPACE_BEFORE_CATCH_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, FINALLY_STATEMENT).spaceIf(commonSettings.SPACE_BEFORE_FINALLY_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, UNSAFE_STATEMENT).spaceIf(customSettings.SPACE_BEFORE_UNSAFE_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, USING_STATEMENT).spaceIf(customSettings.SPACE_BEFORE_USING_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, LOCK_STATEMENT).spaceIf(customSettings.SPACE_BEFORE_LOCK_LBRACE);
		myBuilder.beforeInside(BLOCK_STATEMENT, FIXED_STATEMENT).spaceIf(customSettings.SPACE_BEFORE_FIXED_LBRACE);

		// call(arg
		myBuilder.afterInside(CSharpTokens.LPAR, CSharpElements.CALL_ARGUMENT_LIST).spaceIf(commonSettings.SPACE_WITHIN_METHOD_CALL_PARENTHESES);
		// call[arg
		myBuilder.afterInside(CSharpTokens.LBRACKET, CSharpElements.CALL_ARGUMENT_LIST).spaceIf(commonSettings.SPACE_WITHIN_BRACKETS);
		// arg)
		myBuilder.beforeInside(CSharpTokens.RPAR, CSharpElements.CALL_ARGUMENT_LIST).spaceIf(commonSettings.SPACE_WITHIN_METHOD_CALL_PARENTHESES);
		// arg]
		myBuilder.beforeInside(CSharpTokens.RBRACKET, CSharpElements.CALL_ARGUMENT_LIST).spaceIf(commonSettings.SPACE_WITHIN_BRACKETS);
		// arg, arg
		myBuilder.afterInside(CSharpTokens.COMMA, CSharpElements.CALL_ARGUMENT_LIST).spaceIf(commonSettings.SPACE_AFTER_COMMA);
		myBuilder.beforeInside(CSharpTokens.COMMA, CSharpElements.CALL_ARGUMENT_LIST).spaceIf(commonSettings.SPACE_BEFORE_COMMA);
		// call(
		myBuilder.before(CSharpElements.CALL_ARGUMENT_LIST).spaceIf(commonSettings.SPACE_BEFORE_METHOD_CALL_PARENTHESES);

		// (Type
		myBuilder.afterInside(CSharpTokens.LPAR, CSharpStubElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_WITHIN_METHOD_PARENTHESES);
		myBuilder.afterInside(CSharpTokens.LPAR, CSharpElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_WITHIN_METHOD_PARENTHESES);
		myBuilder.afterInside(CSharpTokens.LPAR, CSharpElements.LAMBDA_PARAMETER_LIST).spaceIf(commonSettings.SPACE_WITHIN_METHOD_PARENTHESES);
		// , type identifier)
		myBuilder.afterInside(CSharpTokens.COMMA, CSharpStubElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_AFTER_COMMA);
		myBuilder.afterInside(CSharpTokens.COMMA, CSharpElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_AFTER_COMMA);
		myBuilder.afterInside(CSharpTokens.COMMA, CSharpElements.LAMBDA_PARAMETER_LIST).spaceIf(commonSettings.SPACE_AFTER_COMMA);
		myBuilder.beforeInside(CSharpTokens.COMMA, CSharpStubElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_BEFORE_COMMA);
		myBuilder.beforeInside(CSharpTokens.COMMA, CSharpElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_BEFORE_COMMA);
		myBuilder.beforeInside(CSharpTokens.COMMA, CSharpElements.LAMBDA_PARAMETER_LIST).spaceIf(commonSettings.SPACE_BEFORE_COMMA);
		// name)
		myBuilder.beforeInside(CSharpTokens.RPAR, CSharpStubElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_WITHIN_METHOD_PARENTHESES);
		myBuilder.beforeInside(CSharpTokens.RPAR, CSharpElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_WITHIN_METHOD_PARENTHESES);
		myBuilder.beforeInside(CSharpTokens.RPAR, CSharpElements.LAMBDA_PARAMETER_LIST).spaceIf(commonSettings.SPACE_WITHIN_METHOD_PARENTHESES);

		// <Type
		myBuilder.afterInside(CSharpTokens.LT, CSharpElements.TYPE_ARGUMENTS).spaces(0);
		myBuilder.afterInside(CSharpTokens.LT, CSharpStubElements.TYPE_ARGUMENTS).spaces(0);
		// <Type, Type
		myBuilder.afterInside(CSharpTokens.COMMA, CSharpElements.TYPE_ARGUMENTS).spaceIf(commonSettings.SPACE_AFTER_COMMA_IN_TYPE_ARGUMENTS);
		myBuilder.afterInside(CSharpTokens.COMMA, CSharpStubElements.TYPE_ARGUMENTS).spaceIf(commonSettings.SPACE_AFTER_COMMA_IN_TYPE_ARGUMENTS);
		// Type>
		myBuilder.beforeInside(CSharpTokens.GT, CSharpElements.TYPE_ARGUMENTS).spaces(0);
		myBuilder.beforeInside(CSharpTokens.GT, CSharpStubElements.TYPE_ARGUMENTS).spaces(0);

		// <modifier-list> <type>
		myBuilder.between(CSharpStubElements.MODIFIER_LIST, CSharpStubElements.TYPE_SET).spaces(1);
		// <modifier> <modifier>
		myBuilder.between(CSharpTokenSets.MODIFIERS, CSharpTokenSets.MODIFIERS).spaces(1);
		// [Att]
		// [Att]
		myBuilder.between(CSharpStubElements.ATTRIBUTE_LIST, CSharpStubElements.ATTRIBUTE_LIST).blankLines(0);
		// [Att]
		// <modifier>
		myBuilder.between(CSharpStubElements.ATTRIBUTE_LIST, CSharpTokenSets.MODIFIERS).blankLines(0);

		// name(parameterList)
		myBuilder.between(CSharpTokens.IDENTIFIER, CSharpStubElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_BEFORE_METHOD_PARENTHESES);
		// delegate(parameterList)
		myBuilder.between(CSharpTokens.DELEGATE_KEYWORD, CSharpElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_BEFORE_METHOD_PARENTHESES);

		myBuilder.beforeInside(IDENTIFIER, TYPE_DECLARATION).spaces(1);
		myBuilder.beforeInside(IDENTIFIER, LOCAL_VARIABLE).spaces(1);
		myBuilder.beforeInside(IDENTIFIER, FIELD_DECLARATION).spaces(1);
		myBuilder.betweenInside(DOT, IDENTIFIER, EVENT_DECLARATION).spaces(0);
		myBuilder.beforeInside(IDENTIFIER, EVENT_DECLARATION).spaces(1);
		myBuilder.betweenInside(DOT, IDENTIFIER, PROPERTY_DECLARATION).spaces(0);
		myBuilder.beforeInside(IDENTIFIER, PROPERTY_DECLARATION).spaces(1);
		myBuilder.betweenInside(DOT, IDENTIFIER, METHOD_DECLARATION).spaces(0);
		myBuilder.beforeInside(IDENTIFIER, METHOD_DECLARATION).spaces(1);
		myBuilder.beforeInside(IDENTIFIER, CONSTRUCTOR_DECLARATION).spaces(1);
		myBuilder.beforeInside(THIS_KEYWORD, ARRAY_METHOD_DECLARATION).spaces(1);
		myBuilder.beforeInside(IDENTIFIER, CSharpElements.PARAMETER).spaces(1);
		myBuilder.beforeInside(IDENTIFIER, CSharpStubElements.PARAMETER).spaces(1);

		myBuilder.afterInside(COLON, CSharpStubElements.EXTENDS_LIST).spaces(1);
		myBuilder.before(CSharpStubElements.EXTENDS_LIST).spaces(1);

		myBuilder.around(COLONCOLON).spaces(0);
		myBuilder.around(DARROW).spaces(1);
		myBuilder.around(ARROW).spaces(0);

		myBuilder.before(CSharpTokens.ELSE_KEYWORD).spaceIf(commonSettings.SPACE_BEFORE_ELSE_KEYWORD);
		myBuilder.betweenInside(CSharpTokens.ELSE_KEYWORD, CSharpElements.BLOCK_STATEMENT, CSharpElements.IF_STATEMENT).spaceIf(commonSettings
				.SPACE_BEFORE_ELSE_LBRACE);

		// need be after else declaration
		myBuilder.beforeInside(BLOCK_STATEMENT, IF_STATEMENT).spaceIf(commonSettings.SPACE_BEFORE_IF_LBRACE);

		operatorReferenceSpacing(commonSettings.SPACE_AROUND_EQUALITY_OPERATORS, CSharpTokens.EQEQ, CSharpTokens.NTEQ);
		operatorReferenceSpacing(commonSettings.SPACE_AROUND_LOGICAL_OPERATORS, CSharpTokens.ANDAND, CSharpTokens.OROR);
		operatorReferenceSpacing(commonSettings.SPACE_AROUND_RELATIONAL_OPERATORS, CSharpTokens.LT, CSharpTokens.GT, CSharpTokens.LTEQ,
				CSharpTokens.GTEQ);
		operatorReferenceSpacing(commonSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS, CSharpTokens.EQ, CSharpTokens.PLUSEQ, CSharpTokens.MINUSEQ,
				CSharpTokens.MULEQ, CSharpTokens.DIVEQ, CSharpTokens.PERCEQ);

		// field, local var, etc initialization
		myBuilder.around(CSharpTokens.EQ).spaceIf(commonSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS);

		operatorReferenceSpacing(commonSettings.SPACE_AROUND_SHIFT_OPERATORS, CSharpTokens.GTGT, CSharpTokens.GTGTEQ, CSharpTokens.LTLT,
				CSharpTokens.LTLTEQ);
		operatorReferenceSpacing(commonSettings.SPACE_AROUND_ADDITIVE_OPERATORS, CSharpTokens.PLUS, CSharpTokens.MINUS);
		operatorReferenceSpacing(commonSettings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS, CSharpTokens.MUL, CSharpTokens.DIV, CSharpTokens.PERC);
		operatorReferenceSpacing(commonSettings.SPACE_AROUND_BITWISE_OPERATORS, CSharpTokens.XOR, CSharpTokens.AND, CSharpTokens.OR);

		// doc
		myBuilder.after(CSharpDocTokenType.DOC_LINE_START).spacing(1, 1, 0, true, 0);
	}

	public void operatorReferenceSpacing(boolean ifCondition, IElementType... types)
	{
		myOperatorReferenceSpacingBuilders.add(new OperatorReferenceSpacingBuilder(myCommonSettings, types, ifCondition));
	}

	@Nullable
	public Spacing getSpacing(@Nullable ASTBlock child1, @NotNull ASTBlock child2)
	{
		for(OperatorReferenceSpacingBuilder operatorReferenceSpacingBuilder : myOperatorReferenceSpacingBuilders)
		{
			if(operatorReferenceSpacingBuilder.match(child1, child2))
			{
				return operatorReferenceSpacingBuilder.createSpacing();
			}
		}
		return myBuilder.getSpacing(myParent, child1, child2);
	}
}
