package org.mustbe.consulo.csharp.ide.refactoring.introduceVariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.refactoring.util.CSharpNameSuggesterUtil;
import org.mustbe.consulo.csharp.ide.refactoring.util.CSharpRefactoringUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpFile;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariableDeclarationStatement;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.UsefulPsiTreeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpExpressionStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetCodeBlockOwner;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefUtil;
import com.intellij.codeInsight.CodeInsightUtilCore;
import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiParserFacade;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.refactoring.IntroduceTargetChooser;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.introduce.inplace.InplaceVariableIntroducer;
import com.intellij.refactoring.introduce.inplace.OccurrencesChooser;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;

@SuppressWarnings("MethodMayBeStatic")
public abstract class CSharpIntroduceHandler implements RefactoringActionHandler
{
	@Nullable
	protected static PsiElement findAnchor(PsiElement occurrence)
	{
		DotNetStatement statement = PsiTreeUtil.getParentOfType(occurrence, DotNetStatement.class);
		if(statement != null)
		{
			return statement;
		}
		return findAnchor(Arrays.asList(occurrence));
	}

	@Nullable
	protected static PsiElement findAnchor(List<PsiElement> occurrences)
	{
		if(occurrences.isEmpty())
		{
			return null;
		}
		int minOffset = Integer.MAX_VALUE;
		for(PsiElement element : occurrences)
		{
			minOffset = Math.min(minOffset, element.getTextOffset());
		}

		DotNetStatement statements = findContainingStatements(occurrences);
		if(statements == null)
		{
			return null;
		}

		PsiElement child = null;
		PsiElement[] children = statements.getChildren();
		for(PsiElement aChildren : children)
		{
			child = aChildren;
			if(child.getTextRange().contains(minOffset))
			{
				break;
			}
		}

		return child;
	}

	@Nullable
	private static DotNetStatement findContainingStatements(List<PsiElement> occurrences)
	{
		DotNetStatement result = PsiTreeUtil.getParentOfType(occurrences.get(0), DotNetStatement.class, true);
		while(result != null && !UsefulPsiTreeUtil.isAncestor(result, occurrences, true))
		{
			result = PsiTreeUtil.getParentOfType(result, DotNetStatement.class, true);
		}
		return result;
	}

	protected final String myDialogTitle;

	public CSharpIntroduceHandler(@NotNull final String dialogTitle)
	{
		myDialogTitle = dialogTitle;
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext dataContext)
	{
		performAction(new CSharpIntroduceOperation(project, editor, file, null));
	}

	@Override
	public void invoke(@NotNull Project project, @NotNull PsiElement[] elements, DataContext dataContext)
	{
	}

	@RequiredReadAction
	public void performAction(CSharpIntroduceOperation operation)
	{
		final PsiFile file = operation.getFile();
		if(!CommonRefactoringUtil.checkReadOnlyStatus(file))
		{
			return;
		}
		final Editor editor = operation.getEditor();
		if(editor.getSettings().isVariableInplaceRenameEnabled())
		{
			final TemplateState templateState = TemplateManagerImpl.getTemplateState(operation.getEditor());
			if(templateState != null && !templateState.isFinished())
			{
				return;
			}
		}

		PsiElement element1 = null;
		PsiElement element2 = null;
		final SelectionModel selectionModel = editor.getSelectionModel();
		if(selectionModel.hasSelection())
		{
			element1 = file.findElementAt(selectionModel.getSelectionStart());
			element2 = file.findElementAt(selectionModel.getSelectionEnd() - 1);
			if(element1 instanceof PsiWhiteSpace)
			{
				int startOffset = element1.getTextRange().getEndOffset();
				element1 = file.findElementAt(startOffset);
			}
			if(element2 instanceof PsiWhiteSpace)
			{
				int endOffset = element2.getTextRange().getStartOffset();
				element2 = file.findElementAt(endOffset - 1);
			}
		}
		else
		{
			if(smartIntroduce(operation))
			{
				return;
			}
			final CaretModel caretModel = editor.getCaretModel();
			final Document document = editor.getDocument();
			int lineNumber = document.getLineNumber(caretModel.getOffset());
			if((lineNumber >= 0) && (lineNumber < document.getLineCount()))
			{
				element1 = file.findElementAt(document.getLineStartOffset(lineNumber));
				element2 = file.findElementAt(document.getLineEndOffset(lineNumber) - 1);
			}
		}
		final Project project = operation.getProject();
		if(element1 == null || element2 == null)
		{
			showCannotPerformError(project, editor);
			return;
		}

		element1 = CSharpRefactoringUtil.getSelectedExpression(project, file, element1, element2);
		if(element1 == null)
		{
			showCannotPerformError(project, editor);
			return;
		}

		if(!checkIntroduceContext(file, editor, element1))
		{
			return;
		}
		operation.setElement(element1);
		performActionOnElement(operation);
	}

