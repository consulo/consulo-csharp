package org.mustbe.consulo.csharp.lang.psi;

import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 15.09.14
 */
public interface CSharpTokensImpl extends CSharpTokens
{
	IElementType LINE_DOC_COMMENT = new IElementType("LINE_DOC_COMMENT", CSharpLanguage.INSTANCE);/*new ILazyParseableElementType
	("LINE_DOC_COMMENT",
	CSharpLanguage.INSTANCE)
	{
		@Override
		protected Language getLanguageForParser(PsiElement psi)
		{
			return XMLLanguage.INSTANCE;
		}

		@Override
		protected ASTNode doParseContents(@NotNull final ASTNode chameleon, @NotNull final PsiElement psi)
		{
			final Project project = psi.getProject();
			final Language languageForParser = getLanguageForParser(psi);
			final LanguageVersion tempLanguageVersion = chameleon.getUserData(LanguageVersion.KEY);
			final LanguageVersion languageVersion = tempLanguageVersion == null ? psi.getLanguageVersion() : tempLanguageVersion;
			final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, new CSharpDocLexer(), languageForParser,
					languageVersion, chameleon.getChars());
			CSharpDocParser parser = new CSharpDocParser();
			return parser.parse(this, builder, languageVersion).getFirstChildNode();
		}

		@Nullable
		@Override
		public ASTNode createNode(CharSequence text)
		{
			return new LazyParseableElement(this, text);
		}
	}; */
}
