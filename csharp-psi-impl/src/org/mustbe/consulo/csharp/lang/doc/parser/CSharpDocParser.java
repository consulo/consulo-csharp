package org.mustbe.consulo.csharp.lang.doc.parser;

import static com.intellij.psi.xml.XmlElementType.XML_START_TAG_START;

import org.jetbrains.annotations.NotNull;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageVersion;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.impl.source.parsing.xml.XmlParsing;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.xml.XmlElementType;

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
		new XmlParsing(builder)
		{
			@Override
			public void parseDocument() {
				final PsiBuilder.Marker document = mark();

				while (isCommentToken(token())) {
					parseComment();
				}

				while (!eof()) {
					final IElementType tt = token();
					if (tt == XML_START_TAG_START) {
						parseTag(true);
					}
					else if (isCommentToken(tt)) {
						parseComment();
					}
					else {
						advance();
					}
				}
				document.done(XmlElementType.XML_DOCUMENT);
			}
		};
		file.done(root);
		return builder.getTreeBuilt();
	}
}
