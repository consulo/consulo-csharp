/*
 * Copyright 2013-2020 consulo.io
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

package consulo.csharp.impl.ide.highlight;

import consulo.csharp.cfs.impl.CSharpCfsLanguageVersion;
import consulo.csharp.lang.doc.impl.lexer.CSharpDocLexer;
import consulo.csharp.lang.impl.lexer.CSharpLexer;
import consulo.csharp.lang.impl.lexer.CSharpPreprocessorHightlightLexer;
import consulo.csharp.lang.impl.lexer._CSharpLexer;
import consulo.csharp.lang.impl.psi.CSharpTemplateTokens;
import consulo.csharp.lang.impl.psi.CSharpTokensImpl;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.cfs.ide.highlight.CfsSyntaxHighlighter;
import consulo.language.ast.IElementType;
import consulo.language.lexer.LayeredLexer;
import consulo.language.lexer.StringLiteralLexer;

/**
 * @author VISTALL
 * @since 24.01.14
 */
public class CSharpHighlightLexer2 extends LayeredLexer
{
	public CSharpHighlightLexer2()
	{
		super(new CSharpLexer(new _CSharpLexer(true)));

		registerSelfStoppingLayer(new CSharpLexer(new _CSharpLexer(true)), new IElementType[]{CSharpCfsLanguageVersion.getInstance().getExpressionElementType()}, IElementType.EMPTY_ARRAY);

		registerSelfStoppingLayer(new StringLiteralLexer('\"', CSharpTokens.STRING_LITERAL), new IElementType[] {CSharpTokens.STRING_LITERAL}, IElementType.EMPTY_ARRAY);

		registerSelfStoppingLayer(new CSharpDocLexer(), new IElementType[]{CSharpTokensImpl.LINE_DOC_COMMENT}, IElementType.EMPTY_ARRAY);

		registerSelfStoppingLayer(new StringLiteralLexer('\'', CSharpTokens.CHARACTER_LITERAL), new IElementType[] {CSharpTokens.CHARACTER_LITERAL}, IElementType.EMPTY_ARRAY);

		CfsSyntaxHighlighter highlighter = new CfsSyntaxHighlighter(CSharpCfsLanguageVersion.getInstance());

		registerSelfStoppingLayer(highlighter.getHighlightingLexer(), new IElementType[] {CSharpTokensImpl.INTERPOLATION_STRING_LITERAL}, IElementType.EMPTY_ARRAY);

		registerSelfStoppingLayer(new CSharpPreprocessorHightlightLexer(), new IElementType[] {CSharpTemplateTokens.PREPROCESSOR_FRAGMENT}, IElementType.EMPTY_ARRAY);
	}
}
