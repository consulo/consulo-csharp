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
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpContextUtil;
import consulo.csharp.lang.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.CSharpReferenceExpressionEx;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetModifierListOwner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 31.12.14
 */
public class CS0120 extends CompilerCheck<CSharpReferenceExpressionEx>
{
	public static class ReplaceQualifierByTypeFix extends BaseIntentionAction
	{
		private SmartPsiElementPointer<CSharpReferenceExpressionEx> myReferenceExpressionPointer;

		public ReplaceQualifierByTypeFix(CSharpReferenceExpressionEx referenceExpressionEx)
		{
			myReferenceExpressionPointer = SmartPointerManager.getInstance(referenceExpressionEx.getProject()).createSmartPsiElementPointer(referenceExpressionEx);
		}

		@Nonnull
		@Override
		@RequiredReadAction
		public String getText()
		{
			CSharpReferenceExpressionEx element = myReferenceExpressionPointer.getElement();
			if(element == null)
			{
				throw new IllegalArgumentException();
			}

			PsiElement resolvedElement = element.resolve();
			if(resolvedElement == null)
			{
				return "";
			}
			return "Replace qualifier by '" + formatElement(resolvedElement.getParent()) + "'";
		}

		@Nonnull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}

		@Override
		@RequiredReadAction
		public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file)
		{
			CSharpReferenceExpressionEx element = myReferenceExpressionPointer.getElement();
			return element != null && element.resolve() != null && element.getQualifier() != null;
		}

		@Override
		@RequiredWriteAction
		public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
		{
			CSharpReferenceExpressionEx element = myReferenceExpressionPointer.getElement();
			if(element == null)
			{
				return;
			}

			PsiElement qualifier = element.getQualifier();
			if(qualifier == null)
			{
				return;
			}
			PsiElement resolvedElement = element.resolve();
			if(resolvedElement == null)
			{
				return;
			}

			PsiElement parent = resolvedElement.getParent();

			assert parent instanceof PsiNameIdentifierOwner;

			DotNetExpression expression = CSharpFileFactory.createExpression(project, ((PsiNameIdentifierOwner) parent).getName());

			qualifier.replace(expression);
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpReferenceExpressionEx element)
	{
		PsiElement referenceElement = element.getReferenceElement();
		if(referenceElement == null)
		{
			return null;
		}

		PsiElement resolvedElement = element.resolve();
		if(!(resolvedElement instanceof DotNetModifierListOwner))
		{
			return null;
		}

		CSharpContextUtil.ContextType parentContextType = CSharpContextUtil.getParentContextTypeForReference(element);

		CSharpContextUtil.ContextType contextForResolved = CSharpContextUtil.getContextForResolved(resolvedElement);
		if(parentContextType == CSharpContextUtil.ContextType.STATIC && contextForResolved.isAllowInstance())
		{
			return newBuilder(referenceElement, formatElement(resolvedElement));
		}
		else if(contextForResolved == CSharpContextUtil.ContextType.STATIC && !parentContextType.isAllowStatic())
		{
			return newBuilderImpl(CS0176.class, referenceElement, formatElement(resolvedElement)).addQuickFix(new ReplaceQualifierByTypeFix(element));
		}
		return null;
	}
}