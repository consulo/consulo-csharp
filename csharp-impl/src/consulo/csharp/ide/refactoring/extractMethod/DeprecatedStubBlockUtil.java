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

package consulo.csharp.ide.refactoring.extractMethod;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.DeprecationInfo;
import consulo.internal.dotnet.msil.decompiler.textBuilder.block.LineStubBlock;
import consulo.internal.dotnet.msil.decompiler.textBuilder.block.StubBlock;

/**
 * @author VISTALL
 * @since 02.06.14
 */
@Deprecated
@DeprecationInfo("Need removed header from StubBlockUtil and use it")
public class DeprecatedStubBlockUtil
{
	@NotNull
	public static CharSequence buildText(List<? extends StubBlock> blocks)
	{
		StringBuilder builder = new StringBuilder();

		for(int i = 0; i < blocks.size(); i++)
		{
			if(i != 0)
			{
				builder.append('\n');
			}
			StubBlock stubBlock = blocks.get(i);
			processBlock(builder, stubBlock, 0);
		}

		return builder;
	}

	private static void processBlock(StringBuilder builder, StubBlock root, int index)
	{
		repeatSymbol(builder, '\t', index);
		builder.append(root.getStartText());

		if(!(root instanceof LineStubBlock))
		{
			char[] indents = root.getIndents();
			builder.append('\n');
			repeatSymbol(builder, '\t', index);
			builder.append(indents[0]);
			builder.append('\n');

			List<StubBlock> blocks = root.getBlocks();
			for(int i = 0; i < blocks.size(); i++)
			{
				if(i != 0)
				{
					builder.append('\n');
				}
				StubBlock stubBlock = blocks.get(i);
				processBlock(builder, stubBlock, index + 1);
			}

			CharSequence innerText = root.getInnerText();
			if(innerText != null)
			{
				repeatSymbol(builder, '\t', index + 1);
				builder.append(innerText);
			}

			repeatSymbol(builder, '\t', index);
			builder.append(indents[1]);
			builder.append('\n');
		}
	}

	private static void repeatSymbol(StringBuilder builder, char ch, int count)
	{
		for(int i = 0; i < count; i++)
		{
			builder.append(ch);
		}
	}
}
