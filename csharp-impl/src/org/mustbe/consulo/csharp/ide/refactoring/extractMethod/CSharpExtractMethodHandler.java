/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.ide.refactoring.extractMethod;

import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JComponent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.RequiredWriteAction;
import org.mustbe.consulo.csharp.ide.msil.representation.builder.CSharpStubBuilderVisitor;
import org.mustbe.consulo.csharp.ide.refactoring.changeSignature.CSharpChangeSignatureDialog;
import org.mustbe.consulo.csharp.ide.refactoring.changeSignature.CSharpMethodDescriptor;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpRecursiveElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightMethodDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightParameterBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.dotnet.msil.decompiler.textBuilder.block.StubBlock;
import org.mustbe.dotnet.msil.decompiler.textBuilder.util.StubBlockUtil;
import com.intellij.CommonBundle;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiParserFacade;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.PairFunction;
import com.intellij.util.containers.ArrayListSet;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;

/**
 * @author VISTALL
 * @since 07.11.2015
 */
public class CSharpExtractMethodHandler implements RefactoringActionHandler
{
	@Deprecated
	private static final DotNetStatement[] EMPTY_ARRAY = new DotNetStatement[0];

	@Override
	public void invoke(@NotNull Project project, @NotNull PsiElement[] elements, DataContext dataContext)
	{
	}

