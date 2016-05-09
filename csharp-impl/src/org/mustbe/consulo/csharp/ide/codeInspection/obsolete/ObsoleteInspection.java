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

package org.mustbe.consulo.csharp.ide.codeInspection.obsolete;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.codeInspection.CSharpInspectionBundle;
import org.mustbe.consulo.csharp.ide.projectView.CSharpElementTreeNode;
import org.mustbe.consulo.csharp.lang.evaluator.ConstantExpressionEvaluator;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttribute;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldOrPropertySet;
import org.mustbe.consulo.csharp.lang.psi.CSharpNamedFieldOrPropertySet;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetAttribute;
import org.mustbe.consulo.dotnet.psi.DotNetAttributeUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiNamedElement;

/**
 * @author VISTALL
 * @since 28.08.14
 */
public class ObsoleteInspection extends LocalInspectionTool
{
	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly)
	{
		return new CSharpElementVisitor()
		{
			@Override
			@RequiredReadAction
			public void visitReferenceExpression(CSharpReferenceExpression expression)
			{
				PsiElement resolve = expression.resolve();
				if(resolve == null)
				{
					return;
				}

				PsiElement referenceElement = expression.getReferenceElement();
				if(referenceElement == null)
				{
					return;
				}
				process(holder, referenceElement, resolve);
			}
		};
	}

	@RequiredReadAction
	private static void process(@NotNull ProblemsHolder holder, @Nullable PsiElement range, @NotNull PsiElement target)
	{
		if(range == null)
		{
			return;
		}

		// #hasAttribute() is cache result, #findAttribute() not
		if(DotNetAttributeUtil.hasAttribute(target, DotNetTypes.System.ObsoleteAttribute))
		{
			DotNetAttribute attribute = DotNetAttributeUtil.findAttribute(target, DotNetTypes.System.ObsoleteAttribute);
			if(attribute == null)
			{
				return;
			}
			String message = getMessage(attribute);
			if(message == null)
			{
				message = CSharpInspectionBundle.message("target.is.obsolete", CSharpElementTreeNode.getPresentableText((PsiNamedElement) target));
			}

			holder.registerProblem(range, message, ProblemHighlightType.LIKE_DEPRECATED);
		}
	}

	public static String getMessage(DotNetAttribute attribute)
	{
		if(!(attribute instanceof CSharpAttribute))
		{
			return null;
		}

		CSharpCallArgumentList parameterList = ((CSharpAttribute) attribute).getParameterList();
		if(parameterList == null)
		{
			return null;
		}

		DotNetExpression[] expressions = parameterList.getExpressions();
		if(expressions.length == 0)
		{
			for(CSharpFieldOrPropertySet namedCallArgument : parameterList.getSets())
			{
				if(namedCallArgument instanceof CSharpNamedFieldOrPropertySet)
				{
					CSharpReferenceExpression argumentNameReference = (CSharpReferenceExpression) namedCallArgument.getNameElement();
					if("Message".equals(argumentNameReference.getReferenceName()))
					{
						return new ConstantExpressionEvaluator(namedCallArgument.getValueExpression()).getValueAs(String.class);
					}
				}
			}
		}
		else
		{
			DotNetExpression expression = expressions[0];
			return new ConstantExpressionEvaluator(expression).getValueAs(String.class);
		}
		return null;
	}
}
