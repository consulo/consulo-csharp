package org.mustbe.consulo.csharp.lang.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.doc.lexer.CSharpDocLexer;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.LanguageVersion;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.PsiParser;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LazyParseableElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;

/**
 * @author VISTALL
 * @since 15.09.14
 */
public interface CSharpTokensImpl extends CSharpTokens
{
	IElementType LINE_DOC_COMMENT = new ILazyParseableElementType("LINE_DOC_COMMENT", CSharpLanguage.INSTANCE)
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
			final PsiParser parser = LanguageParserDefinitions.INSTANCE.forLanguage(languageForParser).createParser(project, languageVersion);
			return parser.parse(this, builder, languageVersion).getFirstChildNode();
		}

		@Nullable
		@Override
		public ASTNode createNode(CharSequence text)
		{
			return new LazyParseableElement(this, text);
		}
	};
}
