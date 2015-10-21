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
import com.intellij.psi.PsiElementVisitor;

/**
 * @author VISTALL
 * @since 24.01.14
 */
public class CSharpPreprocessorElementVisitor extends PsiElementVisitor
{
	public void visitDefineDirective(CSharpPreprocessorDefineDirective define)
	{
		visitElement(define);
	}

	public void visitUndefDirective(CSharpPreprocessorUndefDirectiveImpl undefDirective)
	{
		visitElement(undefDirective);
	}

	public void visitOpenTag(CSharpPreprocessorOpenTagImpl start)
	{
		visitElement(start);
	}

	public void visitCloseTag(CSharpPreprocessorCloseTagImpl stop)
	{
		visitElement(stop);
	}

	public void visitRegionBlock(CSharpPreprocessorRegionBlockImpl block)
	{
		visitElement(block);
	}

	public void visitPrefixExpression(CSharpPreprocessorPrefixExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitPolyadicExpression(CSharpPreprocessorPolyadicExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitBinaryExpression(CSharpPreprocessorBinaryExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitReferenceExpression(CSharpPreprocessorReferenceExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitParenthesesExpression(CSharpPreprocessorParenthesesExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitConditionBlock(CSharpPreprocessorConditionImpl element)
	{
		visitElement(element);
	}

	public void visitIfElseBlock(CSharpPreprocessorIfElseBlockImpl element)
	{
		visitElement(element);
	}
}
