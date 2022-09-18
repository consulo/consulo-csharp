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

package consulo.csharp.impl.ide.refactoring.extractMethod;

import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.Editor;
import consulo.codeEditor.SelectionModel;
import consulo.csharp.impl.ide.codeInsight.actions.MethodGenerateUtil;
import consulo.csharp.impl.ide.msil.representation.builder.CSharpStubBuilderVisitor;
import consulo.csharp.impl.ide.refactoring.changeSignature.CSharpMethodDescriptor;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.impl.psi.CSharpRecursiveElementVisitor;
import consulo.csharp.lang.impl.psi.UsefulPsiTreeUtil;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightMethodDeclarationBuilder;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightParameterBuilder;
import consulo.csharp.lang.impl.psi.source.CSharpAssignmentExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpBlockStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpMethodBodyImpl;
import consulo.csharp.lang.impl.psi.source.CSharpReturnStatementImpl;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.dataContext.DataContext;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.ide.impl.idea.util.containers.ArrayListSet;
import consulo.internal.dotnet.msil.decompiler.textBuilder.block.StubBlock;
import consulo.internal.dotnet.msil.decompiler.textBuilder.util.StubBlockUtil;
import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.refactoring.RefactoringBundle;
import consulo.language.editor.refactoring.action.RefactoringActionHandler;
import consulo.language.editor.refactoring.util.CommonRefactoringUtil;
import consulo.language.psi.*;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.collection.ArrayUtil;
import consulo.util.collection.MultiMap;
import consulo.util.lang.function.PairFunction;
import consulo.util.lang.ref.Ref;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 07.11.2015
 */
public class CSharpExtractMethodHandler implements RefactoringActionHandler
{
	@Deprecated
	private static final DotNetStatement[] EMPTY_ARRAY = new DotNetStatement[0];

	@Override
	public void invoke(@Nonnull Project project, @Nonnull PsiElement[] elements, DataContext dataContext)
	{
	}

	@Override
	@RequiredUIAccess
	public void invoke(@Nonnull final Project project, final Editor editor, final PsiFile file, DataContext dataContext)
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
		final Set<DotNetVariable> assignmentVariables = new LinkedHashSet<>();

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

		new CSharpExtractMethodDialog(project, descriptor, false, statements[0], it -> {
			final Document document = PsiDocumentManager.getInstance(project).getDocument(file);

			assert document != null;

			WriteCommandAction.runWriteCommandAction(project, "Extract method", null, () ->
			{
				selectionModel.removeSelection();

				String text = document.getText(extractRange);

				document.deleteString(extractRange.getStartOffset(), extractRange.getEndOffset());

				StringBuilder callStatementBuilder = new StringBuilder();
				if(returnTypeRef.get() != null && !(UsefulPsiTreeUtil.getNextSiblingSkippingWhiteSpacesAndComments(ArrayUtil.getLastElement(statements)) instanceof DotNetStatement))
				{
					callStatementBuilder.append("return ");
				}
				callStatementBuilder.append(it.getName());
				callStatementBuilder.append("(");
				StubBlockUtil.join(callStatementBuilder, variables.keySet().toArray(DotNetVariable[]::new), new PairFunction<StringBuilder, DotNetVariable, Void>()
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

				CharSequence methodText = buildText(it, statements, text);

				// insert method
				PsiElement qualifiedParent = qualifiedElement.getParent();

				DotNetLikeMethodDeclaration method = CSharpFileFactory.createMethod(project, methodText);

				PsiDocumentManager.getInstance(project).commitDocument(document);

				qualifiedParent.addAfter(PsiParserFacade.getInstance(file.getProject()).createWhiteSpaceFromText("\n\n"), qualifiedElement);

				PsiElement nextSibling = qualifiedElement.getNextSibling();

				PsiElement newMethod = qualifiedParent.addAfter(method, nextSibling);

				PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());

				PsiDocumentManager.getInstance(project).commitDocument(document);

				CodeStyleManager.getInstance(project).reformat(newMethod);
			}, file);

			return true;
		}).show();
	}

	@RequiredReadAction
	public static CharSequence buildText(@Nonnull DotNetLikeMethodDeclaration methodDeclaration, DotNetStatement[] statements, @Nonnull String statementsText)
	{
		List<StubBlock> stubBlocks = CSharpStubBuilderVisitor.buildBlocks(methodDeclaration, false);
		StringBuilder builder = (StringBuilder) StubBlockUtil.buildText(stubBlocks);

		builder.append("{\n");
		builder.append(statementsText);

		if(!(statements[statements.length - 1] instanceof CSharpReturnStatementImpl) && !DotNetTypeRefUtil.isVmQNameEqual(methodDeclaration.getReturnTypeRef(), DotNetTypes.System.Void))
		{
			String defaultValueForType = MethodGenerateUtil.getDefaultValueForType(methodDeclaration.getReturnTypeRef());
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
				return set.toArray(EMPTY_ARRAY);
			}

			temp = temp.getNextSibling();
		}
		return EMPTY_ARRAY;
	}

	@Nullable
	@Contract("null, _ -> null")
	public static <T extends PsiElement> T getTopmostParentOfType(@Nullable PsiElement element, @Nonnull Class<T> aClass)
	{
		T answer = PsiTreeUtil.getParentOfType(element, aClass);

		do
		{
			T next = PsiTreeUtil.getParentOfType(answer, aClass);
			if(next == null)
			{
				break;
			}
			if(next instanceof CSharpBlockStatementImpl && next.getParent() instanceof CSharpMethodBodyImpl)
			{
				return answer;
			}

			answer = next;
		}
		while(true);

		return answer;
	}
}
