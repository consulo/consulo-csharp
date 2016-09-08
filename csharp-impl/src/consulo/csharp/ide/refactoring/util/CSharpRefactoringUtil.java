package consulo.csharp.ide.refactoring.util;

import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.psi.CSharpRecursiveElementVisitor;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.UsefulPsiTreeUtil;
import consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetStatement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author Fedor.Korotkov
 *
 * from google dart
 */
public class CSharpRefactoringUtil
{
	public static Set<String> collectUsedNames(@NotNull PsiElement context, @Nullable PsiElement toSkip)
	{
		return new THashSet<String>(ContainerUtil.map(collectUsedComponents(context, toSkip), new Function<PsiNamedElement, String>()
		{
			@Nullable
			@Override
			public String fun(PsiNamedElement componentName)
			{
				return componentName.getName();
			}
		}));
	}

	public static Set<PsiNamedElement> collectUsedComponents(@NotNull PsiElement context, @Nullable PsiElement toSkip)
	{
		final Set<PsiNamedElement> usedComponentNames = new THashSet<PsiNamedElement>();
		PsiTreeUtil.treeWalkUp(new ComponentNameScopeProcessor(usedComponentNames, toSkip), context, null, new ResolveState());
		return usedComponentNames;
	}

	@Nullable
	public static DotNetExpression getSelectedExpression(@NotNull final Project project,
			@NotNull PsiFile file,
			@NotNull final PsiElement element1,
			@NotNull final PsiElement element2)
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

	@NotNull
	public static List<PsiElement> getOccurrences(@NotNull final PsiElement pattern, @Nullable final PsiElement context)
	{
		if(context == null)
		{
			return Collections.emptyList();
		}
		final List<PsiElement> occurrences = new ArrayList<PsiElement>();
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
		return ContainerUtil.map2Array(astResult, PsiElement.class, new Function<ASTNode, PsiElement>()
		{
			@Override
			public PsiElement fun(ASTNode node)
			{
				return node.getPsi();
			}
		});
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
