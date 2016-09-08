package consulo.csharp.lang.doc.parser;

import org.jetbrains.annotations.NotNull;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import consulo.lang.LanguageVersion;

/**
 * @author VISTALL
 * @since 15.09.14
 */
public class CSharpDocParser implements PsiParser
{
	@NotNull
	@Override
	public ASTNode parse(@NotNull IElementType root, @NotNull PsiBuilder builder, @NotNull LanguageVersion languageVersion)
	{
		builder.enforceCommentTokens(TokenSet.EMPTY);

		final PsiBuilder.Marker file = builder.mark();
		new CSharpDocParsing(builder).parse();
		file.done(root);
		return builder.getTreeBuilt();
	}
}
