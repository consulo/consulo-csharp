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

package consulo.csharp.ide.highlight;

import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.StringLiteralLexer;
import com.intellij.psi.tree.IElementType;
import consulo.csharp.cfs.ide.highlight.CfsSyntaxHighlighter;
import consulo.csharp.lang.CSharpCfsLanguageVersion;
import consulo.csharp.lang.doc.lexer.CSharpDocLexer;
import consulo.csharp.lang.lexer.CSharpLexer;
import consulo.csharp.lang.lexer.CSharpPreprocessorHightlightLexer;
import consulo.csharp.lang.lexer._CSharpLexer;
import consulo.csharp.lang.psi.CSharpTemplateTokens;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTokensImpl;

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
