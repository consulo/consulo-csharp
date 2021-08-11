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

package consulo.csharp.lang.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.parser.CSharpBuilderWrapper;
import consulo.csharp.lang.parser.ModifierSet;
import consulo.csharp.lang.parser.SharedParsingHelpers;
import consulo.csharp.lang.parser.exp.ExpressionParsing;
import consulo.csharp.lang.parser.stmt.StatementParsing;
import consulo.csharp.lang.psi.impl.CompositeElementTypeAsPsiFactory;
import consulo.csharp.lang.psi.impl.elementType.BaseMethodBodyLazyElementType;
import consulo.csharp.lang.psi.impl.source.*;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 22.11.13.
 */
public interface CSharpElements
{
	IElementType NAMESPACE_DECLARATION = CSharpStubElements.NAMESPACE_DECLARATION;

	IElementType USING_NAMESPACE_STATEMENT = CSharpStubElements.USING_NAMESPACE_STATEMENT;

	IElementType USING_TYPE_STATEMENT = CSharpStubElements.USING_TYPE_STATEMENT;

	IElementType TYPE_DEF_STATEMENT = CSharpStubElements.TYPE_DEF_STATEMENT;

	IElementType METHOD_DECLARATION = CSharpStubElements.METHOD_DECLARATION;

	IElementType CONSTRUCTOR_DECLARATION = CSharpStubElements.CONSTRUCTOR_DECLARATION;

	IElementType PARAMETER_LIST = new CompositeElementTypeAsPsiFactory("PARAMETER_LIST", CSharpLanguage.INSTANCE, CSharpParameterListImpl.class);

	IElementType PARAMETER = new CompositeElementTypeAsPsiFactory("PARAMETER", CSharpLanguage.INSTANCE, CSharpParameterImpl.class);

	IElementType TYPE_DECLARATION = CSharpStubElements.TYPE_DECLARATION;

	IElementType DUMMY_DECLARATION = CSharpStubElements.DUMMY_DECLARATION;

	IElementType EVENT_DECLARATION = CSharpStubElements.EVENT_DECLARATION;

	IElementType CONVERSION_METHOD_DECLARATION = CSharpStubElements.CONVERSION_METHOD_DECLARATION;

	IElementType XXX_ACCESSOR = CSharpStubElements.XACCESSOR;

	IElementType FIELD_DECLARATION = CSharpStubElements.FIELD_DECLARATION;

	IElementType ENUM_CONSTANT_DECLARATION = CSharpStubElements.ENUM_CONSTANT_DECLARATION;

	IElementType LOCAL_VARIABLE = new CompositeElementTypeAsPsiFactory("LOCAL_VARIABLE", CSharpLanguage.INSTANCE, CSharpLocalVariableImpl.class);

	IElementType LOCAL_METHOD = new CompositeElementTypeAsPsiFactory("LOCAL_METHOD", CSharpLanguage.INSTANCE, CSharpLocalMethodDeclarationImpl.class);

	IElementType LOCAL_METHOD_STATEMENT = new CompositeElementTypeAsPsiFactory("LOCAL_METHOD_STATEMENT", CSharpLanguage.INSTANCE, CSharpLocalMethodDeclarationStatementImpl.class);

	IElementType PROPERTY_DECLARATION = CSharpStubElements.PROPERTY_DECLARATION;

	IElementType INDEX_METHOD_DECLARATION = CSharpStubElements.INDEX_METHOD_DECLARATION;

	IElementType GENERIC_PARAMETER_LIST = CSharpStubElements.GENERIC_PARAMETER_LIST;

	IElementType GENERIC_PARAMETER = CSharpStubElements.GENERIC_PARAMETER;

	IElementType GENERIC_CONSTRAINT_LIST = CSharpStubElements.GENERIC_CONSTRAINT_LIST;

	IElementType GENERIC_CONSTRAINT = CSharpStubElements.GENERIC_CONSTRAINT;

	IElementType GENERIC_CONSTRAINT_KEYWORD_VALUE = CSharpStubElements.GENERIC_CONSTRAINT_KEYWORD_VALUE;

	IElementType GENERIC_CONSTRAINT_TYPE_VALUE = CSharpStubElements.GENERIC_CONSTRAINT_TYPE_VALUE;

