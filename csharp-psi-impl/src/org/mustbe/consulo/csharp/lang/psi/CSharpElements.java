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
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IElementTypeAsPsiFactory;

/**
 * @author VISTALL
 * @since 22.11.13.
 */
public interface CSharpElements
{
	IElementType NAMESPACE_DECLARATION = CSharpStubElements.NAMESPACE_DECLARATION;

	IElementType USING_NAMESPACE_LIST = CSharpStubElements.USING_LIST;

	IElementType USING_NAMESPACE_STATEMENT = CSharpStubElements.USING_NAMESPACE_STATEMENT;

	IElementType TYPE_DEF_STATEMENT = CSharpStubElements.TYPE_DEF_STATEMENT;

	IElementType METHOD_DECLARATION = CSharpStubElements.METHOD_DECLARATION;

	IElementType CONSTRUCTOR_DECLARATION = CSharpStubElements.CONSTRUCTOR_DECLARATION;

	IElementType PARAMETER_LIST = CSharpStubElements.PARAMETER_LIST;

	IElementType PARAMETER = CSharpStubElements.PARAMETER;

	IElementType TYPE_DECLARATION = CSharpStubElements.TYPE_DECLARATION;

	IElementType DUMMY_DECLARATION = CSharpStubElements.DUMMY_DECLARATION;

	IElementType EVENT_DECLARATION = CSharpStubElements.EVENT_DECLARATION;

	IElementType CONVERSION_METHOD_DECLARATION = CSharpStubElements.CONVERSION_METHOD_DECLARATION;

	IElementType XXX_ACCESSOR = CSharpStubElements.XXX_ACCESSOR;

	IElementType FIELD_DECLARATION = CSharpStubElements.FIELD_DECLARATION;

	IElementType ENUM_CONSTANT_DECLARATION = CSharpStubElements.ENUM_CONSTANT_DECLARATION;

	IElementType LOCAL_VARIABLE = new IElementTypeAsPsiFactory("LOCAL_VARIABLE", CSharpLanguage.INSTANCE, CSharpLocalVariableImpl.class);

	IElementType PROPERTY_DECLARATION = CSharpStubElements.PROPERTY_DECLARATION;

	IElementType ARRAY_METHOD_DECLARATION = CSharpStubElements.ARRAY_METHOD_DECLARATION;

	IElementType GENERIC_PARAMETER_LIST = CSharpStubElements.GENERIC_PARAMETER_LIST;

	IElementType GENERIC_PARAMETER = CSharpStubElements.GENERIC_PARAMETER;

	IElementType GENERIC_CONSTRAINT_LIST = CSharpStubElements.GENERIC_CONSTRAINT_LIST;

	IElementType GENERIC_CONSTRAINT = CSharpStubElements.GENERIC_CONSTRAINT;

	IElementType GENERIC_CONSTRAINT_KEYWORD_VALUE = new IElementTypeAsPsiFactory("GENERIC_CONSTRAINT_KEYWORD_VALUE", CSharpLanguage.INSTANCE,
			CSharpGenericConstraintKeywordValueImpl.class);

	IElementType GENERIC_CONSTRAINT_TYPE_VALUE = new IElementTypeAsPsiFactory("GENERIC_CONSTRAINT_TYPE_VALUE", CSharpLanguage.INSTANCE,
			CSharpGenericConstraintTypeValueImpl.class);

	IElementType USER_TYPE = new IElementTypeAsPsiFactory("USER_TYPE", CSharpLanguage.INSTANCE, CSharpUserTypeImpl.class);

	IElementType POINTER_TYPE = new IElementTypeAsPsiFactory("POINTER_TYPE", CSharpLanguage.INSTANCE, DotNetPointerTypeImpl.class);

	IElementType NULLABLE_TYPE = new IElementTypeAsPsiFactory("NULLABLE_TYPE", CSharpLanguage.INSTANCE, CSharpNullableTypeImpl.class);

	IElementType NATIVE_TYPE = new IElementTypeAsPsiFactory("NATIVE_TYPE", CSharpLanguage.INSTANCE, CSharpNativeTypeImpl.class);

	IElementType ARRAY_TYPE = new IElementTypeAsPsiFactory("ARRAY_TYPE", CSharpLanguage.INSTANCE, CSharpArrayTypeImpl.class);

	IElementType TYPE_WRAPPER_WITH_TYPE_ARGUMENTS = new IElementTypeAsPsiFactory("TYPE_WRAPPER_WITH_TYPE_ARGUMENTS", CSharpLanguage.INSTANCE,
			CSharpTypeWithTypeArgumentsImpl.class);