	@Override
	@RequiredDispatchThread
	public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file, DataContext dataContext)
	{
		PsiDocumentManager.getInstance(project).commitAllDocuments();

		final SelectionModel selectionModel = editor.getSelectionModel();
		if(!selectionModel.hasSelection())
		{
			selectionModel.selectLineAtCaret();
		}

		final DotNetStatement[] statements = getStatements(file, selectionModel.getSelectionStart(), selectionModel.getSelectionEnd());

		if(statements.length == 0)
		{
			CommonRefactoringUtil.showErrorHint(project, editor, RefactoringBundle.getCannotRefactorMessage("No statements"), "Extract Method", null);
			return;
		}

		CSharpSimpleLikeMethodAsElement methodAsElement = PsiTreeUtil.getParentOfType(statements[0], CSharpSimpleLikeMethodAsElement.class);
		if(methodAsElement == null)
		{
			CommonRefactoringUtil.showErrorHint(project, editor, RefactoringBundle.getCannotRefactorMessage("No parent method"), "Extract Method", null);
			return;
		}

		final TextRange extractRange = new TextRange(statements[0].getTextRange().getStartOffset(), statements[statements.length - 1].getTextRange().getEndOffset());

		final MultiMap<DotNetVariable, CSharpReferenceExpression> variables = MultiMap.createLinkedSet();

		for(DotNetStatement statement : statements)
		{
			statement.accept(new CSharpRecursiveElementVisitor()
			{
				@Override
				public void visitReferenceExpression(CSharpReferenceExpression expression)
				{
					super.visitReferenceExpression(expression);

					if(expression.getQualifier() != null)
					{
						return;
					}

					PsiElement resolvedElement = expression.resolve();
					// parameters always extracted as new parameter
					if(resolvedElement instanceof DotNetParameter)
					{
						variables.putValue((DotNetVariable) resolvedElement, expression);
					}
					else if(resolvedElement instanceof CSharpLocalVariable)
					{
						if(!extractRange.contains(resolvedElement.getTextOffset()))
						{
							variables.putValue((DotNetVariable) resolvedElement, expression);
						}
					}
				}
			});
		}

		final CSharpLightMethodDeclarationBuilder builder = new CSharpLightMethodDeclarationBuilder(project);
		builder.withReturnType(new CSharpTypeRefByQName(DotNetTypes.System.Void));
		builder.addModifier(CSharpModifier.PRIVATE);
		if(methodAsElement.hasModifier(CSharpModifier.STATIC))
		{
			builder.addModifier(CSharpModifier.STATIC);
		}
		builder.withName("");

		for(DotNetVariable variable : variables.keySet())
		{
			CSharpLightParameterBuilder parameterBuilder = new CSharpLightParameterBuilder(project);
			parameterBuilder.withName(variable.getName());
			parameterBuilder.withTypeRef(variable.toTypeRef(true));

			builder.addParameter(parameterBuilder);
		}

		CSharpMethodDescriptor descriptor = new CSharpMethodDescriptor(builder);

		new CSharpChangeSignatureDialog(project, descriptor, false, statements[0])
		{
			{
				setOKButtonText(CommonBundle.getOkButtonText());
				setTitle("Extract Method");
				getRefactorAction().putValue(Action.NAME, CommonBundle.getOkButtonText());
			}

			@Override
			protected boolean hasPreviewButton()
			{
				return false;
			}

			@Override
			protected boolean areButtonsValid()
			{
				return !StringUtil.isEmpty(getMethodName());
			}

			@Override
			protected BaseRefactoringProcessor createRefactoringProcessor()
			{
				return null;
			}

			@Override
			public JComponent getPreferredFocusedComponent()
			{
				return myNameField;
			}

			@Nullable
			@Override
			protected String validateAndCommitData()
			{
				if(StringUtil.isEmpty(getMethodName()))
				{
					return "Method name cant be empty";
				}
				return super.validateAndCommitData();
			}

			@Override
			protected void invokeRefactoring(BaseRefactoringProcessor processor)
			{
				final DotNetQualifiedElement qualifiedElement = PsiTreeUtil.getParentOfType(statements[0], DotNetQualifiedElement.class);
				if(qualifiedElement == null)
				{
					return;
				}

				final Document document = PsiDocumentManager.getInstance(project).getDocument(file);

				assert document != null;

				new WriteCommandAction.Simple<Object>(project, "Extract method", file)
				{
					@Override
					@RequiredWriteAction
					protected void run() throws Throwable
					{
						builder.withName(getMethodName());

						selectionModel.removeSelection();

						document.deleteString(extractRange.getStartOffset(), extractRange.getEndOffset());

						StringBuilder callStatementBuilder = new StringBuilder();
						callStatementBuilder.append(getMethodName());
						callStatementBuilder.append("(");
						StubBlockUtil.join(callStatementBuilder, variables.keySet().toArray(new DotNetVariable[]{}), new PairFunction<StringBuilder, DotNetVariable, Void>()
						{
							@Nullable
							@Override
							public Void fun(StringBuilder stringBuilder, DotNetVariable o)
							{
								stringBuilder.append(o.getName());
								return null;
							}
						}, ", ");
						callStatementBuilder.append(");");

						document.insertString(extractRange.getStartOffset(), callStatementBuilder);

						CharSequence methodText = buildText(builder, statements);

						// insert method
						PsiElement qualifiedParent = qualifiedElement.getParent();

						DotNetLikeMethodDeclaration method = CSharpFileFactory.createMethod(project, methodText);

						PsiDocumentManager.getInstance(project).commitDocument(document);

						qualifiedParent.addAfter(PsiParserFacade.SERVICE.getInstance(file.getProject()).createWhiteSpaceFromText("\n\n"), qualifiedElement);

						PsiElement nextSibling = qualifiedElement.getNextSibling();

						PsiElement newMethod = qualifiedParent.addAfter(method, nextSibling);

						PsiDocumentManager.getInstance(getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());

						PsiDocumentManager.getInstance(project).commitDocument(document);

						CodeStyleManager.getInstance(getProject()).reformat(newMethod);
					}
				}.execute();

				close(DialogWrapper.OK_EXIT_CODE);
			}
		}.show();
	}

	@RequiredReadAction
	public static CharSequence buildText(CSharpMethodDeclaration methodDeclaration, DotNetStatement[] statements)
	{
		List<StubBlock> stubBlocks = CSharpStubBuilderVisitor.buildBlocks(methodDeclaration, false);
		StringBuilder builder = (StringBuilder) DeprecatedStubBlockUtil.buildText(stubBlocks);

		builder.append("{\n");
		for(DotNetStatement statement : statements)
		{
			builder.append(statement.getText().trim()).append("\n");
		}
		builder.append("}");
		return builder;
	}

	@RequiredReadAction
	private DotNetStatement[] getStatements(PsiFile file, int startOffset, int endOffset)
	{
		Set<DotNetStatement> set = new ArrayListSet<DotNetStatement>();

		PsiElement element1 = file.findElementAt(startOffset);
		PsiElement element2 = file.findElementAt(endOffset - 1);
		if(element1 instanceof PsiWhiteSpace)
		{
			startOffset = element1.getTextRange().getEndOffset();
			element1 = file.findElementAt(startOffset);
		}
		if(element2 instanceof PsiWhiteSpace)
		{
			endOffset = element2.getTextRange().getStartOffset();
			element2 = file.findElementAt(endOffset - 1);
		}

		PsiElement statement1 = PsiTreeUtil.getParentOfType(element1, DotNetStatement.class);
		if(statement1 == null)
		{
			return EMPTY_ARRAY;
		}

		PsiElement statement2 = PsiTreeUtil.getParentOfType(element2, DotNetStatement.class);
		if(statement2 == null)
		{
			return EMPTY_ARRAY;
		}

		PsiElement temp = statement1;
		while(temp != null)
		{
			if(temp instanceof DotNetStatement)
			{
				set.add((DotNetStatement) temp);
			}

			if(temp == statement2)
			{
				return ContainerUtil.toArray(set, EMPTY_ARRAY);
			}

			temp = temp.getNextSibling();
		}
		return EMPTY_ARRAY;
	}
}
