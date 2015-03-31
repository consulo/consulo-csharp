/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi;

import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.impl.source.*;
import com.intellij.psi.tree.ElementTypeAsPsiFactory;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 22.11.13.
 */
public interface CSharpElements
{
	IElementType NAMESPACE_DECLARATION = CSharpStubElements.NAMESPACE_DECLARATION;

	IElementType USING_LIST = CSharpStubElements.USING_LIST;

	IElementType USING_NAMESPACE_STATEMENT = CSharpStubElements.USING_NAMESPACE_STATEMENT;

	IElementType USING_TYPE_STATEMENT = CSharpStubElements.USING_TYPE_STATEMENT;

	IElementType TYPE_DEF_STATEMENT = CSharpStubElements.TYPE_DEF_STATEMENT;

	IElementType METHOD_DECLARATION = CSharpStubElements.METHOD_DECLARATION;

	IElementType CONSTRUCTOR_DECLARATION = CSharpStubElements.CONSTRUCTOR_DECLARATION;

	IElementType PARAMETER_LIST = new ElementTypeAsPsiFactory("PARAMETER_LIST", CSharpLanguage.INSTANCE, CSharpParameterListImpl.class);

	IElementType PARAMETER = new ElementTypeAsPsiFactory("PARAMETER", CSharpLanguage.INSTANCE, CSharpParameterImpl.class);

	IElementType TYPE_DECLARATION = CSharpStubElements.TYPE_DECLARATION;

	IElementType DUMMY_DECLARATION = CSharpStubElements.DUMMY_DECLARATION;

	IElementType EVENT_DECLARATION = CSharpStubElements.EVENT_DECLARATION;

	IElementType CONVERSION_METHOD_DECLARATION = CSharpStubElements.CONVERSION_METHOD_DECLARATION;

	IElementType XXX_ACCESSOR = CSharpStubElements.XXX_ACCESSOR;

	IElementType FIELD_DECLARATION = CSharpStubElements.FIELD_DECLARATION;

	IElementType ENUM_CONSTANT_DECLARATION = CSharpStubElements.ENUM_CONSTANT_DECLARATION;

	IElementType LOCAL_VARIABLE = new ElementTypeAsPsiFactory("LOCAL_VARIABLE", CSharpLanguage.INSTANCE, CSharpLocalVariableImpl.class);

	IElementType PROPERTY_DECLARATION = CSharpStubElements.PROPERTY_DECLARATION;

	IElementType ARRAY_METHOD_DECLARATION = CSharpStubElements.ARRAY_METHOD_DECLARATION;

	IElementType GENERIC_PARAMETER_LIST = CSharpStubElements.GENERIC_PARAMETER_LIST;

	IElementType GENERIC_PARAMETER = CSharpStubElements.GENERIC_PARAMETER;

	IElementType GENERIC_CONSTRAINT_LIST = CSharpStubElements.GENERIC_CONSTRAINT_LIST;

	IElementType GENERIC_CONSTRAINT = CSharpStubElements.GENERIC_CONSTRAINT;

	IElementType GENERIC_CONSTRAINT_KEYWORD_VALUE = CSharpStubElements.GENERIC_CONSTRAINT_KEYWORD_VALUE;

	IElementType GENERIC_CONSTRAINT_TYPE_VALUE = CSharpStubElements.GENERIC_CONSTRAINT_TYPE_VALUE;

	IElementType USER_TYPE = new ElementTypeAsPsiFactory("USER_TYPE", CSharpLanguage.INSTANCE, CSharpUserTypeImpl.class);

	IElementType POINTER_TYPE = new ElementTypeAsPsiFactory("POINTER_TYPE", CSharpLanguage.INSTANCE, CSharpPointerTypeImpl.class);

	IElementType NULLABLE_TYPE = new ElementTypeAsPsiFactory("NULLABLE_TYPE", CSharpLanguage.INSTANCE, CSharpNullableTypeImpl.class);

	IElementType NATIVE_TYPE = new ElementTypeAsPsiFactory("NATIVE_TYPE", CSharpLanguage.INSTANCE, CSharpNativeTypeImpl.class);

	IElementType ARRAY_TYPE = new ElementTypeAsPsiFactory("ARRAY_TYPE", CSharpLanguage.INSTANCE, CSharpArrayTypeImpl.class);

	IElementType MODIFIER_LIST = new ElementTypeAsPsiFactory("MODIFIER_LIST", CSharpLanguage.INSTANCE, CSharpModifierListImpl.class);

	IElementType EXTENDS_LIST = CSharpStubElements.EXTENDS_LIST;