	IElementType USER_TYPE = new CompositeElementTypeAsPsiFactory("USER_TYPE", CSharpLanguage.INSTANCE, CSharpUserTypeImpl.class);

	IElementType POINTER_TYPE = new CompositeElementTypeAsPsiFactory("POINTER_TYPE", CSharpLanguage.INSTANCE, CSharpPointerTypeImpl.class);

	IElementType TUPLE_TYPE = new CompositeElementTypeAsPsiFactory("TUPLE_TYPE", CSharpLanguage.INSTANCE, CSharpTupleTypeImpl.class);

	IElementType TUPLE_VARIABLE = new CompositeElementTypeAsPsiFactory("TUPLE_VARIABLE", CSharpLanguage.INSTANCE, CSharpTupleVariableImpl.class);

	IElementType TUPLE_ELEMENT = new CompositeElementTypeAsPsiFactory("TUPLE_ELEMENT", CSharpLanguage.INSTANCE, CSharpTupleElementImpl.class);

	IElementType TUPLE_EXPRESSION = new CompositeElementTypeAsPsiFactory("TUPLE_EXPRESSION", CSharpLanguage.INSTANCE, CSharpTupleExpressionImpl.class);

	IElementType NULLABLE_TYPE = new CompositeElementTypeAsPsiFactory("NULLABLE_TYPE", CSharpLanguage.INSTANCE, CSharpNullableTypeImpl.class);

	IElementType NATIVE_TYPE = new CompositeElementTypeAsPsiFactory("NATIVE_TYPE", CSharpLanguage.INSTANCE, CSharpNativeTypeImpl.class);

	IElementType ARRAY_TYPE = new CompositeElementTypeAsPsiFactory("ARRAY_TYPE", CSharpLanguage.INSTANCE, CSharpArrayTypeImpl.class);

	IElementType MODIFIER_LIST = new CompositeElementTypeAsPsiFactory("MODIFIER_LIST", CSharpLanguage.INSTANCE, CSharpModifierListImpl.class);

	IElementType EXTENDS_LIST = CSharpStubElements.EXTENDS_LIST;

	IElementType TYPE_ARGUMENTS = new CompositeElementTypeAsPsiFactory("TYPE_ARGUMENTS", CSharpLanguage.INSTANCE, CSharpTypeListImpl.class);

	IElementType EMPTY_TYPE_ARGUMENTS = new CompositeElementTypeAsPsiFactory("EMPTY_TYPE_ARGUMENTS", CSharpLanguage.INSTANCE, CSharpEmptyTypeListImpl.class);

	IElementType CONSTANT_EXPRESSION = new CompositeElementTypeAsPsiFactory("CONSTANT_EXPRESSION", CSharpLanguage.INSTANCE, CSharpConstantExpressionImpl.class);

	IElementType REFERENCE_EXPRESSION = new CompositeElementTypeAsPsiFactory("REFERENCE_EXPRESSION", CSharpLanguage.INSTANCE, CSharpReferenceExpressionImpl.class);

	IElementType METHOD_CALL_EXPRESSION = new CompositeElementTypeAsPsiFactory("METHOD_CALL_EXPRESSION", CSharpLanguage.INSTANCE, CSharpMethodCallExpressionImpl.class);

	IElementType CONSTRUCTOR_SUPER_CALL_EXPRESSION = new CompositeElementTypeAsPsiFactory("CONSTRUCTOR_SUPER_CALL_EXPRESSION", CSharpLanguage.INSTANCE, CSharpConstructorSuperCallImpl.class);

	IElementType CHECKED_EXPRESSION = new CompositeElementTypeAsPsiFactory("CHECKED_EXPRESSION", CSharpLanguage.INSTANCE, CSharpCheckedExpressionImpl.class);

	IElementType TYPE_OF_EXPRESSION = new CompositeElementTypeAsPsiFactory("TYPE_OF_EXPRESSION", CSharpLanguage.INSTANCE, CSharpTypeOfExpressionImpl.class);

	IElementType NAMEOF_EXPRESSION = new CompositeElementTypeAsPsiFactory("NAMEOF_EXPRESSION", CSharpLanguage.INSTANCE, CSharpNameOfExpressionImpl.class);

