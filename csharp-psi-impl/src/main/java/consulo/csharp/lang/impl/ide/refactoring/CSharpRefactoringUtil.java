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

package consulo.csharp.lang.impl.ide.refactoring;

import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.Editor;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.impl.psi.CSharpRecursiveElementVisitor;
import consulo.csharp.lang.impl.psi.UsefulPsiTreeUtil;
import consulo.csharp.lang.impl.psi.source.CSharpMethodCallExpressionImpl;
import consulo.csharp.lang.psi.CSharpIdentifier;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetStatement;
import consulo.language.ast.ASTNode;
import consulo.language.psi.*;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Fedor.Korotkov
 *
 * from google dart
 */
public class CSharpRefactoringUtil
{
	@RequiredReadAction
	public static void replaceNameIdentifier(PsiNameIdentifierOwner owner, String newName)
	{
		PsiElement nameIdentifier = owner.getNameIdentifier();
		if(!(nameIdentifier instanceof CSharpIdentifier))
		{
			return;
		}

		CSharpIdentifier newIdentifier = CSharpFileFactory.createIdentifier(owner.getProject(), newName);

		nameIdentifier.replace(newIdentifier);
	}

	public static Set<String> collectUsedNames(@Nonnull PsiElement context, @Nullable PsiElement toSkip)
	{
		return new HashSet<>(ContainerUtil.map(collectUsedComponents(context, toSkip), PsiNamedElement::getName));
	}

	public static Set<PsiNamedElement> collectUsedComponents(@Nonnull PsiElement context, @Nullable PsiElement toSkip)
	{
		final Set<PsiNamedElement> usedComponentNames = new HashSet<>();
		PsiTreeUtil.treeWalkUp(new ComponentNameScopeProcessor(usedComponentNames, toSkip), context, null, new ResolveState());
		return usedComponentNames;
	}

	@Nullable
	public static DotNetExpression getSelectedExpression(@Nonnull final Project project,
			@Nonnull PsiFile file,
			@Nonnull final PsiElement element1,
			@Nonnull final PsiElement element2)
	{
		PsiElement parent = PsiTreeUtil.findCommonParent(element1, element2);
		if(parent == null)
		{
			return null;
		}
		if(parent instanceof DotNetExpression)
		{
			return (DotNetExpression) parent;
		}
		return PsiTreeUtil.getParentOfType(parent, DotNetExpression.class);
	}

	@Nonnull
	public static List<PsiElement> getOccurrences(@Nonnull final PsiElement pattern, @Nullable final PsiElement context)
	{
		if(context == null)
		{
			return Collections.emptyList();
		}
		final List<PsiElement> occurrences = new ArrayList<>();
		context.acceptChildren(new CSharpRecursiveElementVisitor()
		{
			@Override
			public void visitReferenceExpression(CSharpReferenceExpression expression)
			{
				if(expression.resolve() == pattern)
				{
					occurrences.add(expression);
				}
			}
		});
		return occurrences;
	}

	@Nullable
	public static PsiElement findOccurrenceUnderCaret(List<PsiElement> occurrences, Editor editor)
	{
		if(occurrences.isEmpty())
		{
			return null;
		}
		int offset = editor.getCaretModel().getOffset();
		for(PsiElement occurrence : occurrences)
		{
			if(occurrence.getTextRange().contains(offset))
			{
				return occurrence;
			}
		}
		int line = editor.getDocument().getLineNumber(offset);
		for(PsiElement occurrence : occurrences)
		{
			if(occurrence.isValid() && editor.getDocument().getLineNumber(occurrence.getTextRange().getStartOffset()) == line)
			{
				return occurrence;
			}
		}
		for(PsiElement occurrence : occurrences)
		{
			if(occurrence.isValid())
			{
				return occurrence;
			}
		}
		return null;
	}

	public static PsiElement[] findStatementsInRange(PsiFile file, int startOffset, int endOffset)
	{
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

		if(element1 != null && element2 != null)
		{
			PsiElement commonParent = PsiTreeUtil.findCommonParent(element1, element2);
			if(commonParent instanceof DotNetExpression)
			{
				return new PsiElement[]{commonParent};
			}
		}

		final DotNetStatement statements = PsiTreeUtil.getParentOfType(element1, DotNetStatement.class);
		if(statements == null || element1 == null || element2 == null || !PsiTreeUtil.isAncestor(statements, element2, true))
		{
			return PsiElement.EMPTY_ARRAY;
		}

		// don't forget about leafs (ex. ';')
		final ASTNode[] astResult = UsefulPsiTreeUtil.findChildrenRange(statements.getNode().getChildren(null), startOffset, endOffset);
		return ContainerUtil.map2Array(astResult, PsiElement.class, node -> node.getPsi());
	}

	@Nullable
	public static DotNetExpression findExpressionInRange(PsiFile file, int startOffset, int endOffset)
	{
		PsiElement element1 = file.findElementAt(startOffset);
		PsiElement element2 = file.findElementAt(endOffset - 1);
		if(element1 instanceof PsiWhiteSpace)
		{
			startOffset = element1.getTextRange().getEndOffset();
		}
		if(element2 instanceof PsiWhiteSpace)
		{
			endOffset = element2.getTextRange().getStartOffset();
		}
		DotNetExpression expression = PsiTreeUtil.findElementOfClassAtRange(file, startOffset, endOffset, DotNetExpression.class);
		if(expression == null || expression.getTextRange().getEndOffset() != endOffset)
		{
			return null;
		}
		if(expression instanceof CSharpReferenceExpression && expression.getParent() instanceof CSharpMethodCallExpressionImpl)
		{
			return null;
		}
		return expression;
	}
}
