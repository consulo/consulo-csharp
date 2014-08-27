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
import org.mustbe.consulo.csharp.ide.codeInspection.CSharpInspectionBundle;
import org.mustbe.consulo.csharp.ide.projectView.CSharpElementTreeNode;
import org.mustbe.consulo.csharp.lang.psi.*;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImpl;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetAttribute;
import org.mustbe.consulo.dotnet.psi.DotNetAttributeUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetFieldDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
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
			public void visitTypeDeclaration(CSharpTypeDeclaration declaration)
			{
				process(holder, declaration.getNameIdentifier(), declaration);
			}

			@Override
			public void visitMethodDeclaration(CSharpMethodDeclaration declaration)
			{
				process(holder, declaration.getNameIdentifier(), declaration);
			}

			@Override
			public void visitFieldDeclaration(DotNetFieldDeclaration declaration)
			{
				process(holder, declaration.getNameIdentifier(), declaration);
			}

			@Override
			public void visitConstructorDeclaration(CSharpConstructorDeclaration declaration)
			{
				process(holder, declaration.getNameIdentifier(), declaration);
			}

			@Override
			public void visitEventDeclaration(CSharpEventDeclaration declaration)
			{
				process(holder, declaration.getNameIdentifier(), declaration);
			}

			@Override
			public void visitPropertyDeclaration(CSharpPropertyDeclaration declaration)
			{
				process(holder, declaration.getNameIdentifier(), declaration);
			}

			@Override
			public void visitParameter(DotNetParameter parameter)
			{
				process(holder, parameter.getNameIdentifier(), parameter);
			}

			@Override
			public void visitReferenceExpression(CSharpReferenceExpressionImpl expression)
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

	private static void process(@NotNull ProblemsHolder holder, @Nullable PsiElement range, @NotNull PsiElement target)
	{
		if(range == null)
		{
			return;
		}

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
			for(CSharpNamedCallArgument namedCallArgument : parameterList.getNamedArguments())
			{
				CSharpReferenceExpression argumentNameReference = namedCallArgument.getArgumentNameReference();
				if("Message".equals(argumentNameReference.getReferenceName()))
				{
					return stringValueOfExpression(namedCallArgument.getArgumentExpression());
				}
			}
		}
		else
		{
			DotNetExpression expression = expressions[0];
			return stringValueOfExpression(expression);
		}
		return null;
	}

	private static String stringValueOfExpression(DotNetExpression expression)
	{
		if(expression instanceof CSharpConstantExpressionImpl)
		{
			Object value = ((CSharpConstantExpressionImpl) expression).getValue();
			return value instanceof String ? (String) value : null;
		}
		return null;
	}
}
