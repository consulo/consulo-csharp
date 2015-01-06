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

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.refactoring.CSharpGenerateUtil;
import org.mustbe.consulo.csharp.ide.refactoring.util.CSharpNameSuggesterUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpBodyWithBraces;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpNamedCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetMemberOwner;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.BundleBase;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.ConstantNode;
import com.intellij.ide.IdeBundle;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.ex.FileEditorProviderManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 30.12.14
 */
public abstract class CreateUnresolvedLikeMethodFix<CONTEXT extends BaseLikeMethodGenerateContext> extends BaseIntentionAction
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

	protected void buildParameterList(@NotNull CONTEXT context, @NotNull PsiFile file, @NotNull Template template)
	{
		template.addTextSegment("(");

		CSharpCallArgumentListOwner parent = PsiTreeUtil.getParentOfType(context.getExpression(), CSharpCallArgumentListOwner.class);

		assert parent != null;

		CSharpCallArgument[] callArguments = parent.getCallArguments();

		for(int i = 0; i < callArguments.length; i++)
		{
			if(i != 0)
			{
				template.addTextSegment(", ");
			}

			CSharpCallArgument callArgument = callArguments[i];

			DotNetTypeRef parameterTypeRef = new CSharpTypeRefByQName(DotNetTypes.System.Object);
			DotNetExpression argumentExpression = callArgument.getArgumentExpression();
			if(argumentExpression != null)
			{
				parameterTypeRef = argumentExpression.toTypeRef(false);
			}

			template.addVariable(new ConstantNode(CSharpTypeRefPresentationUtil.buildShortText(parameterTypeRef, context.getExpression())), true);

			template.addTextSegment(" ");
			if(callArgument instanceof CSharpNamedCallArgument)
			{
				template.addTextSegment(((CSharpNamedCallArgument) callArgument).getName());
			}
			else
			{
				Collection<String> suggestedNames = CSharpNameSuggesterUtil.getSuggestedNames(argumentExpression);
				String item = ContainerUtil.getFirstItem(suggestedNames);
				template.addVariable(new ConstantNode(item + i), true);
			}
		}
		template.addTextSegment(")");
	}

	public abstract void buildTemplate(@NotNull CONTEXT context, @NotNull PsiFile file, @NotNull Template template);

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
		PsiDocumentManager.getInstance(project).commitAllDocuments();
		val generateContext = createGenerateContext();
		if(generateContext == null)
		{
			return;
		}

		val templateManager = TemplateManager.getInstance(project);
		val template = templateManager.createTemplate("", "");
		template.setToReformat(true);

		template.addTextSegment("\n\n");
		buildTemplate(generateContext, file, template);

		new WriteCommandAction.Simple<Object>(project, file)
		{
			@Override
			protected void run() throws Throwable
			{
				DotNetMemberOwner targetForGenerate = generateContext.getTargetForGenerate();

				assert targetForGenerate instanceof CSharpBodyWithBraces;

				Editor editorForAdd;
				DotNetNamedElement[] members = targetForGenerate.getMembers();
				if(members.length == 0)
				{
					CSharpGenerateUtil.normalizeBraces((CSharpBodyWithBraces) targetForGenerate);

					PsiElement leftBrace = ((CSharpBodyWithBraces) targetForGenerate).getLeftBrace();

					editorForAdd = openEditor(leftBrace, leftBrace.getTextOffset() + 1);
				}
				else
				{
					PsiElement lastElement = getElementForAfterAdd(members, (CSharpBodyWithBraces) targetForGenerate);

					editorForAdd = openEditor(lastElement, lastElement.getTextRange().getEndOffset());
				}

				if(editorForAdd == null)
				{
					return;
				}

				templateManager.startTemplate(editorForAdd, template);
			}
		}.execute();
	}

	@Nullable
	protected static Editor openEditor(@NotNull PsiElement anchor, int offset)
	{
		PsiFile containingFile = anchor.getContainingFile();
		if(containingFile == null)
		{
			return null;
		}
		VirtualFile virtualFile = containingFile.getVirtualFile();
		if(virtualFile == null)
		{
			return null;
		}

		Project project = containingFile.getProject();
		FileEditorProviderManager editorProviderManager = FileEditorProviderManager.getInstance();
		if(editorProviderManager.getProviders(project, virtualFile).length == 0)
		{
			Messages.showMessageDialog(project, IdeBundle.message("error.files.of.this.type.cannot.be.opened",
					ApplicationNamesInfo.getInstance().getProductName()), IdeBundle.message("title.cannot.open.file"), Messages.getErrorIcon());
			return null;
		}

		OpenFileDescriptor descriptor = new OpenFileDescriptor(project, virtualFile);
		Editor editor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
		if(editor != null)
		{
			editor.getCaretModel().moveToOffset(offset);
			editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
			return editor;
		}
		return null;
	}

	@NotNull
	@Override
	public String getFamilyName()
	{
		return "C#";
	}
}
