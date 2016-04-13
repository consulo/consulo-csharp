/*
 * Copyright 2013-2016 must-be.org
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.RequiredWriteAction;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpBinaryExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpOperatorReferenceImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpStaticTypeRef;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 13.04.2016
 */
public class CS0019 extends CompilerCheck<CSharpBinaryExpressionImpl>
{
	public static class ReplaceByEqualsCallFix extends BaseIntentionAction
	{
		private SmartPsiElementPointer<CSharpBinaryExpressionImpl> myElementPointer;

		public ReplaceByEqualsCallFix(CSharpBinaryExpressionImpl element)
		{
			myElementPointer = SmartPointerManager.getInstance(element.getProject()).createSmartPsiElementPointer(element);
			setText("Replace by 'Equals()' call");
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
			return myElementPointer.getElement() != null;
		}

		@Override
		@RequiredWriteAction
		public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
		{
			CSharpBinaryExpressionImpl element = myElementPointer.getElement();
			if(element == null)
			{
				return;
			}

			DotNetExpression leftExpression = element.getLeftExpression();
			DotNetExpression rightExpression = element.getRightExpression();
			if(leftExpression == null || rightExpression == null)
			{
				return;
			}

			StringBuilder builder = new StringBuilder();
			if(element.getOperatorElement().getOperatorElementType() == CSharpTokens.NTEQ)
			{
				builder.append("!");
			}
			builder.append(leftExpression.getText());
			builder.append(".Equals(");
			builder.append(rightExpression.getText());
			builder.append(")");

			DotNetExpression expression = CSharpFileFactory.createExpression(project, builder.toString());

			element.replace(expression);
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpBinaryExpressionImpl element)
	{
		CSharpOperatorReferenceImpl operatorElement = element.getOperatorElement();
		IElementType operatorElementType = operatorElement.getOperatorElementType();
		if(operatorElementType == CSharpTokens.EQEQ || operatorElementType == CSharpTokens.NTEQ)
		{
			DotNetExpression leftExpression = element.getLeftExpression();
			DotNetExpression rightExpression = element.getRightExpression();
			if(leftExpression == null || rightExpression == null)
			{
				return null;
			}

			DotNetTypeRef leftType = leftExpression.toTypeRef(true);
			DotNetTypeRef rightType = rightExpression.toTypeRef(true);

			boolean applicable = CSharpTypeUtil.isInheritable(leftType, rightType, element, CSharpStaticTypeRef.IMPLICIT).isSuccess() || CSharpTypeUtil.isInheritable(rightType, leftType, element,
					CSharpStaticTypeRef.IMPLICIT).isSuccess();
			if(!applicable)
			{
				return newBuilder(operatorElement, operatorElement.getCanonicalText(), formatTypeRef(leftType, element), formatTypeRef(rightType,
						element)).addQuickFix(new ReplaceByEqualsCallFix(element));
			}
		}
		return null;
	}
}
