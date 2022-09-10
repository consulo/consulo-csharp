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

package consulo.csharp.lang.impl.psi;

import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.parser.CSharpBuilderWrapper;
import consulo.csharp.lang.impl.parser.ModifierSet;
import consulo.csharp.lang.impl.parser.SharedParsingHelpers;
import consulo.csharp.lang.impl.parser.exp.ExpressionParsing;
import consulo.csharp.lang.impl.parser.stmt.StatementParsing;
import consulo.csharp.lang.impl.psi.elementType.BaseMethodBodyLazyElementType;
import consulo.csharp.lang.impl.psi.source.*;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;

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

	IElementType PARAMETER_LIST = new CompositeElementTypeAsPsiFactory("PARAMETER_LIST", CSharpLanguage.INSTANCE, CSharpParameterListImpl::new);

	IElementType PARAMETER = new CompositeElementTypeAsPsiFactory("PARAMETER", CSharpLanguage.INSTANCE, CSharpParameterImpl::new);

	IElementType TYPE_DECLARATION = CSharpStubElements.TYPE_DECLARATION;

	IElementType DUMMY_DECLARATION = CSharpStubElements.DUMMY_DECLARATION;

	IElementType EVENT_DECLARATION = CSharpStubElements.EVENT_DECLARATION;

	IElementType CONVERSION_METHOD_DECLARATION = CSharpStubElements.CONVERSION_METHOD_DECLARATION;

	IElementType XACCESSOR = CSharpStubElements.XACCESSOR;

	IElementType FIELD_DECLARATION = CSharpStubElements.FIELD_DECLARATION;

	IElementType ENUM_CONSTANT_DECLARATION = CSharpStubElements.ENUM_CONSTANT_DECLARATION;

	IElementType LOCAL_VARIABLE = new CompositeElementTypeAsPsiFactory("LOCAL_VARIABLE", CSharpLanguage.INSTANCE, CSharpLocalVariableImpl::new);

	IElementType LOCAL_METHOD = new CompositeElementTypeAsPsiFactory("LOCAL_METHOD", CSharpLanguage.INSTANCE, CSharpLocalMethodDeclarationImpl::new);

	IElementType LOCAL_METHOD_STATEMENT = new CompositeElementTypeAsPsiFactory("LOCAL_METHOD_STATEMENT", CSharpLanguage.INSTANCE, CSharpLocalMethodDeclarationStatementImpl::new);

	IElementType PROPERTY_DECLARATION = CSharpStubElements.PROPERTY_DECLARATION;

	IElementType INDEX_METHOD_DECLARATION = CSharpStubElements.INDEX_METHOD_DECLARATION;

	IElementType GENERIC_PARAMETER_LIST = CSharpStubElements.GENERIC_PARAMETER_LIST;

	IElementType GENERIC_PARAMETER = CSharpStubElements.GENERIC_PARAMETER;

	IElementType GENERIC_CONSTRAINT_LIST = CSharpStubElements.GENERIC_CONSTRAINT_LIST;

	IElementType GENERIC_CONSTRAINT = CSharpStubElements.GENERIC_CONSTRAINT;

	IElementType GENERIC_CONSTRAINT_KEYWORD_VALUE = CSharpStubElements.GENERIC_CONSTRAINT_KEYWORD_VALUE;

	IElementType GENERIC_CONSTRAINT_TYPE_VALUE = CSharpStubElements.GENERIC_CONSTRAINT_TYPE_VALUE;

	IElementType USER_TYPE = new CompositeElementTypeAsPsiFactory("USER_TYPE", CSharpLanguage.INSTANCE, CSharpUserTypeImpl::new);

	IElementType POINTER_TYPE = new CompositeElementTypeAsPsiFactory("POINTER_TYPE", CSharpLanguage.INSTANCE, CSharpPointerTypeImpl::new);

	IElementType TUPLE_TYPE = new CompositeElementTypeAsPsiFactory("TUPLE_TYPE", CSharpLanguage.INSTANCE, CSharpTupleTypeImpl::new);

	IElementType TUPLE_VARIABLE = new CompositeElementTypeAsPsiFactory("TUPLE_VARIABLE", CSharpLanguage.INSTANCE, CSharpTupleVariableImpl::new);

	IElementType TUPLE_ELEMENT = new CompositeElementTypeAsPsiFactory("TUPLE_ELEMENT", CSharpLanguage.INSTANCE, CSharpTupleElementImpl::new);

	IElementType TUPLE_EXPRESSION = new CompositeElementTypeAsPsiFactory("TUPLE_EXPRESSION", CSharpLanguage.INSTANCE, CSharpTupleExpressionImpl::new);

	IElementType NULLABLE_TYPE = new CompositeElementTypeAsPsiFactory("NULLABLE_TYPE", CSharpLanguage.INSTANCE, CSharpNullableTypeImpl::new);

	IElementType NATIVE_TYPE = new CompositeElementTypeAsPsiFactory("NATIVE_TYPE", CSharpLanguage.INSTANCE, CSharpNativeTypeImpl::new);

	IElementType ARRAY_TYPE = new CompositeElementTypeAsPsiFactory("ARRAY_TYPE", CSharpLanguage.INSTANCE, CSharpArrayTypeImpl::new);

	IElementType MODIFIER_LIST = new CompositeElementTypeAsPsiFactory("MODIFIER_LIST", CSharpLanguage.INSTANCE, CSharpModifierListImpl::new);

	IElementType EXTENDS_LIST = CSharpStubElements.EXTENDS_LIST;

	IElementType TYPE_ARGUMENTS = new CompositeElementTypeAsPsiFactory("TYPE_ARGUMENTS", CSharpLanguage.INSTANCE, CSharpTypeListImpl::new);

	IElementType EMPTY_TYPE_ARGUMENTS = new CompositeElementTypeAsPsiFactory("EMPTY_TYPE_ARGUMENTS", CSharpLanguage.INSTANCE, CSharpEmptyTypeListImpl::new);

	IElementType CONSTANT_EXPRESSION = new CompositeElementTypeAsPsiFactory("CONSTANT_EXPRESSION", CSharpLanguage.INSTANCE, CSharpConstantExpressionImpl::new);

	IElementType REFERENCE_EXPRESSION = new CompositeElementTypeAsPsiFactory("REFERENCE_EXPRESSION", CSharpLanguage.INSTANCE, CSharpReferenceExpressionImpl::new);

	IElementType METHOD_CALL_EXPRESSION = new CompositeElementTypeAsPsiFactory("METHOD_CALL_EXPRESSION", CSharpLanguage.INSTANCE, CSharpMethodCallExpressionImpl::new);

	IElementType CONSTRUCTOR_SUPER_CALL_EXPRESSION = new CompositeElementTypeAsPsiFactory("CONSTRUCTOR_SUPER_CALL_EXPRESSION", CSharpLanguage.INSTANCE, CSharpConstructorSuperCallImpl::new);

	IElementType CHECKED_EXPRESSION = new CompositeElementTypeAsPsiFactory("CHECKED_EXPRESSION", CSharpLanguage.INSTANCE, CSharpCheckedExpressionImpl::new);

	IElementType TYPE_OF_EXPRESSION = new CompositeElementTypeAsPsiFactory("TYPE_OF_EXPRESSION", CSharpLanguage.INSTANCE, CSharpTypeOfExpressionImpl::new);

	IElementType NAMEOF_EXPRESSION = new CompositeElementTypeAsPsiFactory("NAMEOF_EXPRESSION", CSharpLanguage.INSTANCE, CSharpNameOfExpressionImpl::new);

	IElementType AWAIT_EXPRESSION = new CompositeElementTypeAsPsiFactory("AWAIT_EXPRESSION", CSharpLanguage.INSTANCE, CSharpAwaitExpressionImpl::new);

	IElementType SIZE_OF_EXPRESSION = new CompositeElementTypeAsPsiFactory("SIZE_OF_EXPRESSION", CSharpLanguage.INSTANCE, CSharpSizeOfExpressionImpl::new);

	IElementType DEFAULT_EXPRESSION = new CompositeElementTypeAsPsiFactory("DEFAULT_EXPRESSION", CSharpLanguage.INSTANCE, CSharpDefaultExpressionImpl::new);

	IElementType BINARY_EXPRESSION = new CompositeElementTypeAsPsiFactory("BINARY_EXPRESSION", CSharpLanguage.INSTANCE, CSharpBinaryExpressionImpl::new);

	IElementType OPERATOR_REFERENCE = new CompositeElementTypeAsPsiFactory("OPERATOR_REFERENCE", CSharpLanguage.INSTANCE, CSharpOperatorReferenceImpl::new);

	IElementType IS_EXPRESSION = new CompositeElementTypeAsPsiFactory("IS_EXPRESSION", CSharpLanguage.INSTANCE, CSharpIsExpressionImpl::new);

	IElementType IS_VARIABLE = new CompositeElementTypeAsPsiFactory("IS_VARIABLE", CSharpLanguage.INSTANCE, CSharpIsVariableImpl::new);

	IElementType CASE_PATTERN_STATEMENT = new CompositeElementTypeAsPsiFactory("CASE_PATTERN_STATEMENT", CSharpLanguage.INSTANCE, CSharpCasePatternStatementImpl::new);

	IElementType CASE_VARIABLE = new CompositeElementTypeAsPsiFactory("CASE_VARIABLE", CSharpLanguage.INSTANCE, CSharpCaseVariableImpl::new);

	IElementType AS_EXPRESSION = new CompositeElementTypeAsPsiFactory("AS_EXPRESSION", CSharpLanguage.INSTANCE, CSharpAsExpressionImpl::new);

	IElementType NEW_ARRAY_LENGTH = new CompositeElementTypeAsPsiFactory("NEW_ARRAY_LENGTH", CSharpLanguage.INSTANCE, CSharpNewArrayLengthImpl::new);

	IElementType NEW_EXPRESSION = new CompositeElementTypeAsPsiFactory("NEW_EXPRESSION", CSharpLanguage.INSTANCE, CSharpNewExpressionImpl::new);

	IElementType SHORT_OBJECT_INITIALIZER_EXPRESSION = new CompositeElementTypeAsPsiFactory("SHORT_OBJECT_INITIALIZER_EXPRESSION", CSharpLanguage.INSTANCE, CSharpShortObjectInitializerExpressionImpl::new);

	IElementType IDENTIFIER = new CompositeElementTypeAsPsiFactory("IDENTIFIER", CSharpLanguage.INSTANCE, CSharpIdentifierImpl::new);

	IElementType __ARGLIST_EXPRESSION = new CompositeElementTypeAsPsiFactory("__ARGLIST_EXPRESSION", CSharpLanguage.INSTANCE, CSharpArglistExpressionImpl::new);

	IElementType __MAKEREF_EXPRESSION = new CompositeElementTypeAsPsiFactory("__MAKEREF_EXPRESSION", CSharpLanguage.INSTANCE, CSharpMakeRefExpressionImpl::new);

	IElementType __REFTYPE_EXPRESSION = new CompositeElementTypeAsPsiFactory("__REFTYPE_EXPRESSION", CSharpLanguage.INSTANCE, CSharpRefTypeExpressionImpl::new);

	IElementType __REFVALUE_EXPRESSION = new CompositeElementTypeAsPsiFactory("__REFVALUE_EXPRESSION", CSharpLanguage.INSTANCE, CSharpRefValueExpressionImpl::new);

	IElementType STACKALLOC_EXPRESSION = new CompositeElementTypeAsPsiFactory("STACKALLOC_EXPRESSION", CSharpLanguage.INSTANCE, CSharpStackAllocExpressionImpl::new);

	IElementType OUT_REF_WRAP_EXPRESSION = new CompositeElementTypeAsPsiFactory("OUT_REF_WRAP_EXPRESSION", CSharpLanguage.INSTANCE, CSharpOutRefWrapExpressionImpl::new);

	IElementType OUT_REF_VARIABLE_EXPRESSION = new CompositeElementTypeAsPsiFactory("OUT_REF_VARIABLE_EXPRESSION", CSharpLanguage.INSTANCE, CSharpOutRefVariableExpressionImpl::new);

	IElementType OUT_REF_VARIABLE = new CompositeElementTypeAsPsiFactory("OUT_REF_VARIABLE", CSharpLanguage.INSTANCE, CSharpOutRefVariableImpl::new);

	IElementType CONDITIONAL_EXPRESSION = new CompositeElementTypeAsPsiFactory("CONDITIONAL_EXPRESSION", CSharpLanguage.INSTANCE, CSharpConditionalExpressionImpl::new);

	IElementType NULL_COALESCING_EXPRESSION = new CompositeElementTypeAsPsiFactory("NULL_COALESCING_EXPRESSION", CSharpLanguage.INSTANCE, CSharpNullCoalescingExpressionImpl::new);

	IElementType ASSIGNMENT_EXPRESSION = new CompositeElementTypeAsPsiFactory("ASSIGNMENT_EXPRESSION", CSharpLanguage.INSTANCE, CSharpAssignmentExpressionImpl::new);

	IElementType TYPE_CAST_EXPRESSION = new CompositeElementTypeAsPsiFactory("TYPE_CAST_EXPRESSION", CSharpLanguage.INSTANCE, CSharpTypeCastExpressionImpl::new);

	IElementType IF_STATEMENT = new CompositeElementTypeAsPsiFactory("IF_STATEMENT", CSharpLanguage.INSTANCE, CSharpIfStatementImpl::new);

	IElementType BLOCK_STATEMENT = new CompositeElementTypeAsPsiFactory("BLOCK_STATEMENT", CSharpLanguage.INSTANCE, CSharpBlockStatementImpl::new);

	IElementType INDEX_ACCESS_EXPRESSION = new CompositeElementTypeAsPsiFactory("ARRAY_ACCESS_EXPRESSION", CSharpLanguage.INSTANCE, CSharpIndexAccessExpressionImpl::new);

	IElementType POSTFIX_EXPRESSION = new CompositeElementTypeAsPsiFactory("POSTFIX_EXPRESSION", CSharpLanguage.INSTANCE, CSharpPostfixExpressionImpl::new);

	IElementType PREFIX_EXPRESSION = new CompositeElementTypeAsPsiFactory("PREFIX_EXPRESSION", CSharpLanguage.INSTANCE, CSharpPrefixExpressionImpl::new);

	IElementType ERROR_EXPRESSION = new CompositeElementTypeAsPsiFactory("ERROR_EXPRESSION", CSharpLanguage.INSTANCE, CSharpErrorExpressionImpl::new);

	IElementType PARENTHESES_EXPRESSION = new CompositeElementTypeAsPsiFactory("PARENTHESES_EXPRESSION", CSharpLanguage.INSTANCE, CSharpParenthesesExpressionImpl::new);

	IElementType LINQ_EXPRESSION = new CompositeElementTypeAsPsiFactory("LINQ_EXPRESSION", CSharpLanguage.INSTANCE, CSharpLinqExpressionImpl::new);

	IElementType LINQ_VARIABLE = new CompositeElementTypeAsPsiFactory("LINQ_VARIABLE", CSharpLanguage.INSTANCE, CSharpLinqVariableImpl::new);

	IElementType LINQ_QUERY_BODY = new CompositeElementTypeAsPsiFactory("LINQ_QUERY_BODY", CSharpLanguage.INSTANCE, CSharpLinqQueryBodyImpl::new);

	IElementType LINQ_FROM_CLAUSE = new CompositeElementTypeAsPsiFactory("LINQ_FROM_CLAUSE", CSharpLanguage.INSTANCE, CSharpLinqFromClauseImpl::new);

	IElementType LINQ_WHERE_CLAUSE = new CompositeElementTypeAsPsiFactory("LINQ_WHERE_CLAUSE", CSharpLanguage.INSTANCE, CSharpLinqWhereClauseImpl::new);

	IElementType LINQ_ORDERBY_CLAUSE = new CompositeElementTypeAsPsiFactory("LINQ_ORDERBY_CLAUSE", CSharpLanguage.INSTANCE, CSharpLinqOrderByClauseImpl::new);

	IElementType LINQ_LET_CLAUSE = new CompositeElementTypeAsPsiFactory("LINQ_LET_CLAUSE", CSharpLanguage.INSTANCE, CSharpLinqLetClauseImpl::new);

	IElementType LINQ_JOIN_CLAUSE = new CompositeElementTypeAsPsiFactory("LINQ_JOIN_CLAUSE", CSharpLanguage.INSTANCE, CSharpLinqJoinClauseImpl::new);

	IElementType LINQ_INTRO_CLAUSE = new CompositeElementTypeAsPsiFactory("LINQ_INTRO_CLAUSE", CSharpLanguage.INSTANCE, CSharpLinqIntoClauseImpl::new);

	IElementType LINQ_QUERY_CONTINUATION = new CompositeElementTypeAsPsiFactory("LINQ_QUERY_CONTINUATION", CSharpLanguage.INSTANCE, CSharpLinqQueryContinuationImpl::new);

	IElementType LINQ_ORDERBY_ORDERING = new CompositeElementTypeAsPsiFactory("LINQ_ORDERBY_ORDERING", CSharpLanguage.INSTANCE, CSharpLinqOrderByOrderingImpl::new);

	IElementType LINQ_SELECT_OR_GROUP_CLAUSE = new CompositeElementTypeAsPsiFactory("LINQ_SELECT_OR_GROUP_CLAUSE", CSharpLanguage.INSTANCE, CSharpLinqSelectOrGroupClauseImpl::new);

	IElementType LAMBDA_EXPRESSION = new CompositeElementTypeAsPsiFactory("LAMBDA_EXPRESSION", CSharpLanguage.INSTANCE, CSharpLambdaExpressionImpl::new);

	IElementType DELEGATE_EXPRESSION = new CompositeElementTypeAsPsiFactory("DELEGATE_EXPRESSION", CSharpLanguage.INSTANCE, CSharpDelegateExpressionImpl::new);

	IElementType LAMBDA_PARAMETER = new CompositeElementTypeAsPsiFactory("LAMBDA_PARAMETER", CSharpLanguage.INSTANCE, CSharpLambdaParameterImpl::new);

	IElementType LAMBDA_PARAMETER_LIST = new CompositeElementTypeAsPsiFactory("LAMBDA_PARAMETER_LIST", CSharpLanguage.INSTANCE, CSharpLambdaParameterListImpl::new);

	IElementType ANONYM_FIELD_OR_PROPERTY_SET = new CompositeElementTypeAsPsiFactory("ANONYM_FIELD_OR_PROPERTY_SET", CSharpLanguage.INSTANCE, CSharpAnonymFieldOrPropertySetImpl::new);

	IElementType NAMED_FIELD_OR_PROPERTY_SET = new CompositeElementTypeAsPsiFactory("NAMED_FIELD_OR_PROPERTY_SET", CSharpLanguage.INSTANCE, CSharpNamedFieldOrPropertySetImpl::new);

	IElementType FIELD_OR_PROPERTY_SET_BLOCK = new CompositeElementTypeAsPsiFactory("FIELD_OR_PROPERTY_SET_BLOCK", CSharpLanguage.INSTANCE, CSharpFieldOrPropertySetBlockImpl::new);

	IElementType ARRAY_INITIALIZER = new CompositeElementTypeAsPsiFactory("ARRAY_INITIALIZER", CSharpLanguage.INSTANCE, CSharpArrayInitializerImpl::new);

	IElementType ARRAY_INITIALIZER_SINGLE_VALUE = new CompositeElementTypeAsPsiFactory("ARRAY_INITIALIZER_SINGLE_VALUE", CSharpLanguage.INSTANCE, CSharpArrayInitializerSingleValueImpl::new);

	IElementType ARRAY_INITIALIZER_COMPOSITE_VALUE = new CompositeElementTypeAsPsiFactory("ARRAY_INITIALIZER_COMPOSITE_VALUE", CSharpLanguage.INSTANCE, CSharpArrayInitializerCompositeValueImpl::new);

	IElementType IMPLICIT_ARRAY_INITIALIZATION_EXPRESSION = new CompositeElementTypeAsPsiFactory("IMPLICIT_ARRAY_INITIALIZATION_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpImplicitArrayInitializationExpressionImpl::new);

	IElementType DICTIONARY_INITIALIZER_LIST = new CompositeElementTypeAsPsiFactory("DICTIONARY_INITIALIZER_LIST", CSharpLanguage.INSTANCE, CSharpDictionaryInitializerListImpl::new);

	IElementType DICTIONARY_INITIALIZER = new CompositeElementTypeAsPsiFactory("DICTIONARY_INITIALIZER", CSharpLanguage.INSTANCE, CSharpDictionaryInitializerImpl::new);

	IElementType CALL_ARGUMENT_LIST = new CompositeElementTypeAsPsiFactory("CALL_ARGUMENT_LIST", CSharpLanguage.INSTANCE, CSharpCallArgumentListImpl::new);

	IElementType CALL_ARGUMENT = new CompositeElementTypeAsPsiFactory("CALL_ARGUMENT", CSharpLanguage.INSTANCE, CSharpCallArgumentImpl::new);

	IElementType DOC_CALL_ARGUMENT = new CompositeElementTypeAsPsiFactory("DOC_CALL_ARGUMENT", CSharpLanguage.INSTANCE, CSharpDocCallArgumentImpl::new);

	IElementType NAMED_CALL_ARGUMENT = new CompositeElementTypeAsPsiFactory("NAMED_CALL_ARGUMENT", CSharpLanguage.INSTANCE, CSharpNamedCallArgumentImpl::new);

	IElementType LOCAL_VARIABLE_DECLARATION_STATEMENT = new CompositeElementTypeAsPsiFactory("LOCAL_VARIABLE_DECLARATION_STATEMENT", CSharpLanguage.INSTANCE, CSharpLocalVariableDeclarationStatementImpl
			::new);

	IElementType DECONSTRUCTION_STATEMENT = new CompositeElementTypeAsPsiFactory("DECONSTRUCTION_STATEMENT", CSharpLanguage.INSTANCE, CSharpDeconstructionStatementImpl::new);

	IElementType EXPRESSION_STATEMENT = new CompositeElementTypeAsPsiFactory("EXPRESSION_STATEMENT", CSharpLanguage.INSTANCE, CSharpExpressionStatementImpl::new);

	IElementType USING_STATEMENT = new CompositeElementTypeAsPsiFactory("USING_STATEMENT", CSharpLanguage.INSTANCE, CSharpUsingStatementImpl::new);

	IElementType LABELED_STATEMENT = new CompositeElementTypeAsPsiFactory("LABELED_STATEMENT", CSharpLanguage.INSTANCE, CSharpLabeledStatementImpl::new);

	IElementType GOTO_STATEMENT = new CompositeElementTypeAsPsiFactory("GOTO_STATEMENT", CSharpLanguage.INSTANCE, CSharpGotoStatementImpl::new);

	IElementType FIXED_STATEMENT = new CompositeElementTypeAsPsiFactory("FIXED_STATEMENT", CSharpLanguage.INSTANCE, CSharpFixedStatementImpl::new);

	IElementType LOCK_STATEMENT = new CompositeElementTypeAsPsiFactory("LOCK_STATEMENT", CSharpLanguage.INSTANCE, CSharpLockStatementImpl::new);

	IElementType EMPTY_STATEMENT = new CompositeElementTypeAsPsiFactory("EMPTY_STATEMENT", CSharpLanguage.INSTANCE, CSharpEmptyStatementImpl::new);

	IElementType FOREACH_STATEMENT = new CompositeElementTypeAsPsiFactory("FOREACH_STATEMENT", CSharpLanguage.INSTANCE, CSharpForeachStatementImpl::new);

	IElementType FOR_STATEMENT = new CompositeElementTypeAsPsiFactory("FOR_STATEMENT", CSharpLanguage.INSTANCE, CSharpForStatementImpl::new);

	IElementType TRY_STATEMENT = new CompositeElementTypeAsPsiFactory("TRY_STATEMENT", CSharpLanguage.INSTANCE, CSharpTryStatementImpl::new);

	IElementType CATCH_STATEMENT = new CompositeElementTypeAsPsiFactory("CATCH_STATEMENT", CSharpLanguage.INSTANCE, CSharpCatchStatementImpl::new);

	IElementType FINALLY_STATEMENT = new CompositeElementTypeAsPsiFactory("FINALLY_STATEMENT", CSharpLanguage.INSTANCE, CSharpFinallyStatementImpl::new);

	IElementType SWITCH_STATEMENT = new CompositeElementTypeAsPsiFactory("SWITCH_STATEMENT", CSharpLanguage.INSTANCE, CSharpSwitchStatementImpl::new);

	IElementType UNSAFE_STATEMENT = new CompositeElementTypeAsPsiFactory("UNSAFE_STATEMENT", CSharpLanguage.INSTANCE, CSharpUnsafeStatementImpl::new);

	IElementType CASE_OR_DEFAULT_STATEMENT = new CompositeElementTypeAsPsiFactory("CASE_OR_DEFAULT_STATEMENT", CSharpLanguage.INSTANCE, CSharpCaseOrDefaultStatementImpl::new);

	IElementType THROW_STATEMENT = new CompositeElementTypeAsPsiFactory("THROW_STATEMENT_STATEMENT", CSharpLanguage.INSTANCE, CSharpThrowStatementImpl::new);

	IElementType RETURN_STATEMENT = new CompositeElementTypeAsPsiFactory("RETURN_STATEMENT", CSharpLanguage.INSTANCE, CSharpReturnStatementImpl::new);

	IElementType CHECKED_STATEMENT = new CompositeElementTypeAsPsiFactory("CHECKED_STATEMENT", CSharpLanguage.INSTANCE, CSharpCheckedStatementImpl::new);

	IElementType YIELD_STATEMENT = new CompositeElementTypeAsPsiFactory("YIELD_STATEMENT", CSharpLanguage.INSTANCE, CSharpYieldStatementImpl::new);

	IElementType WHILE_STATEMENT = new CompositeElementTypeAsPsiFactory("WHILE_STATEMENT", CSharpLanguage.INSTANCE, CSharpWhileStatementImpl::new);

	IElementType DO_WHILE_STATEMENT = new CompositeElementTypeAsPsiFactory("DO_WHILE_STATEMENT", CSharpLanguage.INSTANCE, CSharpDoWhileStatementImpl::new);

	IElementType BREAK_STATEMENT = new CompositeElementTypeAsPsiFactory("BREAK_STATEMENT", CSharpLanguage.INSTANCE, CSharpBreakStatementImpl::new);

	IElementType CONTINUE_STATEMENT = new CompositeElementTypeAsPsiFactory("CONTINUE_STATEMENT", CSharpLanguage.INSTANCE, CSharpContinueStatementImpl::new);

	IElementType ATTRIBUTE_LIST = new CompositeElementTypeAsPsiFactory("ATTRIBUTE_LIST", CSharpLanguage.INSTANCE, CSharpAttributeListImpl::new);

	IElementType ATTRIBUTE = new CompositeElementTypeAsPsiFactory("ATTRIBUTE", CSharpLanguage.INSTANCE, CSharpAttributeImpl::new);

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
