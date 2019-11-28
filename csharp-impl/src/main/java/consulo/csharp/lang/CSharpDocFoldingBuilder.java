package consulo.csharp.lang;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.codeInsight.folding.CodeFoldingSettings;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.doc.psi.CSharpDocRoot;

/**
 * @author VISTALL
 * @since 16-Nov-17
 */
public class CSharpDocFoldingBuilder extends FoldingBuilderEx
{
	@RequiredReadAction
	@Nonnull
	@Override
	public FoldingDescriptor[] buildFoldRegions(@Nonnull PsiElement root, @Nonnull Document document, boolean quick)
	{
		// build by CSharpFoldingBuilder
		return FoldingDescriptor.EMPTY;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPlaceholderText(@Nonnull ASTNode node)
	{
		PsiElement psi = node.getPsi();
		if(psi instanceof CSharpDocRoot)
		{
			Document document = PsiDocumentManager.getInstance(psi.getProject()).getDocument(psi.getContainingFile());
			assert document != null;
			int lineNumber = document.getLineNumber(psi.getTextOffset());

			int lineEndOffset = document.getLineEndOffset(lineNumber);

			String firstLineText = document.getText(new TextRange(psi.getTextOffset(), lineEndOffset));
			return firstLineText.trim();
		}
		return null;
	}

	@RequiredReadAction
	@Override
	public boolean isCollapsedByDefault(@Nonnull ASTNode node)
	{
		return CodeFoldingSettings.getInstance().COLLAPSE_DOC_COMMENTS;
	}
}