	IElementType AWAIT_EXPRESSION = new CompositeElementTypeAsPsiFactory("AWAIT_EXPRESSION", CSharpLanguage.INSTANCE, CSharpAwaitExpressionImpl.class);

	IElementType SIZE_OF_EXPRESSION = new CompositeElementTypeAsPsiFactory("SIZE_OF_EXPRESSION", CSharpLanguage.INSTANCE, CSharpSizeOfExpressionImpl.class);

	IElementType DEFAULT_EXPRESSION = new CompositeElementTypeAsPsiFactory("DEFAULT_EXPRESSION", CSharpLanguage.INSTANCE, CSharpDefaultExpressionImpl.class);

	IElementType BINARY_EXPRESSION = new CompositeElementTypeAsPsiFactory("BINARY_EXPRESSION", CSharpLanguage.INSTANCE, CSharpBinaryExpressionImpl.class);

	IElementType OPERATOR_REFERENCE = new CompositeElementTypeAsPsiFactory("OPERATOR_REFERENCE", CSharpLanguage.INSTANCE, CSharpOperatorReferenceImpl.class);

	IElementType IS_EXPRESSION = new CompositeElementTypeAsPsiFactory("IS_EXPRESSION", CSharpLanguage.INSTANCE, CSharpIsExpressionImpl.class);

	IElementType IS_VARIABLE = new CompositeElementTypeAsPsiFactory("IS_VARIABLE", CSharpLanguage.INSTANCE, CSharpIsVariableImpl.class);

	IElementType CASE_PATTERN_STATEMENT = new CompositeElementTypeAsPsiFactory("CASE_PATTERN_STATEMENT", CSharpLanguage.INSTANCE, CSharpCasePatternStatementImpl.class);

	IElementType CASE_VARIABLE = new CompositeElementTypeAsPsiFactory("CASE_VARIABLE", CSharpLanguage.INSTANCE, CSharpCaseVariableImpl.class);

	IElementType AS_EXPRESSION = new CompositeElementTypeAsPsiFactory("AS_EXPRESSION", CSharpLanguage.INSTANCE, CSharpAsExpressionImpl.class);

	IElementType NEW_ARRAY_LENGTH = new CompositeElementTypeAsPsiFactory("NEW_ARRAY_LENGTH", CSharpLanguage.INSTANCE, CSharpNewArrayLengthImpl.class);

	IElementType NEW_EXPRESSION = new CompositeElementTypeAsPsiFactory("NEW_EXPRESSION", CSharpLanguage.INSTANCE, CSharpNewExpressionImpl.class);

	IElementType SHORT_OBJECT_INITIALIZER_EXPRESSION = new CompositeElementTypeAsPsiFactory("SHORT_OBJECT_INITIALIZER_EXPRESSION", CSharpLanguage.INSTANCE, CSharpShortObjectInitializerExpressionImpl.class);

	IElementType IDENTIFIER = new CompositeElementTypeAsPsiFactory("IDENTIFIER", CSharpLanguage.INSTANCE, CSharpIdentifierImpl.class);

	IElementType __ARGLIST_EXPRESSION = new CompositeElementTypeAsPsiFactory("__ARGLIST_EXPRESSION", CSharpLanguage.INSTANCE, CSharpArglistExpressionImpl.class);

	IElementType __MAKEREF_EXPRESSION = new CompositeElementTypeAsPsiFactory("__MAKEREF_EXPRESSION", CSharpLanguage.INSTANCE, CSharpMakeRefExpressionImpl.class);

	IElementType __REFTYPE_EXPRESSION = new CompositeElementTypeAsPsiFactory("__REFTYPE_EXPRESSION", CSharpLanguage.INSTANCE, CSharpRefTypeExpressionImpl.class);

	IElementType __REFVALUE_EXPRESSION = new CompositeElementTypeAsPsiFactory("__REFVALUE_EXPRESSION", CSharpLanguage.INSTANCE, CSharpRefValueExpressionImpl.class);

	IElementType STACKALLOC_EXPRESSION = new CompositeElementTypeAsPsiFactory("STACKALLOC_EXPRESSION", CSharpLanguage.INSTANCE, CSharpStackAllocExpressionImpl.class);

	IElementType OUT_REF_WRAP_EXPRESSION = new CompositeElementTypeAsPsiFactory("OUT_REF_WRAP_EXPRESSION", CSharpLanguage.INSTANCE, CSharpOutRefWrapExpressionImpl.class);

