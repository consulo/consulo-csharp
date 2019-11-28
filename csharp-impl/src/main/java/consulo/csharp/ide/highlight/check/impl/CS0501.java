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
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.codeInsight.actions.AddModifierFix;
import consulo.csharp.ide.codeInsight.actions.MethodGenerateUtil;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.*;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 18.11.14
 */
public class CS0501 extends CompilerCheck<DotNetCodeBlockOwner>
{
	public static class CreateEmptyCodeBlockFix extends BaseIntentionAction
	{
		private SmartPsiElementPointer<DotNetCodeBlockOwner> myPointer;

		public CreateEmptyCodeBlockFix(DotNetCodeBlockOwner declaration)
		{
			myPointer = SmartPointerManager.getInstance(declaration.getProject()).createSmartPsiElementPointer(declaration);
		}

		@Nonnull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}

		@Nonnull
		@Override
		public String getText()
		{
			return "Create empty code block";
		}

		@Override
		public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file)
		{
			return myPointer.getElement() != null;
		}

		@Override
		public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
		{
			DotNetCodeBlockOwner element = myPointer.getElement();
			if(element == null)
			{
				return;
			}
			PsiDocumentManager.getInstance(project).commitAllDocuments();

			assert element instanceof CSharpSimpleLikeMethodAsElement;

			StringBuilder builder = new StringBuilder();
			builder.append("{\n");
			String defaultValueForType = MethodGenerateUtil.getDefaultValueForType(((CSharpSimpleLikeMethodAsElement) element).getReturnTypeRef(),
					element);
			if(defaultValueForType != null)
			{
				builder.append("return ").append(defaultValueForType).append(";\n");
			}
			builder.append("}");

			DotNetStatement statement = CSharpFileFactory.createStatement(element.getProject(), builder.toString());

			element.getCodeBlock().replace(statement);

			PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());
			PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());

			CodeStyleManager.getInstance(project).reformat(element);
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull DotNetCodeBlockOwner element)
	{
		if(element instanceof CSharpIndexMethodDeclaration)
		{
			return null;
		}

		PsiElement highlight = ((PsiNameIdentifierOwner) element).getNameIdentifier();

		if(highlight == null)
		{
			highlight = element;
		}

		CSharpCodeBodyProxy codeBlock = (CSharpCodeBodyProxy) element.getCodeBlock();
		if(codeBlock.isSemicolonOrEmpty() && !isAllowEmptyCodeBlock(element))
		{
			CompilerCheckBuilder result = newBuilder(highlight, formatElement(element));
			if(element instanceof CSharpConstructorDeclaration)
			{
				result.addQuickFix(new CreateEmptyCodeBlockFix(element));
			}
			else if(element instanceof CSharpMethodDeclaration && !(((CSharpMethodDeclaration) element).isDelegate()))
			{
				result.addQuickFix(new CreateEmptyCodeBlockFix(element));
				result.addQuickFix(new AddModifierFix(CSharpModifier.ABSTRACT, (DotNetModifierListOwner) element));
				result.addQuickFix(new AddModifierFix(CSharpModifier.EXTERN, (DotNetModifierListOwner) element));
				result.addQuickFix(new AddModifierFix(CSharpModifier.PARTIAL, (DotNetModifierListOwner) element));
			}
			else if(element instanceof DotNetXAccessor)
			{
				result.addQuickFix(new CreateEmptyCodeBlockFix(element));
			}
			return result;
		}
		return null;
	}


	@RequiredReadAction
	private boolean isAllowEmptyCodeBlock(DotNetCodeBlockOwner declaration)
	{
		DotNetModifierListOwner owner = (DotNetModifierListOwner) declaration;

		if(owner.hasModifier(DotNetModifier.ABSTRACT) || owner.hasModifier(CSharpModifier.PARTIAL) || owner.hasModifier(CSharpModifier.EXTERN))
		{
			return true;
		}

		if(owner instanceof DotNetXAccessor)
		{
			PsiElement parent = owner.getParent();
			if(parent instanceof CSharpPropertyDeclaration)
			{
				return CSharpPropertyUtil.isAutoProperty(parent);
			}
		}

		if(declaration instanceof CSharpMethodDeclaration)
		{
			if(((CSharpMethodDeclaration) declaration).isDelegate())
			{
				return true;
			}
		}
		return false;
	}
}