	protected boolean checkIntroduceContext(PsiFile file, Editor editor, PsiElement element)
	{
		return true;
	}

	private void showCannotPerformError(Project project, Editor editor)
	{
		CommonRefactoringUtil.showErrorHint(project, editor, RefactoringBundle.message("refactoring.introduce.selection.error"), myDialogTitle, "refactoring.extractMethod");
	}


	@RequiredReadAction
	private boolean smartIntroduce(final CSharpIntroduceOperation operation)
	{
		final Editor editor = operation.getEditor();
		final PsiFile file = operation.getFile();
		int offset = editor.getCaretModel().getOffset();
		PsiElement temp = file.findElementAt(offset);
		assert temp != null;
		if(!checkIntroduceContext(file, editor, temp))
		{
			return true;
		}

		if(temp instanceof PsiWhiteSpace)
		{
			temp = PsiTreeUtil.prevLeaf(temp);
		}

		final List<DotNetExpression> expressions = new ArrayList<DotNetExpression>();

		// int var = 1;<caret>
		if(PsiUtilCore.getElementType(temp) == CSharpTokens.SEMICOLON)
		{
			CSharpLocalVariableDeclarationStatement statement = PsiTreeUtil.getParentOfType(temp, CSharpLocalVariableDeclarationStatement.class);
			if(statement != null)
			{
				CSharpLocalVariable[] variables = statement.getVariables();
				CSharpLocalVariable lastElement = ArrayUtil.getLastElement(variables);
				if(lastElement != null)
				{
					temp = lastElement.getInitializer();
				}
			}
		}

		while(temp != null)
		{
			if(temp instanceof CSharpFile)
			{
				break;
			}
			if(temp instanceof DotNetExpression)
			{
				if(temp instanceof CSharpReferenceExpression)
				{
					CSharpReferenceExpression.ResolveToKind kind = ((CSharpReferenceExpression) temp).kind();
					if(kind == CSharpReferenceExpression.ResolveToKind.TYPE_LIKE || kind == CSharpReferenceExpression.ResolveToKind.CONSTRUCTOR)
					{
						temp = temp.getParent();
						continue;
					}

					PsiElement parent = temp.getParent();
					if(parent instanceof CSharpMethodCallExpressionImpl && ((CSharpMethodCallExpressionImpl) parent).getCallExpression() == temp)
					{
						temp = temp.getParent();
						continue;
					}
				}

				DotNetTypeRef typeRef = ((DotNetExpression) temp).toTypeRef(true);
				if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, file, DotNetTypes.System.Void))
				{
					break;
				}

				expressions.add((DotNetExpression) temp);
			}
			temp = temp.getParent();
		}

		if(expressions.isEmpty())
		{
			PsiElement someElement = UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpaces(file.findElementAt(offset), false);
			if(someElement instanceof CSharpExpressionStatementImpl)
			{
				expressions.add(((CSharpExpressionStatementImpl) someElement).getExpression());
			}
		}

		if(expressions.isEmpty())
		{
			showCannotPerformError(file.getProject(), editor);
			return false;
		}
		if(expressions.size() == 1 || ApplicationManager.getApplication().isUnitTestMode())
		{
			operation.setElement(expressions.get(0));
			performActionOnElement(operation);
			return true;
		}
		else if(expressions.size() > 1)
		{
			IntroduceTargetChooser.showChooser(editor, expressions, new Pass<DotNetExpression>()
					{
						@Override
						public void pass(DotNetExpression expression)
						{
							operation.setElement(expression);
							performActionOnElement(operation);
						}
					}, new Function<DotNetExpression, String>()
					{
						@Override
						public String fun(DotNetExpression expression)
						{
							return expression.getText();
						}
					}
			);
			return true;
		}
		return false;
	}

	private void performActionOnElement(CSharpIntroduceOperation operation)
	{
		if(!checkEnabled(operation))
		{
			return;
		}
		final PsiElement element = operation.getElement();

		final DotNetExpression initializer = (DotNetExpression) element;
		operation.setInitializer(initializer);

		operation.setOccurrences(getOccurrences(element, initializer));
		operation.setSuggestedNames(getSuggestedNames(initializer));
		if(operation.getOccurrences().size() == 0)
		{
			operation.setReplaceAll(false);
		}

		performActionOnElementOccurrences(operation);
	}

	@NotNull
	@RequiredReadAction
	protected Collection<String> getSuggestedNames(@NotNull DotNetExpression initializer)
	{
		return CSharpNameSuggesterUtil.getSuggestedNames(initializer);
	}

	protected void performActionOnElementOccurrences(final CSharpIntroduceOperation operation)
	{
		final Editor editor = operation.getEditor();
		if(editor.getSettings().isVariableInplaceRenameEnabled())
		{
			ensureName(operation);
			if(operation.isReplaceAll() || operation.getOccurrences().isEmpty())
			{
				performInplaceIntroduce(operation);
			}
			else
			{
				OccurrencesChooser.simpleChooser(editor).showChooser(operation.getElement(), operation.getOccurrences(), new Pass<OccurrencesChooser.ReplaceChoice>()
				{
					@Override
					public void pass(OccurrencesChooser.ReplaceChoice replaceChoice)
					{
						operation.setReplaceAll(replaceChoice == OccurrencesChooser.ReplaceChoice.ALL);
						performInplaceIntroduce(operation);
					}
				});
			}
		}
		else
		{
			performIntroduceWithDialog(operation);
		}
	}

	protected boolean checkEnabled(CSharpIntroduceOperation operation)
	{
		return true;
	}

	protected static void ensureName(CSharpIntroduceOperation operation)
	{
		if(operation.getName() == null)
		{
			final Collection<String> suggestedNames = operation.getSuggestedNames();
			if(suggestedNames.size() > 0)
			{
				operation.setName(suggestedNames.iterator().next());
			}
			else
			{
				operation.setName("x");
			}
		}
	}

	protected List<PsiElement> getOccurrences(PsiElement element, @NotNull final DotNetExpression expression)
	{
		PsiElement context = PsiTreeUtil.getParentOfType(element, DotNetCodeBlockOwner.class);
		if(context == null)
		{
			context = element;
		}
		return CSharpRefactoringUtil.getOccurrences(expression, context);
	}

	protected void performIntroduceWithDialog(CSharpIntroduceOperation operation)
	{
		final Project project = operation.getProject();
		if(operation.getName() == null)
		{
			CSharpIntroduceDialog dialog = new CSharpIntroduceDialog(project, myDialogTitle, operation);
			dialog.show();
			if(!dialog.isOK())
			{
				return;
			}
			operation.setName(dialog.getName());
			operation.setReplaceAll(dialog.doReplaceAllOccurrences());
		}

		PsiElement declaration = performRefactoring(operation);
		if(declaration == null)
		{
			return;
		}
		final Editor editor = operation.getEditor();
		editor.getCaretModel().moveToOffset(declaration.getTextRange().getEndOffset());
		editor.getSelectionModel().removeSelection();
	}

	protected void performInplaceIntroduce(CSharpIntroduceOperation operation)
	{
		final PsiElement statement = performRefactoring(operation);
		final CSharpLocalVariable target = PsiTreeUtil.findChildOfType(statement, CSharpLocalVariable.class);
		final PsiElement nameIdentifier = target != null ? target.getNameIdentifier() : null;
		if(nameIdentifier == null)
		{
			return;
		}
		final List<PsiElement> occurrences = operation.getOccurrences();
		operation.getEditor().getCaretModel().moveToOffset(nameIdentifier.getTextOffset());
		final InplaceVariableIntroducer<PsiElement> introducer = createVariableIntroducer(target, operation, occurrences);
		introducer.performInplaceRefactoring(new LinkedHashSet<String>(operation.getSuggestedNames()));
	}

	@NotNull
	protected abstract InplaceVariableIntroducer<PsiElement> createVariableIntroducer(CSharpLocalVariable target, CSharpIntroduceOperation operation, List<PsiElement> occurrences);

	@Nullable
	protected PsiElement performRefactoring(@NotNull CSharpIntroduceOperation operation)
	{
		PsiElement anchor = operation.isReplaceAll() ? findAnchor(operation.getOccurrences()) : findAnchor(operation.getInitializer());
		if(anchor == null)
		{
			CommonRefactoringUtil.showErrorHint(operation.getProject(), operation.getEditor(), RefactoringBundle.getCannotRefactorMessage(null), RefactoringBundle.getCannotRefactorMessage(null),
					null);
			return null;
		}
		PsiElement declaration = createDeclaration(operation);
		if(declaration == null)
		{
			showCannotPerformError(operation.getProject(), operation.getEditor());
			return null;
		}

		declaration = performReplace(declaration, operation);
		if(declaration != null)
		{
			declaration = CodeInsightUtilCore.forcePsiPostprocessAndRestoreElement(declaration);
		}
		return declaration;
	}

	@Nullable
	@RequiredReadAction
	public PsiElement createDeclaration(CSharpIntroduceOperation operation)
	{
		final Project project = operation.getProject();
		final DotNetExpression initializer = operation.getInitializer();
		InitializerTextBuilder builder = new InitializerTextBuilder();
		initializer.accept(builder);
		String assignmentText = getDeclarationString(operation, builder.result());
		return CSharpFileFactory.createStatement(project, assignmentText);
	}

	@NotNull
	@RequiredReadAction
	protected abstract String getDeclarationString(CSharpIntroduceOperation operation, String initExpression);

	@Nullable
	@RequiredDispatchThread
	private PsiElement performReplace(@NotNull final PsiElement declaration, final CSharpIntroduceOperation operation)
	{
		final DotNetExpression initializer = operation.getInitializer();
		final Project project = operation.getProject();
		PsiDocumentManager.getInstance(project).commitAllDocuments();
		return new WriteCommandAction<PsiElement>(project, declaration.getContainingFile())
		{
			@Override
			protected void run(final Result<PsiElement> result) throws Throwable
			{
				PsiElement createdDeclaration = addDeclaration(operation, declaration);

				boolean needReferenceOfVariable = isNeedReferenceOfVariable(initializer);
				if(needReferenceOfVariable)
				{
					if(createdDeclaration != null)
					{
						createdDeclaration = modifyDeclaration(createdDeclaration);
					}
				}

				result.setResult(createdDeclaration);

				PsiElement newExpression = createExpression(project, operation.getName());

				if(operation.isReplaceAll())
				{
					List<PsiElement> newOccurrences = new ArrayList<PsiElement>();
					List<PsiElement> occurrences = operation.getOccurrences();
					if(occurrences.size() == 1 && !needReferenceOfVariable)
					{
						occurrences.get(0).delete();
					}
					else
					{
						for(PsiElement occurrence : occurrences)
						{
							final PsiElement replaced = replaceExpression(occurrence, newExpression, operation);
							if(replaced != null)
							{
								newOccurrences.add(replaced);
							}
						}
						operation.setOccurrences(newOccurrences);
					}
				}
				else
				{
					if(needReferenceOfVariable)
					{
						final PsiElement replaced = replaceExpression(initializer, newExpression, operation);
						operation.setOccurrences(Collections.singletonList(replaced));
					}
					else
					{
						initializer.delete();
					}
				}

				postRefactoring(operation.getElement());
			}
		}.execute().getResultObject();
	}

	protected PsiElement modifyDeclaration(@NotNull PsiElement declaration)
	{
		PsiElement parent = declaration.getParent();
		parent.addAfter(PsiParserFacade.SERVICE.getInstance(declaration.getProject()).createWhiteSpaceFromText("\n"), declaration);
		return declaration;
	}

	private boolean isNeedReferenceOfVariable(@NotNull DotNetExpression expression)
	{
		PsiElement parent = expression.getParent();
		if(parent instanceof CSharpExpressionStatementImpl)
		{
			return ((CSharpExpressionStatementImpl) expression.getParent()).getExpression() != expression;
		}
		return true;
	}

	@Nullable
	protected DotNetExpression createExpression(Project project, String name)
	{
		return CSharpFileFactory.createExpression(project, name);
	}

	@Nullable
	protected PsiElement replaceExpression(PsiElement expression, PsiElement newExpression, CSharpIntroduceOperation operation)
	{
		return expression.replace(newExpression);
	}


	protected void postRefactoring(PsiElement element)
	{
	}

	@Nullable
	public PsiElement addDeclaration(CSharpIntroduceOperation operation, PsiElement declaration)
	{
		PsiElement anchor = operation.isReplaceAll() ? findAnchor(operation.getOccurrences()) : findAnchor(operation.getInitializer());
		if(anchor == null)
		{
			CommonRefactoringUtil.showErrorHint(operation.getProject(), operation.getEditor(), RefactoringBundle.getCannotRefactorMessage(null), RefactoringBundle.getCannotRefactorMessage(null),
					null);
			return null;
		}
		final PsiElement parent = anchor.getParent();
		PsiElement psiElement = parent.addBefore(declaration, anchor);
		CodeStyleManager.getInstance(declaration.getProject()).reformat(psiElement);
		return psiElement;
	}

	protected static class CSharpInplaceVariableIntroducer extends InplaceVariableIntroducer<PsiElement>
	{
		public CSharpInplaceVariableIntroducer(CSharpLocalVariable target, CSharpIntroduceOperation operation, List<PsiElement> occurrences)
		{
			super(target, operation.getEditor(), operation.getProject(), "Introduce Variable", occurrences.toArray(new PsiElement[occurrences.size()]), null);
		}

		@Override
		@RequiredReadAction
		protected void moveOffsetAfter(boolean success)
		{
			super.moveOffsetAfter(success);

			if(success)
			{
				PsiNamedElement variable = getVariable();
				if(variable instanceof DotNetVariable && variable.isValid())
				{
					myEditor.getCaretModel().moveToOffset(getVariableEndOffset((DotNetVariable) variable));
					myEditor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
				}
			}
		}

		@RequiredReadAction
		protected int getVariableEndOffset(DotNetVariable variable)
		{
			return variable.getTextRange().getEndOffset();
		}

		@Override
		protected PsiElement checkLocalScope()
		{
			return myElementToRename.getContainingFile();
		}
	}

	private static class InitializerTextBuilder extends PsiRecursiveElementVisitor
	{
		private final StringBuilder myResult = new StringBuilder();

		@Override
		public void visitWhiteSpace(PsiWhiteSpace space)
		{
			myResult.append(space.getText().replace('\n', ' '));
		}

		@Override
		public void visitElement(PsiElement element)
		{
			if(element.getChildren().length == 0)
			{
				myResult.append(element.getText());
			}
			else
			{
				super.visitElement(element);
			}
		}

		public String result()
		{
			return myResult.toString();
		}
	}
}
