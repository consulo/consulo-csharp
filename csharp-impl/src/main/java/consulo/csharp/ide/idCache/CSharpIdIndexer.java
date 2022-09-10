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

package consulo.csharp.ide.idCache;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpFileType;
import consulo.language.lexer.Lexer;
import consulo.language.psi.stub.LexerBasedIdIndexer;
import consulo.language.psi.stub.OccurrenceConsumer;
import consulo.csharp.lang.impl.lexer.CSharpLexer;
import consulo.virtualFileSystem.fileType.FileType;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 07-May-17
 */
@ExtensionImpl
public class CSharpIdIndexer extends LexerBasedIdIndexer
{
	@Override
	public Lexer createLexer(final OccurrenceConsumer consumer)
	{
		return createIndexingLexer(consumer);
	}

	public static Lexer createIndexingLexer(OccurrenceConsumer consumer)
	{
		return new CSharpIdFilterLexer(new CSharpLexer(), consumer);
	}

	@Nonnull
	@Override
	public FileType getFileType()
	{
		return CSharpFileType.INSTANCE;
	}
}