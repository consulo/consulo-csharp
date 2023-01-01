/*
 * Copyright 2013-2021 consulo.io
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

package consulo.csharp.impl.ide.completion.util;

import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.InsertHandler;

/**
 * @author VISTALL
 * @since 10/09/2021
 */
public class CSharpWeightInsertHandler implements InsertHandler<LookupElement>
{
	public static final CSharpWeightInsertHandler INSTANCE = new CSharpWeightInsertHandler();

	@Override
	public void handleInsert(InsertionContext insertionContext, LookupElement lookupElement)
	{
		if(insertionContext.getCompletionChar() == ';')
		{
			return;
		}

		int offset = insertionContext.getEditor().getCaretModel().getOffset();
		insertionContext.getDocument().insertString(offset, ";");
		insertionContext.getEditor().getCaretModel().moveToOffset(offset + 1);
	}
}
