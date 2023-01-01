/*
 * Copyright 2013-2018 consulo.io
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

import consulo.language.lexer.Lexer;
import consulo.language.ast.IElementType;
import com.intellij.testFramework.LexerTestCase;
import consulo.csharp.cfs.CSharpCfsElementTypeFactory;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.doc.psi.CSharpDocElementFactory;
import consulo.csharp.lang.impl.lexer.CSharpLexer;
import consulo.csharp.lang.lexer._CSharpLexer;
import consulo.disposer.Disposable;
import consulo.disposer.Disposer;
import consulo.injecting.InjectingContainerBuilder;
import consulo.test.light.LightApplicationBuilder;
import consulo.testFramework.AssertEx;
import consulo.testFramework.util.TestUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.net.URISyntaxException;

/**
 * @author VISTALL
 * @since 2018-07-10
 */
public class CSharpHightlightLexerTest extends LexerTestCase
{
	private Disposable myDisposable;

	@Override
	protected void setUp() throws Exception
	{
		myDisposable = Disposable.newDisposable();

		LightApplicationBuilder.DefaultRegistrator registrator = new LightApplicationBuilder.DefaultRegistrator()
		{
			@Override
			public void registerServices(@Nonnull InjectingContainerBuilder builder)
			{
				super.registerServices(builder);

				builder.bind(CSharpCfsElementTypeFactory.class).to(new CSharpCfsElementTypeFactory()
				{

					@Nonnull
					@Override
					public IElementType getInterpolationStringElementType()
					{
						return new IElementType("STRING_INTERPOLATION", CSharpLanguage.INSTANCE);
					}
				});

				builder.bind(CSharpDocElementFactory.class).to(new CSharpDocElementFactory()
				{
					@Nonnull
					@Override
					public IElementType getDocRootElementType()
					{
						return new IElementType("DOC_COMMENT", CSharpLanguage.INSTANCE);
					}
				});
			}
		};

		LightApplicationBuilder builder = LightApplicationBuilder.create(myDisposable, registrator);

		builder.build();

		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
		Disposer.dispose(myDisposable);
	}

	public void testWithStringInterpolationInside() throws Exception
	{
		doTest("$\"some variable {     $\"someValue\"      } asdasdasdas\"");
	}

	public void testHightlightStringInterpolation() throws Exception
	{
		doTest("$\"some variable {\"dasdas\"} asdasdasdas\"\n" +
				"\n" +
				"$\"some variable {'char'} asdasdasdas\"\n" +
				"\n" +
				"$\"some variable {if} asdasdasdas\"\n" +
				"\n" +
				"$\"some variable {$\"someValue\"} asdasdasdas\"\n" +
				"\n" +
				"$\"some variable {@\"someValue\"} asdasdasdas\"\n" +
				"\n" +
				"$\"some variable {someValue} asdasdasdas\"");
	}

	@Override
	protected void doTest(String text, @Nullable String expected)
	{
		String result = printTokens(text, 0);

		if(expected != null)
		{
			AssertEx.assertSameLines(expected, result);
		}
		else
		{
			try
			{
				File file = new File(CSharpHightlightLexerTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());

				File testFile = new File(file, getDirPath() + "/" + TestUtil.getTestName(this, true) + ".txt");

				AssertEx.assertSameLinesWithFile(testFile.getPath(), result);
			}
			catch(URISyntaxException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	public void testHightlightStringInterpolationWithFormat() throws Exception
	{
		doTest("using System;\n" +
				"\n" +
				"public class Program {\n" +
				"\n" +
				"    public static void Main() {\n" +
				"        int value = 1;\n" +
				"        bool v = false;\n" +
				"        String atr = $\"some value {value:####} fsafas\";\n" +
				"        Console.WriteLine(atr);\n" +
				"\n" +
				"\n" +
				"    }\n" +
				"}\n");
	}

	@Override
	protected Lexer createLexer()
	{
		return new CSharpLexer(new _CSharpLexer(true));
	}

	@Override
	protected String getDirPath()
	{
		return "lexerTest";
	}
}