	IElementType MODIFIER_LIST = new IElementTypeAsPsiFactory("MODIFIER_LIST", CSharpLanguage.INSTANCE, CSharpModifierListImpl.class);

	IElementType EXTENDS_LIST = CSharpStubElements.EXTENDS_LIST;

	IElementType TYPE_ARGUMENTS = new IElementTypeAsPsiFactory("TYPE_ARGUMENTS", CSharpLanguage.INSTANCE, CSharpTypeListImpl.class);

	IElementType CONSTANT_EXPRESSION = new IElementTypeAsPsiFactory("CONSTANT_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpConstantExpressionImpl.class);

	IElementType REFERENCE_EXPRESSION = new IElementTypeAsPsiFactory("REFERENCE_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpReferenceExpressionImpl.class);

	IElementType METHOD_CALL_EXPRESSION = new IElementTypeAsPsiFactory("METHOD_CALL_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpMethodCallExpressionImpl.class);

	IElementType CONSTRUCTOR_SUPER_CALL_EXPRESSION = new IElementTypeAsPsiFactory("CONSTRUCTOR_SUPER_CALL_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpConstructorSuperCallImpl.class);

	IElementType CHECKED_EXPRESSION = new IElementTypeAsPsiFactory("CHECKED_EXPRESSION", CSharpLanguage.INSTANCE, CSharpCheckedExpressionImpl.class);

	IElementType TYPE_OF_EXPRESSION = new IElementTypeAsPsiFactory("TYPE_OF_EXPRESSION", CSharpLanguage.INSTANCE, CSharpTypeOfExpressionImpl.class);

	IElementType SIZE_OF_EXPRESSION = new IElementTypeAsPsiFactory("SIZE_OF_EXPRESSION", CSharpLanguage.INSTANCE, CSharpSizeOfExpressionImpl.class);

	IElementType DEFAULT_EXPRESSION = new IElementTypeAsPsiFactory("DEFAULT_EXPRESSION", CSharpLanguage.INSTANCE, CSharpDefaultExpressionImpl.class);

	IElementType BINARY_EXPRESSION = new IElementTypeAsPsiFactory("BINARY_EXPRESSION", CSharpLanguage.INSTANCE, CSharpBinaryExpressionImpl.class);

	IElementType OPERATOR_REFERENCE = new IElementTypeAsPsiFactory("OPERATOR_REFERENCE", CSharpLanguage.INSTANCE, CSharpOperatorReferenceImpl.class);

	IElementType IS_EXPRESSION = new IElementTypeAsPsiFactory("IS_EXPRESSION", CSharpLanguage.INSTANCE, CSharpIsExpressionImpl.class);

	IElementType AS_EXPRESSION = new IElementTypeAsPsiFactory("AS_EXPRESSION", CSharpLanguage.INSTANCE, CSharpAsExpressionImpl.class);

	IElementType NEW_ARRAY_LENGTH = new IElementTypeAsPsiFactory("NEW_ARRAY_LENGTH", CSharpLanguage.INSTANCE, CSharpNewArrayLengthImpl.class);

	IElementType NEW_EXPRESSION = new IElementTypeAsPsiFactory("NEW_EXPRESSION", CSharpLanguage.INSTANCE, CSharpNewExpressionImpl.class);

	IElementType OUT_REF_WRAP_EXPRESSION = new IElementTypeAsPsiFactory("OUT_REF_WRAP_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpOutRefWrapExpressionImpl.class);

	IElementType CONDITIONAL_EXPRESSION = new IElementTypeAsPsiFactory("CONDITIONAL_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpConditionalExpressionImpl.class);

	IElementType NULL_COALESCING_EXPRESSION = new IElementTypeAsPsiFactory("NULL_COALESCING_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpNullCoalescingExpressionImpl.class);

	IElementType ASSIGNMENT_EXPRESSION = new IElementTypeAsPsiFactory("ASSIGNMENT_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpAssignmentExpressionImpl.class);

	IElementType TYPE_CAST_EXPRESSION = new IElementTypeAsPsiFactory("TYPE_CAST_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpTypeCastExpressionImpl.class);

	IElementType IF_STATEMENT = new IElementTypeAsPsiFactory("IF_STATEMENT", CSharpLanguage.INSTANCE, CSharpIfStatementImpl.class);

	IElementType BLOCK_STATEMENT = new IElementTypeAsPsiFactory("BLOCK_STATEMENT", CSharpLanguage.INSTANCE, CSharpBlockStatementImpl.class);

	IElementType ARRAY_ACCESS_EXPRESSION = new IElementTypeAsPsiFactory("ARRAY_ACCESS_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpArrayAccessExpressionImpl.class);

	IElementType POSTFIX_EXPRESSION = new IElementTypeAsPsiFactory("POSTFIX_EXPRESSION", CSharpLanguage.INSTANCE, CSharpPostfixExpressionImpl.class);

	IElementType PREFIX_EXPRESSION = new IElementTypeAsPsiFactory("PREFIX_EXPRESSION", CSharpLanguage.INSTANCE, CSharpPrefixExpressionImpl.class);

	IElementType POLYADIC_EXPRESSION = new IElementTypeAsPsiFactory("POLYADIC_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpPolyadicExpressionImpl.class);

	IElementType EMPTY_EXPRESSION = new IElementTypeAsPsiFactory("EMPTY_EXPRESSION", CSharpLanguage.INSTANCE, CSharpEmptyExpressionImpl.class);

	IElementType PARENTHESES_EXPRESSION = new IElementTypeAsPsiFactory("PARENTHESES_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpParenthesesExpressionImpl.class);

	IElementType LINQ_EXPRESSION = new IElementTypeAsPsiFactory("LINQ_EXPRESSION", CSharpLanguage.INSTANCE, CSharpLinqExpressionImpl.class);

	IElementType LINQ_FROM = new IElementTypeAsPsiFactory("LINQ_FROM", CSharpLanguage.INSTANCE, CSharpLinqFromImpl.class);

	IElementType LINQ_IN = new IElementTypeAsPsiFactory("LINQ_IN", CSharpLanguage.INSTANCE, CSharpLinqInImpl.class);

	IElementType LINQ_LET = new IElementTypeAsPsiFactory("LINQ_LET", CSharpLanguage.INSTANCE, CSharpLinqLetImpl.class);

	IElementType LINQ_WHERE = new IElementTypeAsPsiFactory("LINQ_WHERE", CSharpLanguage.INSTANCE, CSharpLinqWhereImpl.class);

	IElementType LINQ_SELECT = new IElementTypeAsPsiFactory("LINQ_SELECT", CSharpLanguage.INSTANCE, CSharpLinqSelectImpl.class);

	IElementType LAMBDA_EXPRESSION = new IElementTypeAsPsiFactory("LAMBDA_EXPRESSION", CSharpLanguage.INSTANCE, CSharpLambdaExpressionImpl.class);

	IElementType ANONYM_METHOD_EXPRESSION = new IElementTypeAsPsiFactory("ANONYM_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpAnonymMethodExpressionImpl.class);

	IElementType LAMBDA_PARAMETER = new IElementTypeAsPsiFactory("LAMBDA_PARAMETER", CSharpLanguage.INSTANCE, CSharpLambdaParameterImpl.class);

	IElementType LAMBDA_PARAMETER_LIST = new IElementTypeAsPsiFactory("LAMBDA_PARAMETER_LIST", CSharpLanguage.INSTANCE,
			CSharpLambdaParameterListImpl.class);

	IElementType FIELD_OR_PROPERTY_SET = new IElementTypeAsPsiFactory("FIELD_OR_PROPERTY_SET", CSharpLanguage.INSTANCE,
			CSharpFieldOrPropertySetImpl.class);

	IElementType FIELD_OR_PROPERTY_SET_BLOCK = new IElementTypeAsPsiFactory("FIELD_OR_PROPERTY_SET_BLOCK", CSharpLanguage.INSTANCE,
			CSharpFieldOrPropertySetBlockImpl.class);

	IElementType ARRAY_INITIALIZATION_EXPRESSION = new IElementTypeAsPsiFactory("ARRAY_INITIALIZATION_EXPRESSION", CSharpLanguage.INSTANCE,
			CSharpArrayInitializationExpressionImpl.class);

	IElementType METHOD_CALL_PARAMETER_LIST = new IElementTypeAsPsiFactory("METHOD_CALL_PARAMETER_LIST", CSharpLanguage.INSTANCE,
			CSharpMethodCallParameterListImpl.class);

	IElementType LOCAL_VARIABLE_DECLARATION_STATEMENT = new IElementTypeAsPsiFactory("LOCAL_VARIABLE_DECLARATION_STATEMENT",
			CSharpLanguage.INSTANCE, CSharpLocalVariableDeclarationStatementImpl.class);

	IElementType EXPRESSION_STATEMENT = new IElementTypeAsPsiFactory("EXPRESSION_STATEMENT", CSharpLanguage.INSTANCE,
			CSharpExpressionStatementImpl.class);

	IElementType USING_STATEMENT = new IElementTypeAsPsiFactory("USING_STATEMENT", CSharpLanguage.INSTANCE, CSharpUsingStatementImpl.class);

	IElementType LABELED_STATEMENT = new IElementTypeAsPsiFactory("LABELED_STATEMENT", CSharpLanguage.INSTANCE, CSharpLabeledStatementImpl.class);

	IElementType GOTO_STATEMENT = new IElementTypeAsPsiFactory("GOTO_STATEMENT", CSharpLanguage.INSTANCE, CSharpGotoStatementImpl.class);

	IElementType FIXED_STATEMENT = new IElementTypeAsPsiFactory("FIXED_STATEMENT", CSharpLanguage.INSTANCE, CSharpFixedStatementImpl.class);

	IElementType LOCK_STATEMENT = new IElementTypeAsPsiFactory("LOCK_STATEMENT", CSharpLanguage.INSTANCE, CSharpLockStatementImpl.class);

	IElementType EMPTY_STATEMENT = new IElementTypeAsPsiFactory("EMPTY_STATEMENT", CSharpLanguage.INSTANCE, CSharpEmptyStatementImpl.class);

	IElementType FOREACH_STATEMENT = new IElementTypeAsPsiFactory("FOREACH_STATEMENT", CSharpLanguage.INSTANCE, CSharpForeachStatementImpl.class);

	IElementType FOR_STATEMENT = new IElementTypeAsPsiFactory("FOR_STATEMENT", CSharpLanguage.INSTANCE, CSharpForStatementImpl.class);

	IElementType TRY_STATEMENT = new IElementTypeAsPsiFactory("TRY_STATEMENT", CSharpLanguage.INSTANCE, CSharpTryStatementImpl.class);

	IElementType CATCH_STATEMENT = new IElementTypeAsPsiFactory("CATCH_STATEMENT", CSharpLanguage.INSTANCE, CSharpCatchStatementImpl.class);

	IElementType FINALLY_STATEMENT = new IElementTypeAsPsiFactory("FINALLY_STATEMENT", CSharpLanguage.INSTANCE, CSharpFinallyStatementImpl.class);

	IElementType SWITCH_STATEMENT = new IElementTypeAsPsiFactory("SWITCH_STATEMENT", CSharpLanguage.INSTANCE, CSharpSwitchStatementImpl.class);

	IElementType SWITCH_LABEL_STATEMENT = new IElementTypeAsPsiFactory("SWITCH_LABEL_STATEMENT", CSharpLanguage.INSTANCE,
			CSharpSwitchLabelStatementImpl.class);

	IElementType THROW_STATEMENT = new IElementTypeAsPsiFactory("THROW_STATEMENT_STATEMENT", CSharpLanguage.INSTANCE,
			CSharpThrowStatementImpl.class);

	IElementType RETURN_STATEMENT = new IElementTypeAsPsiFactory("RETURN_STATEMENT", CSharpLanguage.INSTANCE, CSharpReturnStatementImpl.class);

	IElementType CHECKED_STATEMENT = new IElementTypeAsPsiFactory("CHECKED_STATEMENT", CSharpLanguage.INSTANCE, CSharpCheckedExpressionImpl.class);

	IElementType YIELD_STATEMENT = new IElementTypeAsPsiFactory("YIELD_STATEMENT", CSharpLanguage.INSTANCE, CSharpYieldStatementImpl.class);

	IElementType WHILE_STATEMENT = new IElementTypeAsPsiFactory("WHILE_STATEMENT", CSharpLanguage.INSTANCE, CSharpWhileStatementImpl.class);

	IElementType DO_WHILE_STATEMENT = new IElementTypeAsPsiFactory("DO_WHILE_STATEMENT", CSharpLanguage.INSTANCE, CSharpDoWhileStatementImpl.class);

	IElementType BREAK_STATEMENT = new IElementTypeAsPsiFactory("BREAK_STATEMENT", CSharpLanguage.INSTANCE, CSharpBreakStatementImpl.class);

	IElementType CONTINUE_STATEMENT = new IElementTypeAsPsiFactory("CONTINUE_STATEMENT", CSharpLanguage.INSTANCE, CSharpContinueStatementImpl.class);

	IElementType ATTRIBUTE_LIST = new IElementTypeAsPsiFactory("ATTRIBUTE_LIST", CSharpLanguage.INSTANCE, CSharpAttributeListImpl.class);

	IElementType ATTRIBUTE = new IElementTypeAsPsiFactory("ATTRIBUTE", CSharpLanguage.INSTANCE, CSharpAttributeImpl.class);
}
