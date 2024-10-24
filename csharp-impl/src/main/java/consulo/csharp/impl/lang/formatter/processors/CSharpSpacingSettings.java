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

package consulo.csharp.impl.lang.formatter.processors;

import consulo.csharp.lang.impl.psi.CSharpStubElementSets;
import consulo.language.codeStyle.ASTBlock;
import consulo.language.codeStyle.CommonCodeStyleSettings;
import consulo.language.codeStyle.Spacing;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.psi.PsiUtilCore;
import consulo.csharp.impl.ide.codeStyle.CSharpCodeStyleSettings;
import consulo.csharp.lang.doc.impl.psi.CSharpDocTokenType;
import consulo.csharp.lang.impl.psi.CSharpElements;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.impl.psi.source.CSharpOperatorReferenceImpl;
import consulo.language.codeStyle.SpacingBuilder;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 11.11.14
 */
public class CSharpSpacingSettings implements CSharpTokens, CSharpElements
{
	private static class OperatorReferenceSpacingBuilder
	{
		private final CommonCodeStyleSettings myCommonSettings;
		private TokenSet myParentSet;
		private final TokenSet myTokenSet;
		private final boolean myCondition;

		OperatorReferenceSpacingBuilder(CommonCodeStyleSettings commonSettings, @Nonnull TokenSet parentSet, IElementType[] types, boolean condition)
		{
			myCommonSettings = commonSettings;
			myParentSet = parentSet;
			myTokenSet = TokenSet.create(types);
			myCondition = condition;
		}

		public boolean match(@Nullable ASTBlock child1, @Nonnull ASTBlock child2)
		{
			CSharpOperatorReferenceImpl operatorReference = findOperatorReference(child1, child2);
			if(operatorReference != null && myParentSet != TokenSet.EMPTY)
			{
				IElementType elementType = PsiUtilCore.getElementType(operatorReference.getParent());
				if(!myParentSet.contains(elementType))
				{
					return false;
				}
			}
			return operatorReference != null && myTokenSet.contains(operatorReference.getOperatorElementType());
		}

		@Nullable
		private static CSharpOperatorReferenceImpl findOperatorReference(@Nullable ASTBlock child1, @Nonnull ASTBlock child2)
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

		@Nonnull
		public Spacing createSpacing()
		{
			int count = myCondition ? 1 : 0;
			return Spacing.createSpacing(count, count, 0, myCommonSettings.KEEP_LINE_BREAKS, myCommonSettings.KEEP_BLANK_LINES_IN_CODE);
		}
	}

	private static TokenSet ourMultiDeclarationSet = TokenSet.create(CSharpStubElements.FIELD_DECLARATION, CSharpElements.LOCAL_VARIABLE, CSharpStubElements.EVENT_DECLARATION);

	private final CommonCodeStyleSettings myCommonSettings;

	private SpacingBuilder myBuilder;
	private List<OperatorReferenceSpacingBuilder> myOperatorReferenceSpacingBuilders = new ArrayList<>();

