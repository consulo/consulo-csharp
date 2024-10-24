/*
 * Copyright 2013-2020 consulo.io
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

package consulo.csharp.impl.ide.codeInspection;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.source.CSharpBlockStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpExpressionStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpReturnStatementImpl;
import consulo.csharp.lang.psi.CSharpCodeBodyProxy;
import consulo.document.util.TextRange;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetStatement;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.util.lang.Couple;
import org.jetbrains.annotations.Nls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-06-29
 */
@ExtensionImpl
public class LambdaBlockStatementCanReplacedByExpressionInspection extends CSharpGeneralLocalInspection
{
	private static class ReplaceStatementByExpressionFix extends LocalQuickFixOnPsiElement
	{
		private ReplaceStatementByExpressionFix(@Nonnull PsiElement element)
		{
			super(element);
		}

		@Nonnull
		@Override
		public String getText()
		{
			return "Replace statement by expression";
		}

		@Override
		@RequiredReadAction
		public void invoke(@Nonnull Project project, @Nonnull PsiFile psiFile, @Nonnull PsiElement lambdaTemp, @Nonnull PsiElement unused)
		{
			CSharpLambdaExpressionImpl expression = (CSharpLambdaExpressionImpl) lambdaTemp;

			Couple<PsiElement> removingInfo = getRemovingInfo(expression);
			if(removingInfo == null)
			{
				return;
			}

			PsiElement newBody = removingInfo.getSecond();

			PsiElement copied = newBody.copy();

			removingInfo.getFirst().replace(copied);
		}

		@RequiredReadAction
		@Nullable
		private Couple<PsiElement> getRemovingInfo(CSharpLambdaExpressionImpl expression)
		{
			CSharpCodeBodyProxy codeBlock = expression.getCodeBlock();

			PsiElement element = codeBlock.getElement();

			if(element instanceof CSharpBlockStatementImpl)
			{
				DotNetStatement[] statements = ((CSharpBlockStatementImpl) element).getStatements();

				if(statements.length == 1)
				{
					DotNetStatement statement = statements[0];

					if(statement instanceof CSharpReturnStatementImpl)
					{
						DotNetExpression returnExpression = ((CSharpReturnStatementImpl) statement).getExpression();
						if(returnExpression == null)
						{
							return null;
						}

						return Couple.of(element, returnExpression);
					}
					else if(statement instanceof CSharpExpressionStatementImpl)
					{
						DotNetExpression innerExpression = ((CSharpExpressionStatementImpl) statement).getExpression();
						return Couple.of(element, innerExpression);
					}
				}
			}

			return null;
		}

		@Nls
		@Nonnull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}
	}

	@Nonnull
	@Override
	public PsiElementVisitor buildVisitor(@Nonnull ProblemsHolder holder, boolean isOnTheFly)
	{
		return new CSharpElementVisitor()
		{
			@Override
			@RequiredReadAction
			public void visitElement(PsiElement element)
			{
				// only return and expression statement
				if(element instanceof CSharpExpressionStatementImpl || element instanceof CSharpReturnStatementImpl)
				{
					PsiElement maybeBlockStatement = element.getParent();
					if(maybeBlockStatement instanceof CSharpBlockStatementImpl)
					{
						DotNetStatement[] statements = ((CSharpBlockStatementImpl) maybeBlockStatement).getStatements();
						// accept only if one statement in block
						if(statements.length != 1)
						{
							return;
						}

						PsiElement maybeLambda = maybeBlockStatement.getParent();
						if(maybeLambda instanceof CSharpLambdaExpressionImpl)
						{
							if(element instanceof CSharpExpressionStatementImpl)
							{
								report(((CSharpBlockStatementImpl) maybeBlockStatement).getLeftBrace(), (CSharpLambdaExpressionImpl) maybeLambda, holder);
								report(((CSharpBlockStatementImpl) maybeBlockStatement).getRightBrace(), (CSharpLambdaExpressionImpl) maybeLambda, holder);
							}
							else if(element instanceof CSharpReturnStatementImpl)
							{
								DotNetExpression expression = ((CSharpReturnStatementImpl) element).getExpression();
								if(expression == null)
								{
									return;
								}

								report(((CSharpReturnStatementImpl) element).getReturnKeyword(), (CSharpLambdaExpressionImpl) maybeLambda, holder);
							}
						}
					}
				}
			}
		};
	}

	@RequiredReadAction
	private void report(@Nonnull PsiElement elementToHighlight, @Nonnull CSharpLambdaExpressionImpl lambdaExpression, ProblemsHolder holder)
	{
		int textLength = elementToHighlight.getTextLength();

		holder.registerProblem(elementToHighlight, "Statement body can be replaced by expression body", ProblemHighlightType.LIKE_UNUSED_SYMBOL, new TextRange(0, textLength), new
				ReplaceStatementByExpressionFix(lambdaExpression));
	}

	@Nonnull
	@Override
	public String getDisplayName()
	{
		return "Lambda statement can replaced by expression";
	}

	@Nonnull
	@Override
	public HighlightDisplayLevel getDefaultLevel()
	{
		return HighlightDisplayLevel.WARNING;
	}
}
