package org.mustbe.consulo.csharp.lang.formatter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpMacroLanguage;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.formatting.FormattingModelProvider;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;

/**
 * @author VISTALL
 * @since 21.03.14
 */
public class CSharpMacroFormattingBuilder implements FormattingModelBuilder
{
	@NotNull
	@Override
	public FormattingModel createModel(PsiElement psiElement, CodeStyleSettings settings)
	{
		ASTNode node = psiElement.getNode();
		assert node != null;
		PsiFile containingFile = psiElement.getContainingFile().getViewProvider().getPsi(CSharpMacroLanguage.INSTANCE);
		assert containingFile != null : psiElement.getContainingFile();
		ASTNode fileNode = containingFile.getNode();
		assert fileNode != null;
		CSharpMacroFormattingBlock block = new CSharpMacroFormattingBlock(fileNode, null, null);
		return FormattingModelProvider.createFormattingModelForPsiFile(containingFile, block, settings);
	}

	@Nullable
	@Override
	public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset)
	{
		return null;
	}
}
