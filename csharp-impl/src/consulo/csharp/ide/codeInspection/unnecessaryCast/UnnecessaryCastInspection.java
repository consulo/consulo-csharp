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

package consulo.csharp.ide.codeInspection.unnecessaryCast;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.impl.source.CSharpAsExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpTypeCastExpressionImpl;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;

/**
 * @author VISTALL
 * @since 18.05.14
 */
public class UnnecessaryCastInspection extends LocalInspectionTool
{
	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session)
	{
		return new CSharpElementVisitor()
		{
			@Override
			@RequiredReadAction
			public void visitTypeCastExpression(CSharpTypeCastExpressionImpl expression)
			{
				DotNetExpression innerExpression = expression.getInnerExpression();
				if(innerExpression == null)
				{
					return;
				}

				DotNetTypeRef innerType = innerExpression.toTypeRef(false);
				DotNetTypeRef castType = expression.toTypeRef(false);

				if(CSharpTypeUtil.isInheritable(innerType, castType, expression) && CSharpTypeUtil.isInheritable(castType, innerType, expression))
				{
					holder.registerProblem(expression.getType(), "Unnecessary cast", ProblemHighlightType.LIKE_UNUSED_SYMBOL);
				}
			}

			@Override
			@RequiredReadAction
			public void visitAsExpression(CSharpAsExpressionImpl expression)
			{
				DotNetExpression innerExpression = expression.getInnerExpression();
				if(innerExpression == null)
				{
					return;
				}

				DotNetType type = expression.getType();
				if(type == null)
				{
					return;
				}

				if(CSharpTypeUtil.isTypeEqual(innerExpression.toTypeRef(true), type.toTypeRef(), expression))
				{
					holder.registerProblem(expression.getAsKeyword(), "Unnecessary 'as' expression", ProblemHighlightType.LIKE_UNUSED_SYMBOL);
				}
			}
		};
	}
}