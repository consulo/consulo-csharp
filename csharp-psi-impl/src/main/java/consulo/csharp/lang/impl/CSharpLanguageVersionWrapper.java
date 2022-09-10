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

package consulo.csharp.lang.impl;

import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.lexer.CSharpLexer;
import consulo.csharp.lang.impl.parser.CSharpParser;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.language.ast.TokenSet;
import consulo.language.lexer.Lexer;
import consulo.language.parser.PsiParser;
import consulo.language.version.LanguageVersion;
import consulo.language.version.LanguageVersionWithParsing;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 22.11.13.
 */
public class CSharpLanguageVersionWrapper extends LanguageVersion implements LanguageVersionWithParsing
{
	private final CSharpLanguageVersion myLanguageVersion;

	public CSharpLanguageVersionWrapper(CSharpLanguageVersion languageVersion)
	{
		super(languageVersion.name(), languageVersion.getPresentableName(), CSharpLanguage.INSTANCE);
		myLanguageVersion = languageVersion;
	}

	@Nonnull
	@Override
	public PsiParser createParser()
	{
		return new CSharpParser();
	}

	@Nonnull
	@Override
	public Lexer createLexer()
	{
		return new CSharpLexer();
	}

	@Nonnull
	@Override
	public TokenSet getCommentTokens()
	{
		return CSharpTokenSets.COMMENTS;
	}

	@Nonnull
	@Override
	public TokenSet getStringLiteralElements()
	{
		return CSharpTokenSets.LITERALS;
	}

	@Nonnull
	@Override
	public TokenSet getWhitespaceTokens()
	{
		return CSharpTokenSets.WHITESPACES;
	}

	public CSharpLanguageVersion getLanguageVersion()
	{
		return myLanguageVersion;
	}
}
