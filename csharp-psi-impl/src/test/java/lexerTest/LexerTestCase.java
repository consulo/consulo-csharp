/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
package lexerTest;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NonNls;
import com.intellij.lang.TokenWrapper;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.UsefulTestCase;

/**
 * @author peter
 */
public abstract class LexerTestCase extends UsefulTestCase
{
	protected void doTest(@NonNls String text) throws Exception
	{
		doTest(text, null);
	}

	protected void doTest(@NonNls String text, @Nullable String expected) throws Exception
	{
		String result = printTokens(text, 0);

		if(expected != null)
		{
			assertSameLines(expected, result);
		}
		else
		{
			File parent = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());

			File file = new File(parent, getDirPath() + "/" + getTestName(true) + ".txt");

			assertSameLinesWithFile(file.getPath(), result);
		}
	}

	protected void checkCorrectRestart(String text)
	{
		Lexer mainLexer = createLexer();
		String allTokens = printTokens(text, 0, mainLexer);

		Lexer auxLexer = createLexer();
		auxLexer.start(text);
		while(true)
		{
			IElementType type = auxLexer.getTokenType();
			if(type == null)
			{
				break;
			}
			if(auxLexer.getState() == 0)
			{
				int tokenStart = auxLexer.getTokenStart();
				String subTokens = printTokens(text, tokenStart, mainLexer);
				if(!allTokens.endsWith(subTokens))
				{
					assertEquals("Restarting impossible from offset " + tokenStart + "; lexer state should not return 0 at this point", allTokens, subTokens);
				}
			}
			auxLexer.advance();
		}
	}

	protected String printTokens(String text, int start)
	{
		return printTokens(text, start, createLexer());
	}

	public static String printTokens(CharSequence text, int start, Lexer lexer)
	{
		lexer.start(text, start, text.length());
		String result = "";
		while(true)
		{
			IElementType tokenType = lexer.getTokenType();
			if(tokenType == null)
			{
				break;
			}
			CharSequence tokenText = getTokenText(lexer);
			String tokenTypeName = tokenType.toString();
			String line = tokenTypeName + " ('" + tokenText + "')\n";
			result += line;
			lexer.advance();
		}
		return result;
	}

	protected void doFileTest(@NonNls String fileExt) throws Exception
	{
		File parent = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
		File file = new File(parent, getDirPath() + "/" + getTestName(true) + "." + fileExt);
		String text = "";
		try
		{
			String fileText = FileUtil.loadFile(file);
			text = StringUtil.convertLineSeparators(shouldTrim() ? fileText.trim() : fileText);
		}
		catch(IOException e)
		{
			fail("can't load file " + file + ": " + e.getMessage());
		}
		doTest(text);
	}

	protected boolean shouldTrim()
	{
		return true;
	}

	private static CharSequence getTokenText(Lexer lexer)
	{
		final IElementType tokenType = lexer.getTokenType();
		if(tokenType instanceof TokenWrapper)
		{
			return ((TokenWrapper) tokenType).getValue();
		}

		String text = lexer.getBufferSequence().subSequence(lexer.getTokenStart(), lexer.getTokenEnd()).toString();
		text = StringUtil.replace(text, "\n", "\\n");
		return text;
	}

	protected abstract Lexer createLexer();

	protected abstract String getDirPath();
}
