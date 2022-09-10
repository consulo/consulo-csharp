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

import consulo.codeEditor.Editor;
import consulo.language.editor.intention.BaseIntentionAction;
import consulo.language.ast.ASTNode;
import consulo.language.impl.ast.TreeElement;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.SmartPointerManager;
import consulo.language.util.IncorrectOperationException;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.codeInsight.actions.MethodGenerateUtil;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.language.impl.psi.CodeEditUtil;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
			setText("Remove 'const' keyword");
		}

		@Nonnull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}

		@Override
		public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file)
		{
			return myVariablePointer.getElement() != null;
		}

		@Override
		public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
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

	public static class InitializeConstantFix extends BaseIntentionAction
	{
		private SmartPsiElementPointer<DotNetVariable> myVariablePointer;

		public InitializeConstantFix(DotNetVariable element)
		{
			myVariablePointer = SmartPointerManager.getInstance(element.getProject()).createSmartPsiElementPointer(element);
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
			return "Initialize constant";
		}

		@Override
		@RequiredUIAccess
		public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file)
		{
			return myVariablePointer.getElement() != null;
		}

		@Override
		@RequiredUIAccess
		public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
		{
			DotNetVariable element = myVariablePointer.getElement();
			if(element == null)
			{
				return;
			}

			PsiDocumentManager.getInstance(project).commitAllDocuments();

			String defaultValueForType = MethodGenerateUtil.getDefaultValueForType(element.toTypeRef(false));

			if(defaultValueForType == null)
			{
				return;
			}

			DotNetExpression expression = CSharpFileFactory.createExpression(project, defaultValueForType);

			PsiElement nameIdentifier = element.getNameIdentifier();
			if(nameIdentifier == null)
			{
				return;
			}

			ASTNode variableNode = element.getNode();

			ASTNode semicolon = variableNode.findChildByType(CSharpTokens.SEMICOLON);

			variableNode.addLeaf(CSharpTokens.WHITE_SPACE, " ", semicolon);
			variableNode.addLeaf(CSharpTokens.EQ, "=", semicolon);
			variableNode.addLeaf(CSharpTokens.WHITE_SPACE, " ", semicolon);
			ASTNode node = expression.getNode();
			CodeEditUtil.setOldIndentation((TreeElement) node, 0);
			variableNode.addChild(node, semicolon);

			editor.getCaretModel().moveToOffset(element.getTextRange().getEndOffset());
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull DotNetVariable element)
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
				// special case for void type
				if(DotNetTypeRefUtil.isVmQNameEqual(element.toTypeRef(false), DotNetTypes.System.Void))
				{
					return null;
				}
				return newBuilder(nameIdentifier).withQuickFix(new RemoveConstKeywordFix(element)).withQuickFix(new InitializeConstantFix(element));
			}
		}
		return null;
	}
}
