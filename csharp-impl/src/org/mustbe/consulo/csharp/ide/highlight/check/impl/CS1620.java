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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;

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

		@NotNull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}

		@Override
		public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file)
		{
			return myPointer.getElement() != null;
		}

		@Override
		public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
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
	@NotNull
	@Override
	public List<CompilerCheckBuilder> check(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpMethodCallExpressionImpl element)
	{
		ResolveResult resolveResult = CSharpResolveUtil.findFirstValidResult(element.multiResolve(true));
		if(!(resolveResult instanceof MethodResolveResult))
		{
			return Collections.emptyList();
		}

		MethodCalcResult calcResult = ((MethodResolveResult) resolveResult).getCalcResult();

		List<CompilerCheckBuilder> results = new SmartList<CompilerCheckBuilder>();

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

			CSharpRefTypeRef.Type type = null;

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
			if(!(typeRef instanceof CSharpRefTypeRef) || ((CSharpRefTypeRef) typeRef).getType() != type)
			{
				results.add(newBuilder(argumentExpression, String.valueOf(parameter.getIndex() + 1), type.name()).addQuickFix(new BaseUseTypeFix
						(argumentExpression, type)));
			}
			return results;
		}
		return Collections.emptyList();
	}
}
