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

package consulo.csharp.impl.ide.codeInspection.unnecessaryNamedArgument;

import consulo.csharp.impl.ide.codeInsight.actions.ConvertNamedToSimpleArgumentFix;
import consulo.csharp.impl.ide.highlight.check.impl.CS1738;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.source.resolve.MethodResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.arguments.NCallArgument;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import consulo.csharp.lang.psi.CSharpNamedCallArgument;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetParameter;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.ContainerUtil;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author VISTALL
 * @since 02.12.14
 */
public abstract class UnnecessaryNamedArgumentInspection extends LocalInspectionTool
{
	@Nonnull
	@Override
	public PsiElementVisitor buildVisitor(@Nonnull final ProblemsHolder holder, boolean isOnTheFly)
	{
		return new CSharpElementVisitor()
		{
			@Override
			public void visitNamedCallArgument(final CSharpNamedCallArgument argument)
			{
				DotNetExpression argumentExpression = argument.getArgumentExpression();
				if(argumentExpression == null || CS1738.argumentIsInWrongPosition(argument))
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

				NCallArgument nCallArgument = ContainerUtil.find(arguments, nCallArgument1 -> nCallArgument1.getCallArgument() == argument);

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
							new consulo.ide.impl.idea.codeInspection.IntentionWrapper(new ConvertNamedToSimpleArgumentFix(argument), owner.getContainingFile()));
				}
			}
		};
	}
}
