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
public class CSharpMacroElementVisitor extends PsiElementVisitor
{
	public void visitMacroDefine(CSharpMacroDefine cSharpMacroDefine)
	{
		visitElement(cSharpMacroDefine);
	}

	public void visitMacroBlockStart(CSharpMacroBlockStartImpl start)
	{
		visitElement(start);
	}

	public void visitMacroBlockStop(CSharpMacroBlockStopImpl stop)
	{
		visitElement(stop);
	}

	public void visitMacroBlock(CSharpMacroBlockImpl block)
	{
		visitElement(block);
	}

	public void visitPrefixExpression(CSharpMacroPrefixExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitPolyadicExpression(CSharpMacroPolyadicExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitBinaryExpression(CSharpMacroBinaryExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitReferenceExpression(CSharpMacroReferenceExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitParenthesesExpression(CSharpMacroParenthesesExpressionImpl expression)
	{
		visitElement(expression);
	}

	public void visitMacroIf(CSharpMacroIfImpl element)
	{
		visitElement(element);
	}

	public void visitMacroIfConditionBlock(CSharpMacroIfConditionBlockImpl element)
	{
		visitElement(element);
	}
}