	public CSharpSpacingSettings(CommonCodeStyleSettings commonSettings, CSharpCodeStyleSettings customSettings)
	{
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

		IElementType[] arrayInitializerElementsTypes = {
				CSharpElements.ARRAY_INITIALIZER,
				CSharpElements.IMPLICIT_ARRAY_INITIALIZATION_EXPRESSION,
				CSharpElements.ARRAY_INITIALIZER_COMPOSITE_VALUE
		};
		//myBuilder.afterInside(CSharpTokens.LBRACE, CSharpElements.ARRAY_INITIALIZER).none();
		//myBuilder.afterInside(CSharpTokens.LBRACE, CSharpElements.IMPLICIT_ARRAY_INITIALIZATION_EXPRESSION).none();
		//myBuilder.afterInside(CSharpTokens.LBRACE, CSharpElements.ARRAY_INITIALIZER_COMPOSITE_VALUE).none();

		//myBuilder.beforeInside(CSharpTokens.RBRACE, CSharpElements.ARRAY_INITIALIZER).none();
		//myBuilder.beforeInside(CSharpTokens.RBRACE, CSharpElements.IMPLICIT_ARRAY_INITIALIZATION_EXPRESSION).none();
		//myBuilder.beforeInside(CSharpTokens.RBRACE, CSharpElements.ARRAY_INITIALIZER_COMPOSITE_VALUE).none();

		for(IElementType arrayInitializerElementsType : arrayInitializerElementsTypes)
		{
			myBuilder.betweenInside(CSharpTokens.COMMA, CSharpElements.ARRAY_INITIALIZER_SINGLE_VALUE, arrayInitializerElementsType).spaces(1);
			myBuilder.betweenInside(CSharpTokens.COMMA, CSharpElements.ARRAY_INITIALIZER_COMPOSITE_VALUE, arrayInitializerElementsType).spaces(1);
		}

		spaceIfNoBlankLines(myBuilder.beforeInside(LBRACE, TYPE_DECLARATION), commonSettings.SPACE_BEFORE_CLASS_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(LBRACE, PROPERTY_DECLARATION), customSettings.SPACE_BEFORE_PROPERTY_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(LBRACE, EVENT_DECLARATION), customSettings.SPACE_BEFORE_EVENT_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(LBRACE, INDEX_METHOD_DECLARATION), customSettings.SPACE_BEFORE_INDEX_METHOD_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(LBRACE, NAMESPACE_DECLARATION), customSettings.SPACE_BEFORE_NAMESPACE_LBRACE);

		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, METHOD_DECLARATION), commonSettings.SPACE_BEFORE_METHOD_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, CONSTRUCTOR_DECLARATION), commonSettings.SPACE_BEFORE_METHOD_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, CONVERSION_METHOD_DECLARATION), commonSettings.SPACE_BEFORE_METHOD_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, XACCESSOR), commonSettings.SPACE_BEFORE_METHOD_LBRACE);

		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, SWITCH_STATEMENT), commonSettings.SPACE_BEFORE_SWITCH_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, FOR_STATEMENT), commonSettings.SPACE_BEFORE_FOR_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, FOREACH_STATEMENT), customSettings.SPACE_BEFORE_FOREACH_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, WHILE_STATEMENT), commonSettings.SPACE_BEFORE_WHILE_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, TRY_STATEMENT), commonSettings.SPACE_BEFORE_TRY_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, CATCH_STATEMENT), commonSettings.SPACE_BEFORE_CATCH_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, FINALLY_STATEMENT), commonSettings.SPACE_BEFORE_FINALLY_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, UNSAFE_STATEMENT), customSettings.SPACE_BEFORE_UNSAFE_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, USING_STATEMENT), customSettings.SPACE_BEFORE_USING_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, LOCK_STATEMENT), customSettings.SPACE_BEFORE_LOCK_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, FIXED_STATEMENT), customSettings.SPACE_BEFORE_FIXED_LBRACE);

		if(customSettings.KEEP_AUTO_PROPERTY_IN_ONE_LINE)
		{
			spaceIfNoBlankLines(myBuilder.afterInside(LBRACE, PROPERTY_DECLARATION), true);
			spaceIfNoBlankLines(myBuilder.afterInside(LBRACE, EVENT_DECLARATION), true);
			spaceIfNoBlankLines(myBuilder.afterInside(LBRACE, INDEX_METHOD_DECLARATION), true);

			spaceIfNoBlankLines(myBuilder.between(XACCESSOR, XACCESSOR), true);

			spaceIfNoBlankLines(myBuilder.beforeInside(RBRACE, PROPERTY_DECLARATION), true);
			spaceIfNoBlankLines(myBuilder.beforeInside(RBRACE, EVENT_DECLARATION), true);
			spaceIfNoBlankLines(myBuilder.beforeInside(RBRACE, INDEX_METHOD_DECLARATION), true);
		}

		// between members - one line
		myBuilder.between(CSharpStubElements.FIELD_DECLARATION, CSharpStubElements.FIELD_DECLARATION).blankLines(commonSettings.BLANK_LINES_AROUND_FIELD);
		myBuilder.between(CSharpStubElementSets.QUALIFIED_MEMBERS, CSharpStubElementSets.QUALIFIED_MEMBERS).blankLines(commonSettings.BLANK_LINES_AROUND_METHOD);

		myBuilder.afterInside(CSharpTokens.LBRACE, CSharpStubElementSets.QUALIFIED_MEMBERS).spacing(0, 0, 1, commonSettings.KEEP_LINE_BREAKS, commonSettings.KEEP_BLANK_LINES_BEFORE_RBRACE);
		myBuilder.beforeInside(CSharpTokens.RBRACE, CSharpStubElementSets.QUALIFIED_MEMBERS).spacing(0, 0, 1, commonSettings.KEEP_LINE_BREAKS, commonSettings.KEEP_BLANK_LINES_BEFORE_RBRACE);

		myBuilder.afterInside(CSharpTokens.LBRACE, CSharpElements.BLOCK_STATEMENT).spacing(0, 0, 1, commonSettings.KEEP_LINE_BREAKS, commonSettings.KEEP_BLANK_LINES_BEFORE_RBRACE);
		myBuilder.beforeInside(CSharpTokens.RBRACE, CSharpElements.BLOCK_STATEMENT).spacing(0, 0, 1, commonSettings.KEEP_LINE_BREAKS, commonSettings.KEEP_BLANK_LINES_BEFORE_RBRACE);

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

		IElementType[] iElementTypes = {
				CSharpElements.TYPE_OF_EXPRESSION,
				CSharpElements.DEFAULT_EXPRESSION,
				CSharpElements.NAMEOF_EXPRESSION,
				CSharpElements.__MAKEREF_EXPRESSION,
				CSharpElements.__REFTYPE_EXPRESSION,
				CSharpElements.__REFVALUE_EXPRESSION,
				CSharpElements.SIZE_OF_EXPRESSION
		};

		for(IElementType elementType : iElementTypes)
		{
			myBuilder.afterInside(CSharpTokens.LPAR, elementType).spaces(0);
			myBuilder.beforeInside(CSharpTokens.RPAR, elementType).spaces(0);
		}

		myBuilder.before(CSharpElements.PARAMETER_LIST).none();
		myBuilder.before(CSharpStubElements.PARAMETER_LIST).none();

		// (Type
		myBuilder.afterInside(CSharpTokens.LPAR, CSharpStubElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_WITHIN_METHOD_PARENTHESES);
		myBuilder.afterInside(CSharpTokens.LPAR, CSharpElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_WITHIN_METHOD_PARENTHESES);
		myBuilder.afterInside(CSharpTokens.LPAR, CSharpElements.LAMBDA_PARAMETER_LIST).spaceIf(commonSettings.SPACE_WITHIN_METHOD_PARENTHESES);
		// , type CSharpTokens.IDENTIFIER)
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
		myBuilder.afterInside(CSharpTokens.LT, CSharpElements.TYPE_ARGUMENTS).none();
		myBuilder.afterInside(CSharpTokens.LT, CSharpStubElements.TYPE_ARGUMENTS).none();
		// <Type, Type
		myBuilder.afterInside(CSharpTokens.COMMA, CSharpElements.TYPE_ARGUMENTS).spaceIf(commonSettings.SPACE_AFTER_COMMA_IN_TYPE_ARGUMENTS);
		myBuilder.afterInside(CSharpTokens.COMMA, CSharpStubElements.TYPE_ARGUMENTS).spaceIf(commonSettings.SPACE_AFTER_COMMA_IN_TYPE_ARGUMENTS);
		// Type>
		myBuilder.beforeInside(CSharpTokens.GT, CSharpElements.TYPE_ARGUMENTS).none();
		myBuilder.beforeInside(CSharpTokens.GT, CSharpStubElements.TYPE_ARGUMENTS).none();

		// <modifier-list> <type>
		myBuilder.between(CSharpStubElements.MODIFIER_LIST, CSharpStubElementSets.TYPE_SET).spaces(1);
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

		myBuilder.beforeInside(CSharpStubElements.IDENTIFIER, TYPE_DECLARATION).spaces(1);
		myBuilder.beforeInside(CSharpTokens.IDENTIFIER, LOCAL_VARIABLE).spaces(1);
		myBuilder.beforeInside(CSharpStubElements.IDENTIFIER, FIELD_DECLARATION).spaces(1);
		myBuilder.betweenInside(CSharpTokens.DOT, CSharpTokens.IDENTIFIER, EVENT_DECLARATION).none();
		myBuilder.betweenInside(CSharpTokens.DOT, CSharpStubElements.IDENTIFIER, EVENT_DECLARATION).none();
		myBuilder.beforeInside(CSharpStubElements.IDENTIFIER, EVENT_DECLARATION).spaces(1);
		myBuilder.betweenInside(CSharpTokens.DOT, CSharpTokens.IDENTIFIER, PROPERTY_DECLARATION).none();
		myBuilder.betweenInside(CSharpTokens.DOT, CSharpStubElements.IDENTIFIER, PROPERTY_DECLARATION).none();
		myBuilder.beforeInside(CSharpStubElements.IDENTIFIER, PROPERTY_DECLARATION).spaces(1);
		myBuilder.betweenInside(CSharpTokens.DOT, CSharpTokens.IDENTIFIER, METHOD_DECLARATION).none();
		myBuilder.betweenInside(CSharpTokens.DOT, CSharpStubElements.IDENTIFIER, METHOD_DECLARATION).none();
		myBuilder.beforeInside(CSharpStubElements.IDENTIFIER, METHOD_DECLARATION).spaces(1);
		myBuilder.beforeInside(CSharpStubElements.IDENTIFIER, CONSTRUCTOR_DECLARATION).spaces(1);
		myBuilder.betweenInside(CSharpTokens.DOT, CSharpTokens.THIS_KEYWORD, INDEX_METHOD_DECLARATION).none();
		myBuilder.beforeInside(CSharpTokens.THIS_KEYWORD, INDEX_METHOD_DECLARATION).spaces(1);

		myBuilder.beforeInside(CSharpTokens.IDENTIFIER, CSharpElements.PARAMETER).spaces(1);
		myBuilder.beforeInside(CSharpTokens.IDENTIFIER, CSharpStubElements.PARAMETER).spaces(1);
		myBuilder.beforeInside(CSharpStubElements.IDENTIFIER, CSharpElements.PARAMETER).spaces(1);
		myBuilder.beforeInside(CSharpStubElements.IDENTIFIER, CSharpStubElements.PARAMETER).spaces(1);

		spaceIfNoBlankLines(myBuilder.afterInside(COLON, CSharpStubElements.EXTENDS_LIST), true);
		spaceIfNoBlankLines(myBuilder.before(CSharpStubElements.EXTENDS_LIST), true);

		// constructor declaration
		spaceIfNoBlankLines(myBuilder.afterInside(COLON, CSharpStubElements.CONSTRUCTOR_DECLARATION), true);
		spaceIfNoBlankLines(myBuilder.beforeInside(COLON, CSharpStubElements.CONSTRUCTOR_DECLARATION), true);

		myBuilder.around(COLONCOLON).none();
		myBuilder.aroundInside(COLON, CSharpElements.GENERIC_CONSTRAINT).spaces(1);
		myBuilder.around(DARROW).spaceIf(commonSettings.SPACE_AROUND_LAMBDA_ARROW);
		myBuilder.around(ARROW).none();

		myBuilder.before(CSharpTokens.ELSE_KEYWORD).spaceIf(commonSettings.SPACE_BEFORE_ELSE_KEYWORD);
		myBuilder.betweenInside(CSharpTokens.ELSE_KEYWORD, CSharpElements.BLOCK_STATEMENT, CSharpElements.IF_STATEMENT).spaceIf(commonSettings.SPACE_BEFORE_ELSE_LBRACE);

		// need be after else declaration
		myBuilder.beforeInside(BLOCK_STATEMENT, IF_STATEMENT).spaceIf(commonSettings.SPACE_BEFORE_IF_LBRACE);

		operatorReferenceSpacing(commonSettings.SPACE_AROUND_EQUALITY_OPERATORS, CSharpTokens.EQEQ, CSharpTokens.NTEQ);
		operatorReferenceSpacing(commonSettings.SPACE_AROUND_LOGICAL_OPERATORS, CSharpTokens.ANDAND, CSharpTokens.OROR);
		operatorReferenceSpacing(commonSettings.SPACE_AROUND_RELATIONAL_OPERATORS, CSharpTokens.LT, CSharpTokens.GT, CSharpTokens.LTEQ, CSharpTokens.GTEQ);
		operatorReferenceSpacing(commonSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS, CSharpTokens.EQ, CSharpTokens.PLUSEQ, CSharpTokens.MINUSEQ, CSharpTokens.MULEQ, CSharpTokens.DIVEQ,
				CSharpTokens.PERCEQ);

		// field, local var, etc initialization
		myBuilder.around(CSharpTokens.EQ).spaceIf(commonSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS);

		operatorReferenceSpacing(commonSettings.SPACE_AROUND_SHIFT_OPERATORS, CSharpTokens.GTGT, CSharpTokens.GTGTEQ, CSharpTokens.LTLT, CSharpTokens.LTLTEQ);

		operatorReferenceSpacingWithParent(commonSettings.SPACE_AROUND_UNARY_OPERATOR, TokenSet.create(POSTFIX_EXPRESSION, PREFIX_EXPRESSION), CSharpTokens.PLUS, CSharpTokens.MINUS,
				CSharpTokens.PLUSPLUS, CSharpTokens.MINUSMINUS, CSharpTokens.MUL, CSharpTokens.AND);

		operatorReferenceSpacing(commonSettings.SPACE_AROUND_ADDITIVE_OPERATORS, CSharpTokens.PLUS, CSharpTokens.MINUS);
		operatorReferenceSpacing(commonSettings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS, CSharpTokens.MUL, CSharpTokens.DIV, CSharpTokens.PERC);
		operatorReferenceSpacing(commonSettings.SPACE_AROUND_BITWISE_OPERATORS, CSharpTokens.XOR, CSharpTokens.AND, CSharpTokens.OR);

		// doc
		myBuilder.after(CSharpDocTokenType.DOC_LINE_START).spacing(1, 1, 0, true, 0);

		// semicolons
		// special hacks for 'for()'
		myBuilder.betweenInside(CSharpTokens.LPAR, CSharpTokens.SEMICOLON, CSharpElements.FOR_STATEMENT).spaces(0);
		myBuilder.betweenInside(CSharpTokens.SEMICOLON, CSharpTokens.RPAR, CSharpElements.FOR_STATEMENT).spaces(0);
		myBuilder.betweenInside(CSharpTokens.SEMICOLON, CSharpTokens.SEMICOLON, CSharpElements.FOR_STATEMENT).spaces(0);

		myBuilder.beforeInside(CSharpTokens.SEMICOLON, CSharpElements.FOR_STATEMENT).spaceIf(commonSettings.SPACE_BEFORE_SEMICOLON);
		myBuilder.afterInside(CSharpTokens.SEMICOLON, CSharpElements.FOR_STATEMENT).spaceIf(commonSettings.SPACE_AFTER_SEMICOLON);
		myBuilder.before(CSharpTokens.SEMICOLON).spaces(0);

		spaceIfNoBlankLines(myBuilder.between(CSharpStubElements.PARAMETER_LIST, CSharpElements.STATEMENT_METHOD_BODY), commonSettings.METHOD_BRACE_STYLE ==CommonCodeStyleSettings.END_OF_LINE);
	}

	private void spaceIfNoBlankLines(SpacingBuilder.RuleBuilder builder, boolean config)
	{
		int count = config ? 1 : 0;
		builder.spacing(count, count, 0, false, 0);
	}

	public void operatorReferenceSpacing(boolean ifCondition, IElementType... types)
	{
		operatorReferenceSpacingWithParent(ifCondition, TokenSet.EMPTY, types);
	}

	public void operatorReferenceSpacingWithParent(boolean ifCondition, @Nonnull TokenSet parents, IElementType... types)
	{
		myOperatorReferenceSpacingBuilders.add(new OperatorReferenceSpacingBuilder(myCommonSettings, parents, types, ifCondition));
	}

	@Nullable
	public Spacing getSpacing(@Nonnull ASTBlock parent, @Nullable ASTBlock child1, @Nonnull ASTBlock child2)
	{
		IElementType elementType1 = PsiUtilCore.getElementType(child1 == null ? null : child1.getNode());
		IElementType elementType2 = PsiUtilCore.getElementType(child2.getNode());
		if(elementType1 == NON_ACTIVE_SYMBOL || elementType2 == NON_ACTIVE_SYMBOL)
		{
			return null;
		}

		for(OperatorReferenceSpacingBuilder operatorReferenceSpacingBuilder : myOperatorReferenceSpacingBuilders)
		{
			if(operatorReferenceSpacingBuilder.match(child1, child2))
			{
				return operatorReferenceSpacingBuilder.createSpacing();
			}
		}

		if(ourMultiDeclarationSet.contains(elementType1) && elementType1 == elementType2)
		{
			ASTNode commaNode = child1.getNode().findChildByType(CSharpTokens.COMMA);
			if(commaNode != null)
			{
				return Spacing.createSpacing(1, 1, 0, false, 0);
			}
		}
		return myBuilder.getSpacing(parent, child1, child2);
	}
}
