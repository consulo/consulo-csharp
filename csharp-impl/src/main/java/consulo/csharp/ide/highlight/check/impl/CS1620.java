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

package consulo.csharp.ide.highlight.check.impl;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpOutRefAutoTypeRef;
import consulo.csharp.lang.psi.impl.source.CSharpOutRefVariableExpressionImpl;
import consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodResolvePriorityInfo;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.resolve.DotNetTypeRef;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 01.07.14
 */
public class CS1620 extends CompilerCheck<CSharpMethodCallExpressionImpl>
{
	public static class BaseUseTypeFix extends BaseIntentionAction
	{
		private final CSharpRefTypeRef.Type myType;
		private final SmartPsiElementPointer<DotNetExpression> myPointer;

		public BaseUseTypeFix(DotNetExpression expression, CSharpRefTypeRef.Type type)
		{
			myType = type;
			myPointer = SmartPointerManager.getInstance(expression.getProject()).createSmartPsiElementPointer(expression);

			setText("Wrap with '" + myType + "'");
		}

		@Nonnull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}

		@Override
		public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file)
		{
			return myPointer.getElement() != null;
		}

		@Override
		public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
		{
			DotNetExpression element = myPointer.getElement();
			if(element == null)
			{
				return;
			}

			DotNetExpression expression = CSharpFileFactory.createExpression(project, myType.name() + " " + element.getText());

			element.replace(expression);
		}
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public List<CompilerCheckBuilder> check(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpMethodCallExpressionImpl element)
	{
		ResolveResult resolveResult = CSharpResolveUtil.findFirstValidResult(element.multiResolve(true));
		if(!(resolveResult instanceof MethodResolveResult))
		{
			return Collections.emptyList();
		}

		MethodResolvePriorityInfo calcResult = ((MethodResolveResult) resolveResult).getCalcResult();

		List<CompilerCheckBuilder> results = new SmartList<>();

		List<NCallArgument> arguments = calcResult.getArguments();
		for(NCallArgument argument : arguments)
		{
			CSharpCallArgument callArgument = argument.getCallArgument();
			if(callArgument == null)
			{
				continue;
			}

			DotNetExpression argumentExpression = callArgument.getArgumentExpression();
			if(argumentExpression == null)
			{
				continue;
			}
			PsiElement parameterElement = argument.getParameterElement();
			if(!(parameterElement instanceof DotNetParameter))
			{
				continue;
			}

			CSharpRefTypeRef.Type type;

			DotNetParameter parameter = (DotNetParameter) parameterElement;
			if(parameter.hasModifier(CSharpModifier.REF))
			{
				type = CSharpRefTypeRef.Type.ref;
			}
			else if(parameter.hasModifier(CSharpModifier.OUT))
			{
				type = CSharpRefTypeRef.Type.out;
			}
			else
			{
				continue;
			}

			DotNetTypeRef typeRef = argument.getTypeRef();
			if(typeRef instanceof CSharpOutRefAutoTypeRef && ((CSharpOutRefAutoTypeRef) typeRef).getType() == type)
			{
				continue;
			}

			if(argumentExpression instanceof CSharpOutRefVariableExpressionImpl)
			{
				continue;
			}

			if(!(typeRef instanceof CSharpRefTypeRef) || ((CSharpRefTypeRef) typeRef).getType() != type)
			{
				results.add(newBuilder(argumentExpression, String.valueOf(parameter.getIndex() + 1), type.name()).addQuickFix(new BaseUseTypeFix(argumentExpression, type)));
			}
			return results;
		}
		return Collections.emptyList();
	}
}
