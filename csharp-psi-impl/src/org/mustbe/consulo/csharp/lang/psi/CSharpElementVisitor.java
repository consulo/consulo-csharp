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

import org.mustbe.consulo.csharp.lang.psi.impl.source.*;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetPointerType;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import com.intellij.psi.PsiElementVisitor;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpElementVisitor extends PsiElementVisitor
{
	public static final CSharpElementVisitor EMPTY = new CSharpElementVisitor();

	public void visitCSharpFile(CSharpFileImpl file)
	{
		visitFile(file);
	}

	public void visitUsingNamespaceList(CSharpUsingListImpl list)
	{
		visitElement(list);
	}

	public void visitConstructorDeclaration(CSharpConstructorDeclaration declaration)
	{
		visitElement(declaration);
	}

	public void visitMethodDeclaration(CSharpMethodDeclaration declaration)
	{
		visitElement(declaration);
	}

	public void visitModifierList(CSharpModifierList list)
	{
		visitElement(list);
	}

	public void visitNamespaceDeclaration(CSharpNamespaceDeclaration declaration)
	{
		visitElement(declaration);
	}

	public void visitParameter(DotNetParameter parameter)
	{
		visitVariable(parameter);
	}

	public void visitParameterList(DotNetParameterList list)
	{
		visitElement(list);
	}

	public void visitReferenceExpression(CSharpReferenceExpression expression)
	{
		visitElement(expression);
	}

	public void visitTypeDeclaration(CSharpTypeDeclaration declaration)
	{
		visitElement(declaration);
	}

	public void visitUserType(CSharpUserType type)
	{
		visitElement(type);
	}

	public void visitUsingNamespaceStatement(CSharpUsingNamespaceStatement statement)
	{
		visitElement(statement);
	}

	public void visitGenericParameter(DotNetGenericParameter parameter)
	{
		visitElement(parameter);
	}

	public void visitGenericParameterList(DotNetGenericParameterList list)
	{
		visitElement(list);
	}

	public void visitGenericConstraintList(CSharpGenericConstraintList list)
	{
		visitElement(list);
	}

	public void visitGenericConstraint(CSharpGenericConstraint constraint)
	{
		visitElement(constraint);
	}

	public void visitGenericConstraintKeywordValue(CSharpGenericConstraintKeywordValue value)
	{
		visitElement(value);
	}

	public void visitGenericConstraintTypeValue(CSharpGenericConstraintTypeValue value)
	{
		visitElement(value);
	}

	public void visitTypeList(DotNetTypeList list)
	{
		visitElement(list);
	}

	public void visitEventDeclaration(CSharpEventDeclaration declaration)
	{
		visitVariable(declaration);
	}

	public void visitPropertyDeclaration(CSharpPropertyDeclaration declaration)
	{
		visitVariable(declaration);
	}

	public void visitXXXAccessor(DotNetXXXAccessor accessor)
	{
		visitElement(accessor);
	}

	public void visitFieldDeclaration(CSharpFieldDeclaration declaration)
	{
		visitVariable(declaration);
	}

	public void visitPointerType(DotNetPointerType type)
	{
		visitElement(type);
	}

	public void visitNullableType(CSharpNullableType type)
	{
		visitElement(type);
	}

	public void visitNativeType(CSharpNativeType type)
	{
		visitElement(type);
	}

	public void visitArrayType(CSharpArrayType type)
	{
		visitElement(type);
	}

	public void visitLocalVariable(CSharpLocalVariable variable)
	{
		visitVariable(variable);
	}

	public void visitConstantExpression(CSharpConstantExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitLocalVariableDeclarationStatement(CSharpLocalVariableDeclarationStatement statement)
	{
		visitElement(statement);
	}

	public void visitExpressionStatement(CSharpExpressionStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitMethodCallExpression(CSharpMethodCallExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitMethodCallParameterList(CSharpCallArgumentList list)
	{
		visitElement(list);
	}

	public void visitTypeOfExpression(CSharpTypeOfExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitAttributeList(CSharpAttributeList list)
	{
		visitElement(list);
	}

	public void visitAttribute(CSharpAttribute attribute)
	{
		visitElement(attribute);
	}

	public void visitBinaryExpression(CSharpBinaryExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitNewExpression(CSharpNewExpression expression)
	{
		visitElement(expression);
	}

	public void visitFieldOrPropertySetBlock(CSharpFieldOrPropertySetBlock block)
	{
		visitElement(block);
	}

	public void visitFieldOrPropertySet(CSharpFieldOrPropertySet element)
	{
		visitElement(element);
	}

	public void visitLockStatement(CSharpLockStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitParenthesesExpression(CSharpParenthesesExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitBreakStatement(CSharpBreakStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitContinueStatement(CSharpContinueStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitReturnStatement(CSharpReturnStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitYieldStatement(CSharpYieldStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitWhileStatement(CSharpWhileStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitIsExpression(CSharpIsExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitConditionalExpression(CSharpConditionalExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitNullCoalescingExpression(CSharpNullCoalescingExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitAssignmentExpression(CSharpAssignmentExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitTypeCastExpression(CSharpTypeCastExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitArrayAccessExpression(CSharpArrayAccessExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitPostfixExpression(CSharpPostfixExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitPrefixExpression(CSharpPrefixExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitLambdaExpression(CSharpLambdaExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitLinqExpression(CSharpLinqExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitForeachStatement(CSharpForeachStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitIfStatement(CSharpIfStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitBlockStatement(CSharpBlockStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitAsExpression(CSharpAsExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitDefaultExpression(CSharpDefaultExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitUsingStatement(CSharpUsingStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitSizeOfExpression(CSharpSizeOfExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitFixedStatement(CSharpFixedStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitGotoStatement(CSharpGotoStatementImpl element)
	{
		visitElement(element);
	}

	public void visitLabeledStatement(CSharpLabeledStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitEnumConstantDeclaration(CSharpEnumConstantDeclaration declaration)
	{
		visitVariable(declaration);
	}

	public void visitConversionMethodDeclaration(CSharpConversionMethodDeclaration element)
	{
		visitElement(element);
	}

	public void visitDoWhileStatement(CSharpDoWhileStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitEmptyStatement(CSharpEmptyStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitForStatement(CSharpForStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitTryStatement(CSharpTryStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitCatchStatement(CSharpCatchStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitFinallyStatement(CSharpFinallyStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitThrowStatement(CSharpThrowStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitLambdaParameter(CSharpLambdaParameterImpl parameter)
	{
		visitVariable(parameter);
	}

	public void visitLambdaParameterList(CSharpLambdaParameterListImpl list)
	{
		visitElement(list);
	}

	public void visitAnonymMethodExpression(CSharpDelegateExpressionImpl method)
	{
		visitElement(method);
	}

	public void visitArrayInitializerExpression(CSharpArrayInitializerImpl expression)
	{
		visitElement(expression);
	}

	public void visitImplicitArrayInitializationExpression(CSharpImplicitArrayInitializationExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitTypeDefStatement(CSharpTypeDefStatement statement)
	{
		visitElement(statement);
	}

	public void visitCheckedStatement(CSharpCheckedStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitCheckedExpression(CSharpCheckedExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitOurRefWrapExpression(CSharpOutRefWrapExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitSwitchStatement(CSharpSwitchStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitSwitchLabelStatement(CSharpSwitchLabelStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitArrayMethodDeclaration(CSharpArrayMethodDeclaration methodDeclaration)
	{
		visitElement(methodDeclaration);
	}

	public void visitDummyDeclaration(CSharpDummyDeclarationImpl declaration)
	{
		visitElement(declaration);
	}

	public void visitOperatorReference(CSharpOperatorReferenceImpl referenceExpression)
	{
		visitElement(referenceExpression);
	}

	public void visitConstructorSuperCall(CSharpConstructorSuperCallImpl call)
	{
		visitElement(call);
	}

	public void visitNewArrayLength(CSharpNewArrayLengthImpl element)
	{
		visitElement(element);
	}

	public void visitCallArgument(CSharpCallArgument argument)
	{
		visitElement(argument);
	}

	public void visitNamedCallArgument(CSharpNamedCallArgument argument)
	{
		visitElement(argument);
	}

	public void visitAwaitExpression(CSharpAwaitExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitLinqFromClause(CSharpLinqFromClauseImpl clause)
	{
		visitElement(clause);
	}

	public void visitLinqSelectOrGroupClause(CSharpLinqSelectOrGroupClauseImpl clause)
	{
		visitElement(clause);
	}

	public void visitLinqQueryBody(CSharpLinqQueryBodyImpl body)
	{
		visitElement(body);
	}

	public void visitLinqWhereClause(CSharpLinqWhereClauseImpl clause)
	{
		visitElement(clause);
	}

	public void visitLinqVariable(CSharpLinqVariable variable)
	{
		visitVariable(variable);
	}

	public void visitLinqOrderByClause(CSharpLinqOrderByClauseImpl clause)
	{
		visitElement(clause);
	}

	public void visitLinqOrderByOrdering(CSharpLinqOrderByOrderingImpl ordering)
	{
		visitElement(ordering);
	}

	public void visitLinqLetClause(CSharpLinqLetClauseImpl clause)
	{
		visitElement(clause);
	}

	public void visitLinqJoinClause(CSharpLinqJoinClauseImpl clause)
	{
		visitElement(clause);
	}

	public void visitLinqIntroClause(CSharpLinqIntoClauseImpl clause)
	{
		visitElement(clause);
	}

	public void visitUnsafeStatement(CSharpUnsafeStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitStackAllocExpression(CSharpStackAllocExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitUsingTypeStatement(CSharpUsingTypeStatement statement)
	{
		visitElement(statement);
	}

	public void visitArrayInitializerSingleValue(CSharpArrayInitializerSingleValueImpl value)
	{
		visitElement(value);
	}

	public void visitArrayInitializerCompositeValue(CSharpArrayInitializerCompositeValueImpl value)
	{
		visitElement(value);
	}

	public void visitArglistExpression(CSharpArglistExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitMakeRefExpression(CSharpMakeRefExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitRefValueExpression(CSharpRefValueExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitRefTypeExpression(CSharpRefTypeExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitVariable(DotNetVariable variable)
	{
		visitElement(variable);
	}

	public void visitNameOfExpression(CSharpNameOfExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitDictionaryInitializer(CSharpDictionaryInitializerImpl initializer)
	{
		visitElement(initializer);
	}

	public void visitDictionaryInitializerList(CSharpDictionaryInitializerListImpl list)
	{
		visitElement(list);
	}
}
