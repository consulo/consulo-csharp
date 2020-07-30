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

import com.intellij.psi.PsiElementVisitor;
import consulo.csharp.lang.psi.impl.source.*;

/**
 * @author VISTALL
 * @since 24.01.14
 */
public class CSharpMacroElementVisitor extends PsiElementVisitor
{
	public void visitMacroDefine(CSharpPreprocessorDefine cSharpMacroDefine)
	{
		visitElement(cSharpMacroDefine);
	}

	public void visitMacroBlockStop(CSharpPreprocessorBlockStopImpl stop)
	{
		visitElement(stop);
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

	public void visitMacroIf(CSharpPreprocessorIfImpl element)
	{
		visitElement(element);
	}

	public void visitRegionDirective(CSharpPreprocessorRegionImpl element)
	{
		visitElement(element);
	}

	public void visitEndRegionDirective(CSharpPreprocessorEndRegionImpl element)
	{
		visitElement(element);
	}

	public void visitPragma(CSharpPreprocessorPragmaImpl element)
	{
		visitElement(element);
	}

	public void visitNullable(CSharpPreprocessorNullableImpl element)
	{
		visitElement(element);
	}

	public void visitWarning(CSharpPreprocessorWarningImpl element)
	{
		visitElement(element);
	}

	public void visitError(CSharpPreprocessorErrorImpl element)
	{
		visitElement(element);
	}
}
