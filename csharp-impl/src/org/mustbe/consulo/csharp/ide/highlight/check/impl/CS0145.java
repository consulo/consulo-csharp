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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 19.12.14
 */
public class CS0145 extends CompilerCheck<DotNetVariable>
{
	public static class RemoveConstKeywordFix extends BaseIntentionAction
	{
		private SmartPsiElementPointer<DotNetVariable> myVariablePointer;

		public RemoveConstKeywordFix(DotNetVariable element)
		{
			myVariablePointer = SmartPointerManager.getInstance(element.getProject()).createSmartPsiElementPointer(element);
		}

		@NotNull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}

		@NotNull
		@Override
		public String getText()
		{
			return "Remove 'const' keyword";
		}

		@Override
		public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file)
		{
			return myVariablePointer.getElement() != null;
		}

		@Override
		public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
		{
			DotNetVariable element = myVariablePointer.getElement();
			if(element == null)
			{
				return;
			}

			PsiDocumentManager.getInstance(project).commitAllDocuments();
			PsiElement constantKeywordElement = element.getConstantKeywordElement();
			if(constantKeywordElement == null)
			{
				return;
			}

			PsiElement nextSibling = constantKeywordElement.getNextSibling();

			constantKeywordElement.delete();
			if(nextSibling instanceof PsiWhiteSpace)
			{
				element.getNode().removeChild(nextSibling.getNode());
			}
		}
	}

	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull DotNetVariable element)
	{
		PsiElement constantKeywordElement = element.getConstantKeywordElement();
		if(constantKeywordElement != null)
		{
			PsiElement nameIdentifier = element.getNameIdentifier();
			if(nameIdentifier == null)
			{
				return null;
			}
			if(element.getInitializer() == null)
			{
				return newBuilder(nameIdentifier).addQuickFix(new RemoveConstKeywordFix(element));
			}
		}
		return null;
	}
}