	IElementType TYPE_ARGUMENTS = new ElementTypeAsPsiFactory("TYPE_ARGUMENTS", CSharpLanguage.INSTANCE, CSharpTypeListImpl.class);

	IElementType EMPTY_TYPE_ARGUMENTS = new ElementTypeAsPsiFactory("EMPTY_TYPE_ARGUMENTS", CSharpLanguage.INSTANCE, CSharpEmptyTypeListImpl.class);

	IElementType CONSTANT_EXPRESSION = new ElementTypeAsPsiFactory("CONSTANT_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpConstantExpressionImpl.class);

	IElementType REFERENCE_EXPRESSION = new ElementTypeAsPsiFactory("REFERENCE_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpReferenceExpressionImpl.class);

	IElementType METHOD_CALL_EXPRESSION = new ElementTypeAsPsiFactory("METHOD_CALL_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpMethodCallExpressionImpl.class);

	IElementType CONSTRUCTOR_SUPER_CALL_EXPRESSION = new ElementTypeAsPsiFactory("CONSTRUCTOR_SUPER_CALL_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpConstructorSuperCallImpl.class);

	IElementType CHECKED_EXPRESSION = new ElementTypeAsPsiFactory("CHECKED_EXPRESSION", CSharpLanguage.INSTANCE, CSharpCheckedExpressionImpl.class);

	IElementType TYPE_OF_EXPRESSION = new ElementTypeAsPsiFactory("TYPE_OF_EXPRESSION", CSharpLanguage.INSTANCE, CSharpTypeOfExpressionImpl.class);

	IElementType NAMEOF_EXPRESSION = new ElementTypeAsPsiFactory("NAMEOF_EXPRESSION", CSharpLanguage.INSTANCE, CSharpNameOfExpressionImpl.class);

	IElementType AWAIT_EXPRESSION = new ElementTypeAsPsiFactory("AWAIT_EXPRESSION", CSharpLanguage.INSTANCE, CSharpAwaitExpressionImpl.class);

	IElementType SIZE_OF_EXPRESSION = new ElementTypeAsPsiFactory("SIZE_OF_EXPRESSION", CSharpLanguage.INSTANCE, CSharpSizeOfExpressionImpl.class);

	IElementType DEFAULT_EXPRESSION = new ElementTypeAsPsiFactory("DEFAULT_EXPRESSION", CSharpLanguage.INSTANCE, CSharpDefaultExpressionImpl.class);

	IElementType BINARY_EXPRESSION = new ElementTypeAsPsiFactory("BINARY_EXPRESSION", CSharpLanguage.INSTANCE, CSharpBinaryExpressionImpl.class);

	IElementType OPERATOR_REFERENCE = new ElementTypeAsPsiFactory("OPERATOR_REFERENCE", CSharpLanguage.INSTANCE, CSharpOperatorReferenceImpl.class);

	IElementType IS_EXPRESSION = new ElementTypeAsPsiFactory("IS_EXPRESSION", CSharpLanguage.INSTANCE, CSharpIsExpressionImpl.class);

	IElementType AS_EXPRESSION = new ElementTypeAsPsiFactory("AS_EXPRESSION", CSharpLanguage.INSTANCE, CSharpAsExpressionImpl.class);

	IElementType NEW_ARRAY_LENGTH = new ElementTypeAsPsiFactory("NEW_ARRAY_LENGTH", CSharpLanguage.INSTANCE, CSharpNewArrayLengthImpl.class);

	IElementType NEW_EXPRESSION = new ElementTypeAsPsiFactory("NEW_EXPRESSION", CSharpLanguage.INSTANCE, CSharpNewExpressionImpl.class);

	IElementType __ARGLIST_EXPRESSION = new ElementTypeAsPsiFactory("__ARGLIST_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpArglistExpressionImpl.class);

	IElementType __MAKEREF_EXPRESSION = new ElementTypeAsPsiFactory("__MAKEREF_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpMakeRefExpressionImpl.class);

	IElementType __REFTYPE_EXPRESSION = new ElementTypeAsPsiFactory("__REFTYPE_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpRefTypeExpressionImpl.class);

	IElementType __REFVALUE_EXPRESSION = new ElementTypeAsPsiFactory("__REFVALUE_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpRefValueExpressionImpl.class);

	IElementType STACKALLOC_EXPRESSION = new ElementTypeAsPsiFactory("STACKALLOC_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpStackAllocExpressionImpl.class);

	IElementType OUT_REF_WRAP_EXPRESSION = new ElementTypeAsPsiFactory("OUT_REF_WRAP_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpOutRefWrapExpressionImpl.class);

	IElementType CONDITIONAL_EXPRESSION = new ElementTypeAsPsiFactory("CONDITIONAL_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpConditionalExpressionImpl.class);

	IElementType NULL_COALESCING_EXPRESSION = new ElementTypeAsPsiFactory("NULL_COALESCING_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpNullCoalescingExpressionImpl.class);

	IElementType ASSIGNMENT_EXPRESSION = new ElementTypeAsPsiFactory("ASSIGNMENT_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpAssignmentExpressionImpl.class);

	IElementType TYPE_CAST_EXPRESSION = new ElementTypeAsPsiFactory("TYPE_CAST_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpTypeCastExpressionImpl.class);

	IElementType IF_STATEMENT = new ElementTypeAsPsiFactory("IF_STATEMENT", CSharpLanguage.INSTANCE, CSharpIfStatementImpl.class);

	IElementType BLOCK_STATEMENT = new ElementTypeAsPsiFactory("BLOCK_STATEMENT", CSharpLanguage.INSTANCE, CSharpBlockStatementImpl.class);

	IElementType ARRAY_ACCESS_EXPRESSION = new ElementTypeAsPsiFactory("ARRAY_ACCESS_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpArrayAccessExpressionImpl.class);

	IElementType POSTFIX_EXPRESSION = new ElementTypeAsPsiFactory("POSTFIX_EXPRESSION", CSharpLanguage.INSTANCE, CSharpPostfixExpressionImpl.class);

	IElementType PREFIX_EXPRESSION = new ElementTypeAsPsiFactory("PREFIX_EXPRESSION", CSharpLanguage.INSTANCE, CSharpPrefixExpressionImpl.class);

	IElementType ERROR_EXPRESSION = new ElementTypeAsPsiFactory("ERROR_EXPRESSION", CSharpLanguage.INSTANCE, CSharpErrorExpressionImpl.class);

	IElementType PARENTHESES_EXPRESSION = new ElementTypeAsPsiFactory("PARENTHESES_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpParenthesesExpressionImpl.class);

	IElementType LINQ_EXPRESSION = new ElementTypeAsPsiFactory("LINQ_EXPRESSION", CSharpLanguage.INSTANCE, CSharpLinqExpressionImpl.class);

	IElementType LINQ_VARIABLE = new ElementTypeAsPsiFactory("LINQ_VARIABLE", CSharpLanguage.INSTANCE, CSharpLinqVariableImpl.class);

	IElementType LINQ_QUERY_BODY = new ElementTypeAsPsiFactory("LINQ_QUERY_BODY", CSharpLanguage.INSTANCE, CSharpLinqQueryBodyImpl.class);

	IElementType LINQ_FROM_CLAUSE = new ElementTypeAsPsiFactory("LINQ_FROM_CLAUSE", CSharpLanguage.INSTANCE, CSharpLinqFromClauseImpl.class);

	IElementType LINQ_WHERE_CLAUSE = new ElementTypeAsPsiFactory("LINQ_WHERE_CLAUSE", CSharpLanguage.INSTANCE, CSharpLinqWhereClauseImpl.class);

	IElementType LINQ_ORDERBY_CLAUSE = new ElementTypeAsPsiFactory("LINQ_ORDERBY_CLAUSE", CSharpLanguage.INSTANCE,
			CSharpLinqOrderByClauseImpl.class);

	IElementType LINQ_LET_CLAUSE = new ElementTypeAsPsiFactory("LINQ_LET_CLAUSE", CSharpLanguage.INSTANCE, CSharpLinqLetClauseImpl.class);

	IElementType LINQ_JOIN_CLAUSE = new ElementTypeAsPsiFactory("LINQ_JOIN_CLAUSE", CSharpLanguage.INSTANCE, CSharpLinqJoinClauseImpl.class);

	IElementType LINQ_INTRO_CLAUSE = new ElementTypeAsPsiFactory("LINQ_INTRO_CLAUSE", CSharpLanguage.INSTANCE, CSharpLinqIntoClauseImpl.class);

	IElementType LINQ_ORDERBY_ORDERING = new ElementTypeAsPsiFactory("LINQ_ORDERBY_ORDERING", CSharpLanguage.INSTANCE,
			CSharpLinqOrderByOrderingImpl.class);

	IElementType LINQ_SELECT_OR_GROUP_CLAUSE = new ElementTypeAsPsiFactory("LINQ_SELECT_OR_GROUP_CLAUSE", CSharpLanguage.INSTANCE,
			CSharpLinqSelectOrGroupClauseImpl.class);

	IElementType LAMBDA_EXPRESSION = new ElementTypeAsPsiFactory("LAMBDA_EXPRESSION", CSharpLanguage.INSTANCE, CSharpLambdaExpressionImpl.class);

	IElementType DELEGATE_EXPRESSION = new ElementTypeAsPsiFactory("DELEGATE_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpDelegateExpressionImpl.class);

	IElementType LAMBDA_PARAMETER = new ElementTypeAsPsiFactory("LAMBDA_PARAMETER", CSharpLanguage.INSTANCE, CSharpLambdaParameterImpl.class);

	IElementType LAMBDA_PARAMETER_LIST = new ElementTypeAsPsiFactory("LAMBDA_PARAMETER_LIST", CSharpLanguage.INSTANCE,
			CSharpLambdaParameterListImpl.class);

	IElementType FIELD_OR_PROPERTY_SET = new ElementTypeAsPsiFactory("FIELD_OR_PROPERTY_SET", CSharpLanguage.INSTANCE,
			CSharpFieldOrPropertySetImpl.class);

	IElementType FIELD_OR_PROPERTY_SET_BLOCK = new ElementTypeAsPsiFactory("FIELD_OR_PROPERTY_SET_BLOCK", CSharpLanguage.INSTANCE,
			CSharpFieldOrPropertySetBlockImpl.class);

	IElementType ARRAY_INITIALIZER = new ElementTypeAsPsiFactory("ARRAY_INITIALIZER", CSharpLanguage.INSTANCE, CSharpArrayInitializerImpl.class);

	IElementType ARRAY_INITIALIZER_SINGLE_VALUE = new ElementTypeAsPsiFactory("ARRAY_INITIALIZER_SINGLE_VALUE", CSharpLanguage.INSTANCE,
			CSharpArrayInitializerSingleValueImpl.class);

	IElementType ARRAY_INITIALIZER_COMPOSITE_VALUE = new ElementTypeAsPsiFactory("ARRAY_INITIALIZER_COMPOSITE_VALUE", CSharpLanguage.INSTANCE,
			CSharpArrayInitializerCompositeValueImpl.class);

	IElementType IMPLICIT_ARRAY_INITIALIZATION_EXPRESSION = new ElementTypeAsPsiFactory("IMPLICIT_ARRAY_INITIALIZATION_EXPRESSION",
			CSharpLanguage.INSTANCE, CSharpImplicitArrayInitializationExpressionImpl.class);

	IElementType DICTIONARY_INITIALIZER_LIST = new ElementTypeAsPsiFactory("DICTIONARY_INITIALIZER_LIST", CSharpLanguage.INSTANCE,
			CSharpDictionaryInitializerListImpl.class);

	IElementType DICTIONARY_INITIALIZER = new ElementTypeAsPsiFactory("DICTIONARY_INITIALIZER", CSharpLanguage.INSTANCE,
			CSharpDictionaryInitializerImpl.class);

	IElementType CALL_ARGUMENT_LIST = new ElementTypeAsPsiFactory("CALL_ARGUMENT_LIST", CSharpLanguage.INSTANCE, CSharpCallArgumentListImpl.class);

	IElementType CALL_ARGUMENT = new ElementTypeAsPsiFactory("CALL_ARGUMENT", CSharpLanguage.INSTANCE, CSharpCallArgumentImpl.class);

	IElementType DOC_CALL_ARGUMENT = new ElementTypeAsPsiFactory("DOC_CALL_ARGUMENT", CSharpLanguage.INSTANCE, CSharpDocCallArgumentImpl.class);

	IElementType NAMED_CALL_ARGUMENT = new ElementTypeAsPsiFactory("NAMED_CALL_ARGUMENT", CSharpLanguage.INSTANCE,
			CSharpNamedCallArgumentImpl.class);

	IElementType LOCAL_VARIABLE_DECLARATION_STATEMENT = new ElementTypeAsPsiFactory("LOCAL_VARIABLE_DECLARATION_STATEMENT", CSharpLanguage.INSTANCE,
			CSharpLocalVariableDeclarationStatementImpl.class);

	IElementType EXPRESSION_STATEMENT = new ElementTypeAsPsiFactory("EXPRESSION_STATEMENT", CSharpLanguage.INSTANCE,
			CSharpExpressionStatementImpl.class);

	IElementType USING_STATEMENT = new ElementTypeAsPsiFactory("USING_STATEMENT", CSharpLanguage.INSTANCE, CSharpUsingStatementImpl.class);

	IElementType LABELED_STATEMENT = new ElementTypeAsPsiFactory("LABELED_STATEMENT", CSharpLanguage.INSTANCE, CSharpLabeledStatementImpl.class);

	IElementType GOTO_STATEMENT = new ElementTypeAsPsiFactory("GOTO_STATEMENT", CSharpLanguage.INSTANCE, CSharpGotoStatementImpl.class);

	IElementType FIXED_STATEMENT = new ElementTypeAsPsiFactory("FIXED_STATEMENT", CSharpLanguage.INSTANCE, CSharpFixedStatementImpl.class);

	IElementType LOCK_STATEMENT = new ElementTypeAsPsiFactory("LOCK_STATEMENT", CSharpLanguage.INSTANCE, CSharpLockStatementImpl.class);

	IElementType EMPTY_STATEMENT = new ElementTypeAsPsiFactory("EMPTY_STATEMENT", CSharpLanguage.INSTANCE, CSharpEmptyStatementImpl.class);

	IElementType FOREACH_STATEMENT = new ElementTypeAsPsiFactory("FOREACH_STATEMENT", CSharpLanguage.INSTANCE, CSharpForeachStatementImpl.class);

	IElementType FOR_STATEMENT = new ElementTypeAsPsiFactory("FOR_STATEMENT", CSharpLanguage.INSTANCE, CSharpForStatementImpl.class);

	IElementType TRY_STATEMENT = new ElementTypeAsPsiFactory("TRY_STATEMENT", CSharpLanguage.INSTANCE, CSharpTryStatementImpl.class);

	IElementType CATCH_STATEMENT = new ElementTypeAsPsiFactory("CATCH_STATEMENT", CSharpLanguage.INSTANCE, CSharpCatchStatementImpl.class);

	IElementType FINALLY_STATEMENT = new ElementTypeAsPsiFactory("FINALLY_STATEMENT", CSharpLanguage.INSTANCE, CSharpFinallyStatementImpl.class);

	IElementType SWITCH_STATEMENT = new ElementTypeAsPsiFactory("SWITCH_STATEMENT", CSharpLanguage.INSTANCE, CSharpSwitchStatementImpl.class);

	IElementType UNSAFE_STATEMENT = new ElementTypeAsPsiFactory("UNSAFE_STATEMENT", CSharpLanguage.INSTANCE, CSharpUnsafeStatementImpl.class);

	IElementType SWITCH_LABEL_STATEMENT = new ElementTypeAsPsiFactory("SWITCH_LABEL_STATEMENT", CSharpLanguage.INSTANCE,
			CSharpSwitchLabelStatementImpl.class);

	IElementType THROW_STATEMENT = new ElementTypeAsPsiFactory("THROW_STATEMENT_STATEMENT", CSharpLanguage.INSTANCE, CSharpThrowStatementImpl.class);

	IElementType RETURN_STATEMENT = new ElementTypeAsPsiFactory("RETURN_STATEMENT", CSharpLanguage.INSTANCE, CSharpReturnStatementImpl.class);

	IElementType CHECKED_STATEMENT = new ElementTypeAsPsiFactory("CHECKED_STATEMENT", CSharpLanguage.INSTANCE, CSharpCheckedExpressionImpl.class);

	IElementType YIELD_STATEMENT = new ElementTypeAsPsiFactory("YIELD_STATEMENT", CSharpLanguage.INSTANCE, CSharpYieldStatementImpl.class);

	IElementType WHILE_STATEMENT = new ElementTypeAsPsiFactory("WHILE_STATEMENT", CSharpLanguage.INSTANCE, CSharpWhileStatementImpl.class);

	IElementType DO_WHILE_STATEMENT = new ElementTypeAsPsiFactory("DO_WHILE_STATEMENT", CSharpLanguage.INSTANCE, CSharpDoWhileStatementImpl.class);

	IElementType BREAK_STATEMENT = new ElementTypeAsPsiFactory("BREAK_STATEMENT", CSharpLanguage.INSTANCE, CSharpBreakStatementImpl.class);

	IElementType CONTINUE_STATEMENT = new ElementTypeAsPsiFactory("CONTINUE_STATEMENT", CSharpLanguage.INSTANCE, CSharpContinueStatementImpl.class);

	IElementType ATTRIBUTE_LIST = new ElementTypeAsPsiFactory("ATTRIBUTE_LIST", CSharpLanguage.INSTANCE, CSharpAttributeListImpl.class);

	IElementType ATTRIBUTE = new ElementTypeAsPsiFactory("ATTRIBUTE", CSharpLanguage.INSTANCE, CSharpAttributeImpl.class);
}
