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

import com.intellij.lexer.Lexer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.tree.IElementType;
import consulo.csharp.lang.CSharpCfsElementTypeFactory;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.doc.psi.CSharpDocElementFactory;
import consulo.csharp.lang.lexer.CSharpLexer;
import consulo.csharp.lang.lexer._CSharpHighlightLexer;
import consulo.injecting.InjectingContainerBuilder;
import consulo.test.light.LightApplicationBuilder;

import javax.annotation.Nonnull;

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
		myDisposable = Disposer.newDisposable();

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
		return new CSharpLexer(new _CSharpHighlightLexer());
	}

	@Override
	protected String getDirPath()
	{
		return "lexerTest";
	}
}
