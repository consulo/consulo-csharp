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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.codeInsight.actions.AddModifierFix;
import consulo.csharp.ide.codeInsight.actions.MethodGenerateUtil;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpPropertyUtil;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetCodeBlockOwner;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetStatement;
import consulo.dotnet.psi.DotNetXXXAccessor;
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
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;

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
			return "Create empty code block";
		}

		@Override
		public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file)
		{
			return myPointer.getElement() != null;
		}

		@Override
		public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
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

			ASTNode node = element.getNode();
			ASTNode childByType = node.findChildByType(CSharpTokens.SEMICOLON);
			if(childByType != null)
			{
				childByType.getPsi().replace(statement);
			}

			PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());
			PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());

			CodeStyleManager.getInstance(project).reformat(element);
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull DotNetCodeBlockOwner element)
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

		PsiElement codeBlock = element.getCodeBlock();
		if(codeBlock == null && !isAllowEmptyCodeBlock(element))
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
			else if(element instanceof DotNetXXXAccessor)
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

		if(owner instanceof DotNetXXXAccessor)
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
