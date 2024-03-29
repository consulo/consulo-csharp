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

package consulo.csharp.impl.ide.completion;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpFileType;
import consulo.language.editor.completion.lookup.CharFilter;
import consulo.language.psi.PsiFile;
import consulo.language.editor.completion.lookup.Lookup;

/**
 * @author VISTALL
 * @since 03.01.15
 */
@ExtensionImpl
public class CSharpCompletionCharFilter extends CharFilter
{
	@Override
	public Result acceptChar(char c, final int prefixLength, final Lookup lookup)
	{
		if(!lookup.isCompletion())
		{
			return null;
		}

		PsiFile psiFile = lookup.getPsiFile();
		if(psiFile.getFileType() != CSharpFileType.INSTANCE)
		{
			return null;
		}

		if(Character.isJavaIdentifierPart(c))
		{
			return Result.ADD_TO_PREFIX;
		}

		switch(c)
		{
			case '@':
				return Result.ADD_TO_PREFIX;
			case '{':
			case '<':
				return Result.SELECT_ITEM_AND_FINISH_LOOKUP;
			default:
				return null;
		}
	}
}
