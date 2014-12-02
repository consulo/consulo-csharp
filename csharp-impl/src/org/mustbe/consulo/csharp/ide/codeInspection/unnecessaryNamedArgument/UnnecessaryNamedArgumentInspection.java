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

package org.mustbe.consulo.csharp.ide.codeInspection.unnecessaryNamedArgument;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpNamedCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFixOnPsiElement;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 02.12.14
 */
public class UnnecessaryNamedArgumentInspection extends LocalInspectionTool
{
	private static class ConvertToSimpleArgument extends LocalQuickFixOnPsiElement
	{
		ConvertToSimpleArgument(@NotNull PsiElement element)
		{
			super(element);
		}

		@NotNull
		@Override
		public String getText()
		{
			return "Convert to simple argument";
		}

		@NotNull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}

		@Override
		public void invoke(@NotNull final Project project, @NotNull PsiFile psiFile, @NotNull final PsiElement element, @NotNull PsiElement element2)
		{
			new WriteCommandAction.Simple<Object>(project, psiFile)
			{
				@Override
				protected void run() throws Throwable
				{
					DotNetExpression argumentExpression = ((CSharpNamedCallArgument) element).getArgumentExpression();

					assert argumentExpression != null;

					CSharpMethodCallExpressionImpl expression = (CSharpMethodCallExpressionImpl) CSharpFileFactory.createExpression(project,
							"test(" + argumentExpression.getText() + ")");

					CSharpCallArgument callArgument = expression.getCallArguments()[0];

					element.replace(callArgument);
				}
			}.execute();
		}
	}

	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly)
	{
		return new CSharpElementVisitor()
		{
			@Override
			public void visitNamedCallArgument(final CSharpNamedCallArgument argument)
			{
				DotNetExpression argumentExpression = argument.getArgumentExpression();
				if(argumentExpression == null)
				{
					return;
				}
				CSharpCallArgumentListOwner owner = PsiTreeUtil.getParentOfType(argument, CSharpCallArgumentListOwner.class);

				assert owner != null;

				ResolveResult result = CSharpResolveUtil.findValidOrFirstMaybeResult(owner.multiResolve(false));

				if(!(result instanceof MethodResolveResult))
				{
					return;
				}

				List<NCallArgument> arguments = ((MethodResolveResult) result).getCalcResult().getArguments();

				NCallArgument nCallArgument = ContainerUtil.find(arguments, new Condition<NCallArgument>()
				{
					@Override
					public boolean value(NCallArgument nCallArgument)
					{
						return nCallArgument.getCallArgument() == argument;
					}
				});

				if(nCallArgument == null)
				{
					return;
				}

				PsiElement parameterElement = nCallArgument.getParameterElement();
				if(!(parameterElement instanceof DotNetParameter))
				{
					return;
				}

				int positionInParameterList = ((DotNetParameter) parameterElement).getIndex();

				int positionInCall = arguments.indexOf(nCallArgument);
				assert positionInCall != -1;

				if(positionInCall == positionInParameterList)
				{
					CSharpReferenceExpression argumentNameReference = argument.getArgumentNameReference();
					holder.registerProblem(argumentNameReference, "Unnecessary argument name specific", ProblemHighlightType.LIKE_UNUSED_SYMBOL,
							new ConvertToSimpleArgument(argument));
				}
			}
		};
	}
}
