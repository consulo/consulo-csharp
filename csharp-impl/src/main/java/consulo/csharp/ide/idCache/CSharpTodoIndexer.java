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
import consulo.language.psi.stub.IdAndToDoScannerBasedOnFilterLexer;
import consulo.language.psi.stub.OccurrenceConsumer;
import consulo.language.psi.stub.todo.LexerBasedTodoIndexer;
import consulo.virtualFileSystem.fileType.FileType;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 07-May-17
 */
@ExtensionImpl
public class CSharpTodoIndexer extends LexerBasedTodoIndexer implements IdAndToDoScannerBasedOnFilterLexer
{
	@Override
	public Lexer createLexer(OccurrenceConsumer consumer)
	{
		return CSharpIdIndexer.createIndexingLexer(consumer);
	}

	@Nonnull
	@Override
	public FileType getFileType()
	{
		return CSharpFileType.INSTANCE;
	}
}