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

package consulo.csharp.ide.refactoring.extractMethod;

import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.ide.codeInsight.actions.MethodGenerateUtil;
import consulo.csharp.ide.msil.representation.builder.CSharpStubBuilderVisitor;
import consulo.csharp.ide.refactoring.changeSignature.CSharpMethodDescriptor;
import consulo.csharp.lang.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpRecursiveElementVisitor;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.lang.psi.UsefulPsiTreeUtil;
import consulo.csharp.lang.psi.impl.light.builder.CSharpLightMethodDeclarationBuilder;
import consulo.csharp.lang.psi.impl.light.builder.CSharpLightParameterBuilder;
import consulo.csharp.lang.psi.impl.source.CSharpAssignmentExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import consulo.csharp.lang.psi.impl.source.CSharpReturnStatementImpl;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiParserFacade;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PairFunction;
import com.intellij.util.Processor;
import com.intellij.util.containers.ArrayListSet;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import consulo.annotations.RequiredDispatchThread;
import consulo.annotations.RequiredReadAction;
import consulo.annotations.RequiredWriteAction;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetLocalVariable;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetStatement;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefUtil;
import consulo.internal.dotnet.msil.decompiler.textBuilder.block.StubBlock;
import consulo.internal.dotnet.msil.decompiler.textBuilder.util.StubBlockUtil;

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

		final CSharpSimpleLikeMethodAsElement methodAsElement = PsiTreeUtil.getParentOfType(statements[0], CSharpSimpleLikeMethodAsElement.class);
		if(methodAsElement == null)
		{
			CommonRefactoringUtil.showErrorHint(project, editor, RefactoringBundle.getCannotRefactorMessage("No parent method"), "Extract Method", null);
			return;
		}

		final DotNetQualifiedElement qualifiedElement = PsiTreeUtil.getParentOfType(statements[0], DotNetQualifiedElement.class);
		if(qualifiedElement == null)
		{
			CommonRefactoringUtil.showErrorHint(project, editor, RefactoringBundle.getCannotRefactorMessage("No parent method"), "Extract Method", null);
			return;
		}

		final TextRange extractRange = new TextRange(statements[0].getTextRange().getStartOffset(), statements[statements.length - 1].getTextRange().getEndOffset());

		final MultiMap<DotNetVariable, CSharpReferenceExpression> variables = MultiMap.createLinkedSet();
		final Set<DotNetVariable> assignmentVariables = new ArrayListSet<DotNetVariable>();

		final Ref<DotNetTypeRef> returnTypeRef = Ref.create();
		for(DotNetStatement statement : statements)
		{
			statement.accept(new CSharpRecursiveElementVisitor()
			{
				@Override
				public void visitReturnStatement(CSharpReturnStatementImpl statement)
				{
					DotNetExpression expression = statement.getExpression();
					if(expression != null)
					{
						returnTypeRef.set(methodAsElement.getReturnTypeRef());
					}
				}

				@Override
				public void visitAssignmentExpression(CSharpAssignmentExpressionImpl expression)
				{
					super.visitAssignmentExpression(expression);

					DotNetExpression[] parameterExpressions = expression.getParameterExpressions();
					if(parameterExpressions.length > 0)
					{
						DotNetExpression parameterExpression = parameterExpressions[0];
						if(parameterExpression instanceof CSharpReferenceExpression)
						{
							PsiElement resolvedElement = ((CSharpReferenceExpression) parameterExpression).resolve();
							if(resolvedElement instanceof DotNetLocalVariable || resolvedElement instanceof DotNetParameter)
							{
								assignmentVariables.add((DotNetVariable) resolvedElement);
							}
						}
					}
				}

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

		CSharpLightMethodDeclarationBuilder builder = new CSharpLightMethodDeclarationBuilder(project);
		builder.withReturnType(returnTypeRef.get() == null ? new CSharpTypeRefByQName(file, DotNetTypes.System.Void) : returnTypeRef.get());
		builder.addModifier(CSharpModifier.PRIVATE);
		if(qualifiedElement instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) qualifiedElement).hasModifier(CSharpModifier.STATIC))
		{
			builder.addModifier(CSharpModifier.STATIC);
		}
		builder.withName("");

		for(DotNetVariable variable : variables.keySet())
		{
			CSharpLightParameterBuilder parameterBuilder = new CSharpLightParameterBuilder(project);
			if(assignmentVariables.contains(variable))
			{
				parameterBuilder.addModifier(CSharpModifier.REF);
			}
			parameterBuilder.withName(variable.getName());
			parameterBuilder.withTypeRef(variable.toTypeRef(true));

			builder.addParameter(parameterBuilder);
		}

		CSharpMethodDescriptor descriptor = new CSharpMethodDescriptor(builder);

		new CSharpExtractMethodDialog(project, descriptor, false, statements[0], new Processor<DotNetLikeMethodDeclaration>()
		{
			@Override
			public boolean process(final DotNetLikeMethodDeclaration builder)
			{
				final Document document = PsiDocumentManager.getInstance(project).getDocument(file);

				assert document != null;

				new WriteCommandAction.Simple<Object>(project, "Extract method", file)
				{
					@Override
					@RequiredWriteAction
					protected void run() throws Throwable
					{
						selectionModel.removeSelection();

						String text = document.getText(extractRange);

						document.deleteString(extractRange.getStartOffset(), extractRange.getEndOffset());

						StringBuilder callStatementBuilder = new StringBuilder();
						if(returnTypeRef.get() != null && !(UsefulPsiTreeUtil.getNextSiblingSkippingWhiteSpacesAndComments(ArrayUtil.getLastElement(statements)) instanceof DotNetStatement))
						{
							callStatementBuilder.append("return ");
						}
						callStatementBuilder.append(builder.getName());
						callStatementBuilder.append("(");
						StubBlockUtil.join(callStatementBuilder, variables.keySet().toArray(new DotNetVariable[]{}), new PairFunction<StringBuilder, DotNetVariable, Void>()
						{
							@Nullable
							@Override
							public Void fun(StringBuilder stringBuilder, DotNetVariable o)
							{
								if(assignmentVariables.contains(o))
								{
									stringBuilder.append("ref ");
								}
								stringBuilder.append(o.getName());
								return null;
							}
						}, ", ");
						callStatementBuilder.append(");");

						document.insertString(extractRange.getStartOffset(), callStatementBuilder);

						CharSequence methodText = buildText(builder, statements, text);

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

				return true;
			}
		}).show();
	}

	@RequiredReadAction
	public static CharSequence buildText(@NotNull DotNetLikeMethodDeclaration methodDeclaration, DotNetStatement[] statements, @NotNull String statementsText)
	{
		List<StubBlock> stubBlocks = CSharpStubBuilderVisitor.buildBlocks(methodDeclaration, false);
		StringBuilder builder = (StringBuilder) DeprecatedStubBlockUtil.buildText(stubBlocks);

		builder.append("{\n");
		builder.append(statementsText);

		if(!(statements[statements.length - 1] instanceof CSharpReturnStatementImpl) && !DotNetTypeRefUtil.isVmQNameEqual(methodDeclaration.getReturnTypeRef(), statements[0],
				DotNetTypes.System.Void))
		{
			String defaultValueForType = MethodGenerateUtil.getDefaultValueForType(methodDeclaration.getReturnTypeRef(), statements[0]);
			if(defaultValueForType != null)
			{
				builder.append("\nreturn ").append(defaultValueForType).append(";");
			}
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

		PsiElement statement1 = getTopmostParentOfType(element1, DotNetStatement.class);
		if(statement1 == null)
		{
			return EMPTY_ARRAY;
		}

		PsiElement statement2 = getTopmostParentOfType(element2, DotNetStatement.class);
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

	@Nullable
	@Contract("null, _ -> null")
	public static <T extends PsiElement> T getTopmostParentOfType(@Nullable PsiElement element, @NotNull Class<T> aClass)
	{
		T answer = PsiTreeUtil.getParentOfType(element, aClass);

		do
		{
			T next = PsiTreeUtil.getParentOfType(answer, aClass);
			if(next == null)
			{
				break;
			}
			if(next instanceof CSharpBlockStatementImpl && next.getParent() instanceof DotNetLikeMethodDeclaration)
			{
				return answer;
			}

			answer = next;
		}
		while(true);

		return answer;
	}
}
