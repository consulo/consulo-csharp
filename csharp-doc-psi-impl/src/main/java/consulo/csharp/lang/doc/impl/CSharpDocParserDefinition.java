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

package consulo.csharp.lang.doc.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.doc.impl.lexer.DeprecatedCSharpDocLexer;
import consulo.csharp.lang.doc.impl.parser.CSharpDocParser;
import consulo.csharp.lang.doc.impl.psi.CSharpDocTokenType;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IFileElementType;
import consulo.language.ast.TokenSet;
import consulo.language.file.FileViewProvider;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.lexer.Lexer;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;


/**
 * @author VISTALL
 * @since 03.03.2015
 */
@ExtensionImpl
public class CSharpDocParserDefinition implements ParserDefinition
{
	@Override
	public Language getLanguage()
	{
		return CSharpDocLanguage.INSTANCE;
	}

	@Override
	public Lexer createLexer(LanguageVersion languageVersion)
	{
		return new DeprecatedCSharpDocLexer();
	}

	@Override
	public PsiParser createParser(LanguageVersion languageVersion)
	{
		return new CSharpDocParser();
	}

	@Override
	public IFileElementType getFileNodeType()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public TokenSet getWhitespaceTokens(LanguageVersion languageVersion)
	{
		return CSharpDocTokenType.WHITESPACES;
	}

	@Override
	public TokenSet getCommentTokens(LanguageVersion languageVersion)
	{
		return TokenSet.EMPTY;
	}

	@Override
	public TokenSet getStringLiteralElements(LanguageVersion languageVersion)
	{
		return TokenSet.EMPTY;
	}

	@RequiredReadAction
	@Override
	public PsiElement createElement(ASTNode node)
	{
		return new ASTWrapperPsiElement(node);
	}

	@Override
	public PsiFile createFile(FileViewProvider viewProvider)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right)
	{
		return SpaceRequirements.MAY;
	}
}
