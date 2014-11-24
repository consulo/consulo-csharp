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
import org.mustbe.consulo.csharp.ide.codeInsight.actions.AddModifierFix;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.MethodGenerateUtil;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 18.11.14
 */
public class CS0501 extends CompilerCheck<DotNetLikeMethodDeclaration>
{
	public static class CreateEmptyBoxFix extends BaseIntentionAction
	{
		private SmartPsiElementPointer<DotNetLikeMethodDeclaration> myPointer;

		public CreateEmptyBoxFix(DotNetLikeMethodDeclaration declaration)
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
			return "Create empty method body";
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

			StringBuilder builder = new StringBuilder();
			builder.append("{\n");
			String defaultValueForType = MethodGenerateUtil.getDefaultValueForType(element.getReturnTypeRef(), element);
			if(defaultValueForType != null)
			{
				builder.append("return ").append(defaultValueForType).append(";\n");
			}
			builder.append("}");

			DotNetStatement statement = CSharpFileFactory.createStatement(element.getProject(), builder.toString());

			ASTNode node = element.getNode();
			ASTNode childByType = node.findChildByType(CSharpTokens.SEMICOLON);
			if(childByType != null)
			{
				childByType.getPsi().replace(statement);
			}
		}
	}

	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull DotNetLikeMethodDeclaration element)
	{
		if(element instanceof CSharpArrayMethodDeclaration)
		{
			return null;
		}

		PsiElement highlight = null;
		if(element instanceof CSharpConversionMethodDeclaration)
		{
			highlight = ((CSharpConversionMethodDeclaration) element).getConversionType();
		}
		else
		{
			highlight = ((PsiNameIdentifierOwner) element).getNameIdentifier();
		}

		if(highlight == null)
		{
			highlight = element;
		}

		PsiElement codeBlock = element.getCodeBlock();
		if(codeBlock == null && !isAllowEmptyCodeBlock(element))
		{
			CompilerCheckBuilder result = newBuilder(highlight, formatElement(element));
			if(element instanceof CSharpConstructorDeclaration)
			{
				result.addQuickFix(new CreateEmptyBoxFix(element));
			}
			else if(element instanceof CSharpMethodDeclaration && !(((CSharpMethodDeclaration) element).isDelegate()))
			{
				result.addQuickFix(new CreateEmptyBoxFix(element));
				result.addQuickFix(new AddModifierFix(CSharpModifier.ABSTRACT, element));
				//TODO [VISTALL] result.addQuickFix(new AddModifierFix(CSharpModifier.EXTERN, element));
				result.addQuickFix(new AddModifierFix(CSharpModifier.PARTIAL, element));
			}
			return result;
		}
		return null;
	}

	private boolean isAllowEmptyCodeBlock(DotNetLikeMethodDeclaration declaration)
	{
		if(declaration instanceof CSharpMethodDeclaration)
		{
			if(((CSharpMethodDeclaration) declaration).isDelegate())
			{
				return true;
			}

			if(declaration.hasModifier(DotNetModifier.ABSTRACT) || declaration.hasModifier(CSharpModifier.PARTIAL) || declaration.hasModifier
					(CSharpModifier.EXTERN))
			{
				return true;
			}
		}
		return false;
	}
}
