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

import consulo.csharp.lang.impl.lexer.CSharpLexer;
import consulo.csharp.lang.impl.lexer._CSharpLexer;
import consulo.language.lexer.RestartableLexer;
import consulo.language.lexer.TokenIterator;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-08
 */
public class CSharpHighlighterLexer extends CSharpLexer implements RestartableLexer
{
	public CSharpHighlighterLexer()
	{
		super(new _CSharpLexer(true));
	}

	@Override
	public int getStartState()
	{
		return getState();
	}

	@Override
	public boolean isRestartableState(int state)
	{
		return false;
	}

	@Override
	public void start(@Nonnull CharSequence buffer, int startOffset, int endOffset, int initialState, TokenIterator tokenIterator)
	{
		start(buffer, startOffset, endOffset, initialState);
	}
}
