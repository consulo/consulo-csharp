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

package org.mustbe.consulo.csharp.ide.codeInsight.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.refactoring.CSharpGenerateUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpBodyWithBraces;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetMemberOwner;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.BundleBase;
import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.codeStyle.CodeEditUtil;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import lombok.val;

/**
 * @author VISTALL
 * @since 30.12.14
 */
public abstract class CreateUnresolvedLikeMethodFix<CONTEXT extends BaseLikeMethodGenerateContext> extends BaseIntentionAction implements
		HighPriorityAction
{
	protected final SmartPsiElementPointer<CSharpReferenceExpression> myPointer;
	protected final String myReferenceName;

	public CreateUnresolvedLikeMethodFix(CSharpReferenceExpression expression)
	{
		myPointer = SmartPointerManager.getInstance(expression.getProject()).createSmartPsiElementPointer(expression);
		myReferenceName = expression.getReferenceName();
	}

	protected abstract CONTEXT createGenerateContext();

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file)
	{
		return createGenerateContext() != null;
	}

	@NotNull
	@Override
	public String getText()
	{
		String arguments = buildArgumentTypeRefs();
		if(arguments == null)
		{
			return "invalid";
		}
		return BundleBase.format(getTemplateText(), myReferenceName, buildArgumentTypeRefs());
	}

	@Nullable
	public String buildArgumentTypeRefs()
	{
		CSharpReferenceExpression element = myPointer.getElement();
		if(element == null)
		{
			return null;
		}

		StringBuilder builder = new StringBuilder();

		CSharpCallArgumentListOwner parent = PsiTreeUtil.getParentOfType(element, CSharpCallArgumentListOwner.class);

		assert parent != null;

		CSharpCallArgument[] callArguments = parent.getCallArguments();

		for(int i = 0; i < callArguments.length; i++)
		{
			if(i != 0)
			{
				builder.append(", ");
			}

			CSharpCallArgument callArgument = callArguments[i];

			DotNetExpression argumentExpression = callArgument.getArgumentExpression();
			if(argumentExpression != null)
			{
				DotNetTypeRef typeRef = argumentExpression.toTypeRef(false);
				builder.append(CSharpTypeRefPresentationUtil.buildShortText(typeRef, element));
			}
			else
			{
				builder.append("object");
			}
		}
		return builder.toString();
	}

	@NotNull
	public abstract CharSequence buildTemplateForAdd(@NotNull CONTEXT context, @NotNull PsiFile file);

	@NotNull
	public abstract String getTemplateText();

	@NotNull
	public PsiElement getElementForAfterAdd(@NotNull DotNetNamedElement[] elements, @NotNull CSharpBodyWithBraces targetForGenerate)
	{
		return ArrayUtil.getLastElement(elements);
	}

	@Override
	public void invoke(@NotNull Project project, final Editor editor, PsiFile file) throws IncorrectOperationException
	{
		val generateContext = createGenerateContext();
		if(generateContext == null)
		{
			return;
		}
		PsiDocumentManager.getInstance(project).commitAllDocuments();

		val templateForAdd = buildTemplateForAdd(generateContext, file);

		new WriteCommandAction.Simple<Object>(project, file)
		{
			@Override
			protected void run() throws Throwable
			{
				final DotNetLikeMethodDeclaration newMethod = CSharpFileFactory.createMethod(getProject(), templateForAdd);

				DotNetMemberOwner targetForGenerate = generateContext.getTargetForGenerate();

				assert targetForGenerate instanceof CSharpBodyWithBraces;

				DotNetNamedElement[] members = targetForGenerate.getMembers();
				if(members.length == 0)
				{

					CSharpGenerateUtil.normalizeBraces((CSharpBodyWithBraces) targetForGenerate);

					PsiElement leftBrace = ((CSharpBodyWithBraces) targetForGenerate).getLeftBrace();

					add(targetForGenerate, newMethod, leftBrace);
				}
				else
				{
					PsiElement lastElement = getElementForAfterAdd(members, (CSharpBodyWithBraces) targetForGenerate);

					add(targetForGenerate, newMethod, lastElement);
				}
			}

			private void add(DotNetMemberOwner targetForGenerate, DotNetLikeMethodDeclaration newMethod, PsiElement lastElement)
			{
				ASTNode node = lastElement.getNode();
				ASTNode treeNext = node.getTreeNext();

				targetForGenerate.getNode().addLeaf(CSharpTokens.WHITE_SPACE, "\n", treeNext);

				CodeEditUtil.setOldIndentation((TreeElement) newMethod.getNode(), 1);
				targetForGenerate.getNode().addChild(newMethod.getNode(), treeNext);

				PsiDocumentManager.getInstance(getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
				CodeStyleManager.getInstance(getProject()).reformat(newMethod);
			}
		}.execute();
	}

	@NotNull
	@Override
	public String getFamilyName()
	{
		return "C#";
	}
}
