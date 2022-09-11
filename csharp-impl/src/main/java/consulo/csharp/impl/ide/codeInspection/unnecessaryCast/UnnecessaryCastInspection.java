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

package consulo.csharp.impl.ide.codeInspection.unnecessaryCast;

import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.CSharpAsExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpTypeCastExpressionImpl;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElementVisitor;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 18.05.14
 */
public abstract class UnnecessaryCastInspection extends LocalInspectionTool
{
	@Nonnull
	@Override
	public PsiElementVisitor buildVisitor(@Nonnull final ProblemsHolder holder, boolean isOnTheFly, @Nonnull LocalInspectionToolSession session)
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

				if(CSharpTypeUtil.isInheritable(innerType, castType) && CSharpTypeUtil.isInheritable(castType, innerType))
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

				if(CSharpTypeUtil.isTypeEqual(innerExpression.toTypeRef(true), type.toTypeRef()))
				{
					holder.registerProblem(expression.getAsKeyword(), "Unnecessary 'as' expression", ProblemHighlightType.LIKE_UNUSED_SYMBOL);
				}
			}
		};
	}
}