	IElementType OUT_REF_VARIABLE_EXPRESSION = new CompositeElementTypeAsPsiFactory("OUT_REF_VARIABLE_EXPRESSION", CSharpLanguage.INSTANCE, CSharpOutRefVariableExpressionImpl.class);

	IElementType OUT_REF_VARIABLE = new CompositeElementTypeAsPsiFactory("OUT_REF_VARIABLE", CSharpLanguage.INSTANCE, CSharpOutRefVariableImpl.class);

	IElementType CONDITIONAL_EXPRESSION = new CompositeElementTypeAsPsiFactory("CONDITIONAL_EXPRESSION", CSharpLanguage.INSTANCE, CSharpConditionalExpressionImpl.class);

	IElementType NULL_COALESCING_EXPRESSION = new CompositeElementTypeAsPsiFactory("NULL_COALESCING_EXPRESSION", CSharpLanguage.INSTANCE, CSharpNullCoalescingExpressionImpl.class);

	IElementType ASSIGNMENT_EXPRESSION = new CompositeElementTypeAsPsiFactory("ASSIGNMENT_EXPRESSION", CSharpLanguage.INSTANCE, CSharpAssignmentExpressionImpl.class);

	IElementType TYPE_CAST_EXPRESSION = new CompositeElementTypeAsPsiFactory("TYPE_CAST_EXPRESSION", CSharpLanguage.INSTANCE, CSharpTypeCastExpressionImpl.class);

	IElementType IF_STATEMENT = new CompositeElementTypeAsPsiFactory("IF_STATEMENT", CSharpLanguage.INSTANCE, CSharpIfStatementImpl.class);

	IElementType BLOCK_STATEMENT = new CompositeElementTypeAsPsiFactory("BLOCK_STATEMENT", CSharpLanguage.INSTANCE, CSharpBlockStatementImpl.class);

	IElementType INDEX_ACCESS_EXPRESSION = new CompositeElementTypeAsPsiFactory("ARRAY_ACCESS_EXPRESSION", CSharpLanguage.INSTANCE, CSharpIndexAccessExpressionImpl.class);

	IElementType POSTFIX_EXPRESSION = new CompositeElementTypeAsPsiFactory("POSTFIX_EXPRESSION", CSharpLanguage.INSTANCE, CSharpPostfixExpressionImpl.class);

	IElementType PREFIX_EXPRESSION = new CompositeElementTypeAsPsiFactory("PREFIX_EXPRESSION", CSharpLanguage.INSTANCE, CSharpPrefixExpressionImpl.class);

	IElementType ERROR_EXPRESSION = new CompositeElementTypeAsPsiFactory("ERROR_EXPRESSION", CSharpLanguage.INSTANCE, CSharpErrorExpressionImpl.class);

	IElementType PARENTHESES_EXPRESSION = new CompositeElementTypeAsPsiFactory("PARENTHESES_EXPRESSION", CSharpLanguage.INSTANCE, CSharpParenthesesExpressionImpl.class);

	IElementType LINQ_EXPRESSION = new CompositeElementTypeAsPsiFactory("LINQ_EXPRESSION", CSharpLanguage.INSTANCE, CSharpLinqExpressionImpl.class);

	IElementType LINQ_VARIABLE = new CompositeElementTypeAsPsiFactory("LINQ_VARIABLE", CSharpLanguage.INSTANCE, CSharpLinqVariableImpl.class);

	IElementType LINQ_QUERY_BODY = new CompositeElementTypeAsPsiFactory("LINQ_QUERY_BODY", CSharpLanguage.INSTANCE, CSharpLinqQueryBodyImpl.class);

	IElementType LINQ_FROM_CLAUSE = new CompositeElementTypeAsPsiFactory("LINQ_FROM_CLAUSE", CSharpLanguage.INSTANCE, CSharpLinqFromClauseImpl.class);

	IElementType LINQ_WHERE_CLAUSE = new CompositeElementTypeAsPsiFactory("LINQ_WHERE_CLAUSE", CSharpLanguage.INSTANCE, CSharpLinqWhereClauseImpl.class);

