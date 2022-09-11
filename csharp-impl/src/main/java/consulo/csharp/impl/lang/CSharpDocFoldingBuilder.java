package consulo.csharp.impl.lang;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.doc.psi.CSharpDocRoot;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.editor.folding.CodeFoldingSettings;
import consulo.language.editor.folding.FoldingBuilderEx;
import consulo.language.editor.folding.FoldingDescriptor;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 16-Nov-17
 */
@ExtensionImpl
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

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
