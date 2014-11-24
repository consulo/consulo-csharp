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
import org.mustbe.consulo.csharp.ide.codeInsight.actions.RemoveModifierFix;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 18.11.14
 */
public class CS0500 extends CompilerCheck<CSharpMethodDeclaration>
{
	public static class RemoveMethodBody extends BaseIntentionAction
	{
		private SmartPsiElementPointer<DotNetLikeMethodDeclaration> myPointer;

		public RemoveMethodBody(DotNetLikeMethodDeclaration declaration)
		{
			myPointer = SmartPointerManager.getInstance(declaration.getProject()).createSmartPsiElementPointer(declaration);
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
			return "Remove method body";
		}

		@Override
		public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file)
		{
			return myPointer.getElement() != null;
		}

		@Override
		public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
		{
			DotNetLikeMethodDeclaration element = myPointer.getElement();
			if(element == null)
			{
				return;
			}
			PsiDocumentManager.getInstance(project).commitAllDocuments();

			PsiElement codeBlock = element.getCodeBlock();
			if(codeBlock != null)
			{
				codeBlock.delete();
				element.getNode().addLeaf(CSharpTokens.SEMICOLON, ";", null);
			}
		}
	}

	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpMethodDeclaration element)
	{
		PsiElement nameIdentifier = element.getNameIdentifier();
		if(nameIdentifier == null)
		{
			return null;
		}
		if((element.hasModifier(CSharpModifier.ABSTRACT) || element.isDelegate()) && element.getCodeBlock() != null)
		{
			CompilerCheckBuilder compilerCheckBuilder = newBuilder(nameIdentifier, formatElement(element));
			compilerCheckBuilder.addQuickFix(new RemoveMethodBody(element));
			if(element.hasModifier(CSharpModifier.ABSTRACT))
			{
				compilerCheckBuilder.addQuickFix(new RemoveModifierFix(CSharpModifier.ABSTRACT, element));
			}
			return compilerCheckBuilder;
		}
		return null;
	}
}