	IElementType LINQ_ORDERBY_CLAUSE = new CompositeElementTypeAsPsiFactory("LINQ_ORDERBY_CLAUSE", CSharpLanguage.INSTANCE, CSharpLinqOrderByClauseImpl.class);

	IElementType LINQ_LET_CLAUSE = new CompositeElementTypeAsPsiFactory("LINQ_LET_CLAUSE", CSharpLanguage.INSTANCE, CSharpLinqLetClauseImpl.class);

	IElementType LINQ_JOIN_CLAUSE = new CompositeElementTypeAsPsiFactory("LINQ_JOIN_CLAUSE", CSharpLanguage.INSTANCE, CSharpLinqJoinClauseImpl.class);

	IElementType LINQ_INTRO_CLAUSE = new CompositeElementTypeAsPsiFactory("LINQ_INTRO_CLAUSE", CSharpLanguage.INSTANCE, CSharpLinqIntoClauseImpl.class);

	IElementType LINQ_QUERY_CONTINUATION = new CompositeElementTypeAsPsiFactory("LINQ_QUERY_CONTINUATION", CSharpLanguage.INSTANCE, CSharpLinqQueryContinuationImpl.class);

	IElementType LINQ_ORDERBY_ORDERING = new CompositeElementTypeAsPsiFactory("LINQ_ORDERBY_ORDERING", CSharpLanguage.INSTANCE, CSharpLinqOrderByOrderingImpl.class);

	IElementType LINQ_SELECT_OR_GROUP_CLAUSE = new CompositeElementTypeAsPsiFactory("LINQ_SELECT_OR_GROUP_CLAUSE", CSharpLanguage.INSTANCE, CSharpLinqSelectOrGroupClauseImpl.class);

	IElementType LAMBDA_EXPRESSION = new CompositeElementTypeAsPsiFactory("LAMBDA_EXPRESSION", CSharpLanguage.INSTANCE, CSharpLambdaExpressionImpl.class);

	IElementType DELEGATE_EXPRESSION = new CompositeElementTypeAsPsiFactory("DELEGATE_EXPRESSION", CSharpLanguage.INSTANCE, CSharpDelegateExpressionImpl.class);

	IElementType LAMBDA_PARAMETER = new CompositeElementTypeAsPsiFactory("LAMBDA_PARAMETER", CSharpLanguage.INSTANCE, CSharpLambdaParameterImpl.class);

	IElementType LAMBDA_PARAMETER_LIST = new CompositeElementTypeAsPsiFactory("LAMBDA_PARAMETER_LIST", CSharpLanguage.INSTANCE, CSharpLambdaParameterListImpl.class);

	IElementType ANONYM_FIELD_OR_PROPERTY_SET = new CompositeElementTypeAsPsiFactory("ANONYM_FIELD_OR_PROPERTY_SET", CSharpLanguage.INSTANCE, CSharpAnonymFieldOrPropertySetImpl.class);

	IElementType NAMED_FIELD_OR_PROPERTY_SET = new CompositeElementTypeAsPsiFactory("NAMED_FIELD_OR_PROPERTY_SET", CSharpLanguage.INSTANCE, CSharpNamedFieldOrPropertySetImpl.class);

	IElementType FIELD_OR_PROPERTY_SET_BLOCK = new CompositeElementTypeAsPsiFactory("FIELD_OR_PROPERTY_SET_BLOCK", CSharpLanguage.INSTANCE, CSharpFieldOrPropertySetBlockImpl.class);

	IElementType ARRAY_INITIALIZER = new CompositeElementTypeAsPsiFactory("ARRAY_INITIALIZER", CSharpLanguage.INSTANCE, CSharpArrayInitializerImpl.class);

	IElementType ARRAY_INITIALIZER_SINGLE_VALUE = new CompositeElementTypeAsPsiFactory("ARRAY_INITIALIZER_SINGLE_VALUE", CSharpLanguage.INSTANCE, CSharpArrayInitializerSingleValueImpl.class);

	IElementType ARRAY_INITIALIZER_COMPOSITE_VALUE = new CompositeElementTypeAsPsiFactory("ARRAY_INITIALIZER_COMPOSITE_VALUE", CSharpLanguage.INSTANCE, CSharpArrayInitializerCompositeValueImpl.class);

