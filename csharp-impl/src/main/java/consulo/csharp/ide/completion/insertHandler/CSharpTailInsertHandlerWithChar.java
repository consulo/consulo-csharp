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

package consulo.csharp.ide.completion.insertHandler;

import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.editor.completion.lookup.TailType;
import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.LookupElement;

/**
 * @author VISTALL
 * @since 01.05.2016
 */
public class CSharpTailInsertHandlerWithChar implements InsertHandler<LookupElement>
{
	private final TailType myTailType;
	private char myEnterChar;

	public CSharpTailInsertHandlerWithChar(TailType tailType, char enterChar)
	{
		myTailType = tailType;
		myEnterChar = enterChar;
	}

	@Override
	public void handleInsert(InsertionContext context, LookupElement item)
	{
		if(context.getCompletionChar() == myEnterChar)
		{
			context.setAddCompletionChar(false);
			myTailType.processTail(context.getEditor(), context.getTailOffset());
		}
	}
}
