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
import org.mustbe.consulo.dotnet.psi.DotNetFieldDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.psi.DotNetUserType;
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

	public void visitModifierList(CSharpModifierListImpl list)
	{
		visitElement(list);
	}

	public void visitNamespaceDeclaration(CSharpNamespaceDeclarationImpl declaration)
	{
		visitElement(declaration);
	}

	public void visitParameter(DotNetParameter parameter)
	{
		visitElement(parameter);
	}

	public void visitParameterList(DotNetParameterList list)
	{
		visitElement(list);
	}

	public void visitReferenceExpression(CSharpReferenceExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitTypeDeclaration(CSharpTypeDeclaration declaration)
	{
		visitElement(declaration);
	}

	public void visitReferenceType(DotNetUserType type)
	{
		visitElement(type);
	}

	public void visitUsingNamespaceStatement(CSharpUsingNamespaceStatementImpl statement)
	{
		visitElement(statement);
	}

	public void visitGenericParameter(DotNetGenericParameter parameter)
	{
		visitElement(parameter);
	}

	public void visitGenericParameterList(CSharpGenericParameterListImpl list)
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
		visitElement(declaration);
	}

	public void visitPropertyDeclaration(CSharpPropertyDeclaration declaration)
	{
		visitElement(declaration);
	}

	public void visitXXXAccessor(CSharpXXXAccessorImpl accessor)
	{
		visitElement(accessor);
	}

	public void visitFieldDeclaration(DotNetFieldDeclaration declaration)
	{
		visitElement(declaration);
	}

	public void visitPointerType(CSharpPointerTypeImpl type)
	{
		visitElement(type);
	}

	public void visitNullableType(CSharpNullableTypeImpl type)
	{
		visitElement(type);
	}

	public void visitNativeType(CSharpNativeTypeImpl type)
	{
		visitElement(type);
	}

	public void visitTypeWrapperWithTypeArguments(CSharpTypeWithTypeArgumentsImpl typeArguments)
	{
		visitElement(typeArguments);
	}

	public void visitArrayType(CSharpArrayTypeImpl type)
	{
		visitElement(type);
	}

	public void visitLocalVariable(CSharpLocalVariable variable)
	{
		visitElement(variable);
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

	public void visitAttributeList(CSharpAttributeListImpl list)
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

	public void visitPolyadicExpression(CSharpPolyadicExpressionImpl expression)
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

	public void visitLinqFrom(CSharpLinqFromImpl select)
	{
		visitElement(select);
	}

	public void visitLinqIn(CSharpLinqInImpl in)
	{
		visitElement(in);
	}

	public void visitLinqLet(CSharpLinqLetImpl let)
	{
		visitElement(let);
	}

	public void visitLinqWhere(CSharpLinqWhereImpl where)
	{
		visitElement(where);
	}

	public void visitLinqSelect(CSharpLinqSelectImpl select)
	{
		visitElement(select);
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
		visitElement(declaration);
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
		visitElement(parameter);
	}

	public void visitLambdaParameterList(CSharpLambdaParameterListImpl list)
	{
		visitElement(list);
	}

	public void visitAnonymMethod(CSharpAnonymMethodExpressionImpl method)
	{
		visitElement(method);
	}

	public void visitArrayInitializationExpression(CSharpArrayInitializationExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitTypeDefStatement(CSharpTypeDefStatementImpl statement)
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

	public void visitNamedCallArgument(CSharpNamedCallArgument argument)
	{
		visitElement(argument);
	}
}