	IElementType IMPLICIT_ARRAY_INITIALIZATION_EXPRESSION = new CompositeElementTypeAsPsiFactory("IMPLICIT_ARRAY_INITIALIZATION_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpImplicitArrayInitializationExpressionImpl.class);

	IElementType DICTIONARY_INITIALIZER_LIST = new CompositeElementTypeAsPsiFactory("DICTIONARY_INITIALIZER_LIST", CSharpLanguage.INSTANCE, CSharpDictionaryInitializerListImpl.class);

	IElementType DICTIONARY_INITIALIZER = new CompositeElementTypeAsPsiFactory("DICTIONARY_INITIALIZER", CSharpLanguage.INSTANCE, CSharpDictionaryInitializerImpl.class);

	IElementType CALL_ARGUMENT_LIST = new CompositeElementTypeAsPsiFactory("CALL_ARGUMENT_LIST", CSharpLanguage.INSTANCE, CSharpCallArgumentListImpl.class);

	IElementType CALL_ARGUMENT = new CompositeElementTypeAsPsiFactory("CALL_ARGUMENT", CSharpLanguage.INSTANCE, CSharpCallArgumentImpl.class);

	IElementType DOC_CALL_ARGUMENT = new CompositeElementTypeAsPsiFactory("DOC_CALL_ARGUMENT", CSharpLanguage.INSTANCE, CSharpDocCallArgumentImpl.class);

	IElementType NAMED_CALL_ARGUMENT = new CompositeElementTypeAsPsiFactory("NAMED_CALL_ARGUMENT", CSharpLanguage.INSTANCE, CSharpNamedCallArgumentImpl.class);

	IElementType LOCAL_VARIABLE_DECLARATION_STATEMENT = new CompositeElementTypeAsPsiFactory("LOCAL_VARIABLE_DECLARATION_STATEMENT", CSharpLanguage.INSTANCE, CSharpLocalVariableDeclarationStatementImpl
			.class);

	IElementType EXPRESSION_STATEMENT = new CompositeElementTypeAsPsiFactory("EXPRESSION_STATEMENT", CSharpLanguage.INSTANCE, CSharpExpressionStatementImpl.class);

	IElementType USING_STATEMENT = new CompositeElementTypeAsPsiFactory("USING_STATEMENT", CSharpLanguage.INSTANCE, CSharpUsingStatementImpl.class);

	IElementType LABELED_STATEMENT = new CompositeElementTypeAsPsiFactory("LABELED_STATEMENT", CSharpLanguage.INSTANCE, CSharpLabeledStatementImpl.class);

	IElementType GOTO_STATEMENT = new CompositeElementTypeAsPsiFactory("GOTO_STATEMENT", CSharpLanguage.INSTANCE, CSharpGotoStatementImpl.class);

	IElementType FIXED_STATEMENT = new CompositeElementTypeAsPsiFactory("FIXED_STATEMENT", CSharpLanguage.INSTANCE, CSharpFixedStatementImpl.class);

	IElementType LOCK_STATEMENT = new CompositeElementTypeAsPsiFactory("LOCK_STATEMENT", CSharpLanguage.INSTANCE, CSharpLockStatementImpl.class);

	IElementType EMPTY_STATEMENT = new CompositeElementTypeAsPsiFactory("EMPTY_STATEMENT", CSharpLanguage.INSTANCE, CSharpEmptyStatementImpl.class);

	IElementType FOREACH_STATEMENT = new CompositeElementTypeAsPsiFactory("FOREACH_STATEMENT", CSharpLanguage.INSTANCE, CSharpForeachStatementImpl.class);

	IElementType FOR_STATEMENT = new CompositeElementTypeAsPsiFactory("FOR_STATEMENT", CSharpLanguage.INSTANCE, CSharpForStatementImpl.class);

	IElementType TRY_STATEMENT = new CompositeElementTypeAsPsiFactory("TRY_STATEMENT", CSharpLanguage.INSTANCE, CSharpTryStatementImpl.class);

	IElementType CATCH_STATEMENT = new CompositeElementTypeAsPsiFactory("CATCH_STATEMENT", CSharpLanguage.INSTANCE, CSharpCatchStatementImpl.class);

	IElementType FINALLY_STATEMENT = new CompositeElementTypeAsPsiFactory("FINALLY_STATEMENT", CSharpLanguage.INSTANCE, CSharpFinallyStatementImpl.class);

	IElementType SWITCH_STATEMENT = new CompositeElementTypeAsPsiFactory("SWITCH_STATEMENT", CSharpLanguage.INSTANCE, CSharpSwitchStatementImpl.class);

	IElementType UNSAFE_STATEMENT = new CompositeElementTypeAsPsiFactory("UNSAFE_STATEMENT", CSharpLanguage.INSTANCE, CSharpUnsafeStatementImpl.class);

	IElementType CASE_OR_DEFAULT_STATEMENT = new CompositeElementTypeAsPsiFactory("CASE_OR_DEFAULT_STATEMENT", CSharpLanguage.INSTANCE, CSharpCaseOrDefaultStatementImpl.class);

	IElementType THROW_STATEMENT = new CompositeElementTypeAsPsiFactory("THROW_STATEMENT_STATEMENT", CSharpLanguage.INSTANCE, CSharpThrowStatementImpl.class);

	IElementType RETURN_STATEMENT = new CompositeElementTypeAsPsiFactory("RETURN_STATEMENT", CSharpLanguage.INSTANCE, CSharpReturnStatementImpl.class);

	IElementType CHECKED_STATEMENT = new CompositeElementTypeAsPsiFactory("CHECKED_STATEMENT", CSharpLanguage.INSTANCE, CSharpCheckedStatementImpl.class);

	IElementType YIELD_STATEMENT = new CompositeElementTypeAsPsiFactory("YIELD_STATEMENT", CSharpLanguage.INSTANCE, CSharpYieldStatementImpl.class);

	IElementType WHILE_STATEMENT = new CompositeElementTypeAsPsiFactory("WHILE_STATEMENT", CSharpLanguage.INSTANCE, CSharpWhileStatementImpl.class);

	IElementType DO_WHILE_STATEMENT = new CompositeElementTypeAsPsiFactory("DO_WHILE_STATEMENT", CSharpLanguage.INSTANCE, CSharpDoWhileStatementImpl.class);

	IElementType BREAK_STATEMENT = new CompositeElementTypeAsPsiFactory("BREAK_STATEMENT", CSharpLanguage.INSTANCE, CSharpBreakStatementImpl.class);

	IElementType CONTINUE_STATEMENT = new CompositeElementTypeAsPsiFactory("CONTINUE_STATEMENT", CSharpLanguage.INSTANCE, CSharpContinueStatementImpl.class);

	IElementType ATTRIBUTE_LIST = new CompositeElementTypeAsPsiFactory("ATTRIBUTE_LIST", CSharpLanguage.INSTANCE, CSharpAttributeListImpl.class);

	IElementType ATTRIBUTE = new CompositeElementTypeAsPsiFactory("ATTRIBUTE", CSharpLanguage.INSTANCE, CSharpAttributeImpl.class);

	IElementType STATEMENT_METHOD_BODY = new BaseMethodBodyLazyElementType("CSHARP_STATEMENT_METHOD_BODY")
	{
		@Override
		protected void parse(@Nonnull CSharpBuilderWrapper wrapper, @Nonnull ModifierSet set)
		{
			StatementParsing.parse(wrapper, set);
		}
	};

	IElementType EMPTY_METHOD_BODY = new BaseMethodBodyLazyElementType("CSHARP_EMPTY_METHOD_BODY")
	{
		@Override
		protected void parse(@Nonnull CSharpBuilderWrapper wrapper, @Nonnull ModifierSet set)
		{
			SharedParsingHelpers.expect(wrapper, CSharpTokens.SEMICOLON, "';' expected");
		}
	};

	IElementType EXPRESSION_METHOD_BODY = new BaseMethodBodyLazyElementType("CSHARP_EXPRESSION_METHOD_BODY")
	{
		@Override
		protected void parse(@Nonnull CSharpBuilderWrapper wrapper, @Nonnull ModifierSet set)
		{
			// skip =>
			wrapper.advanceLexer();

			ExpressionParsing.parse(wrapper, set);

			SharedParsingHelpers.expect(wrapper, CSharpTokens.SEMICOLON, "';' expected");
		}
	};

	TokenSet METHOD_BODIES = TokenSet.create(STATEMENT_METHOD_BODY, EMPTY_METHOD_BODY, EXPRESSION_METHOD_BODY);
}
