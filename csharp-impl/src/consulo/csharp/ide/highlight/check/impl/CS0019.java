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

import java.util.Arrays;
import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.impl.source.CSharpBinaryExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpOperatorReferenceImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.MultiMap;
import consulo.annotations.RequiredReadAction;
import consulo.annotations.RequiredWriteAction;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetTypeRef;

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

	private static MultiMap<String, String> ourAllowedMap = new MultiMap<String, String>();

	static
	{
		String[] values = {
				DotNetTypes.System.SByte,
				DotNetTypes.System.Byte,
				DotNetTypes.System.Int16,
				DotNetTypes.System.UInt16,
				DotNetTypes.System.Int32,
				DotNetTypes.System.UInt32,
				DotNetTypes.System.Int64,
				DotNetTypes.System.UInt64
		};

		for(String value : values)
		{
			ourAllowedMap.put(value, Arrays.asList(values));
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpBinaryExpressionImpl element)
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

			boolean applicable = CSharpTypeUtil.isInheritableWithImplicit(leftType, rightType, element) || CSharpTypeUtil.isInheritableWithImplicit(rightType, leftType, element);

			if(!applicable)
			{
				Pair<String, DotNetTypeDeclaration> leftPair = CSharpTypeUtil.resolveTypeElement(leftType);
				if(leftPair != null)
				{
					Collection<String> allowedSetLeft = ourAllowedMap.get(leftPair.getFirst());

					Pair<String, DotNetTypeDeclaration> rightPair = CSharpTypeUtil.resolveTypeElement(rightType);
					if(rightPair != null && allowedSetLeft.contains(rightPair.getFirst()))
					{
						return null;
					}
				}

				return newBuilder(operatorElement, operatorElement.getCanonicalText(), formatTypeRef(leftType, element), formatTypeRef(rightType,
						element)).addQuickFix(new ReplaceByEqualsCallFix(element));
			}
		}
		return null;
	}
}
