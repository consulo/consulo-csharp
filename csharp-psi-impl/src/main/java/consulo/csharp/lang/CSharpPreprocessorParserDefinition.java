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

package consulo.csharp.lang;

import javax.annotation.Nonnull;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import consulo.csharp.lang.lexer._CSharpMacroLexer;
import consulo.csharp.lang.parser.CSharpPreprocessorParser;
import consulo.csharp.lang.psi.CSharpPreprocesorTokens;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.lang.LanguageVersion;

/**
 * @author VISTALL
 * @since 18-May-17
 */
public class CSharpPreprocessorParserDefinition implements ParserDefinition
{
	private static final IFileElementType ourFileElementType = new IFileElementType(CSharpPreprocessorLanguage.INSTANCE);

	@Nonnull
	@Override
	public Lexer createLexer(@Nonnull LanguageVersion languageVersion)
	{
		return new _CSharpMacroLexer();
	}

	@Nonnull
	@Override
	public PsiParser createParser(@Nonnull LanguageVersion languageVersion)
	{
		return new CSharpPreprocessorParser();
	}

	@Nonnull
	@Override
	public IFileElementType getFileNodeType()
	{
		return ourFileElementType;
	}

	@Nonnull
	@Override
	public TokenSet getWhitespaceTokens(@Nonnull LanguageVersion languageVersion)
	{
		return TokenSet.create(CSharpTokens.WHITE_SPACE);
	}

	@Nonnull
	@Override
	public TokenSet getCommentTokens(@Nonnull LanguageVersion languageVersion)
	{
		return TokenSet.create(CSharpPreprocesorTokens.LINE_COMMENT);
	}

	@Nonnull
	@Override
	public TokenSet getStringLiteralElements(@Nonnull LanguageVersion languageVersion)
	{
		return TokenSet.EMPTY;
	}

	@Override
	public PsiFile createFile(@Nonnull FileViewProvider viewProvider)
	{
		throw new IllegalArgumentException();
	}

	@Nonnull
	@Override
	public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right)
	{
		return SpaceRequirements.MAY;
	}
}
