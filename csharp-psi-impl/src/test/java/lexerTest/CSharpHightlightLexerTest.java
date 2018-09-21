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

import javax.annotation.Nonnull;

import com.intellij.lexer.Lexer;
import com.intellij.mock.MockApplication;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.tree.IElementType;
import consulo.csharp.lang.CSharpCfsElementTypeFactory;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.doc.psi.CSharpDocElementFactory;
import consulo.csharp.lang.lexer._CSharpHighlightLexer;

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

		MockApplication mockApplication = new MockApplication(myDisposable);
		ApplicationManager.setApplication(mockApplication, myDisposable);

		mockApplication.registerService(CSharpCfsElementTypeFactory.class, new CSharpCfsElementTypeFactory()
		{
			@Nonnull
			@Override
			public IElementType getInterpolationStringElementType()
			{
				return new IElementType("STRING_INTERPOLATION", CSharpLanguage.INSTANCE);
			}
		});

		mockApplication.registerService(CSharpDocElementFactory.class, new CSharpDocElementFactory()
		{
			@Nonnull
			@Override
			public IElementType getDocRootElementType()
			{
				return new IElementType("DOC_COMMENT", CSharpLanguage.INSTANCE);
			}
		});

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

	@Override
	protected Lexer createLexer()
	{
		return new _CSharpHighlightLexer();
	}

	@Override
	protected String getDirPath()
	{
		return "lexerTest";
	}
}