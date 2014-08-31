/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.source.injection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.util.ProperTextRange;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiLanguageInjectionHost;

/**
 * @author VISTALL
 * @since 31.08.14
 * <p/>
 * Based on java impl
 */
public class CSharpStringLiteralEscaper<T extends PsiLanguageInjectionHost> extends LiteralTextEscaper<T>
{
	public static boolean parseStringCharacters(@NotNull String chars, @NotNull StringBuilder outChars, @Nullable int[] sourceOffsets)
	{
		assert sourceOffsets == null || sourceOffsets.length == chars.length() + 1;
		if(chars.indexOf('\\') < 0)
		{
			outChars.append(chars);
			if(sourceOffsets != null)
			{
				for(int i = 0; i < sourceOffsets.length; i++)
				{
					sourceOffsets[i] = i;
				}
			}
			return true;
		}
		int index = 0;
		final int outOffset = outChars.length();
		while(index < chars.length())
		{
			char c = chars.charAt(index++);
			if(sourceOffsets != null)
			{
				sourceOffsets[outChars.length() - outOffset] = index - 1;
				sourceOffsets[outChars.length() + 1 - outOffset] = index;
			}
			if(c != '\\')
			{
				outChars.append(c);
				continue;
			}
			if(index == chars.length())
			{
				return false;
			}
			c = chars.charAt(index++);
			switch(c)
			{
				case 'b':
					outChars.append('\b');
					break;

				case 't':
					outChars.append('\t');
					break;

				case 'n':
					outChars.append('\n');
					break;

				case 'f':
					outChars.append('\f');
					break;

				case 'r':
					outChars.append('\r');
					break;

				case '"':
					outChars.append('"');
					break;

				case '\'':
					outChars.append('\'');
					break;

				case '\\':
					outChars.append('\\');
					break;

				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
					char startC = c;
					int v = (int) c - '0';
					if(index < chars.length())
					{
						c = chars.charAt(index++);
						if('0' <= c && c <= '7')
						{
							v <<= 3;
							v += c - '0';
							if(startC <= '3' && index < chars.length())
							{
								c = chars.charAt(index++);
								if('0' <= c && c <= '7')
								{
									v <<= 3;
									v += c - '0';
								}
								else
								{
									index--;
								}
							}
						}
						else
						{
							index--;
						}
					}
					outChars.append((char) v);
					break;

				case 'u':
					// uuuuu1234 is valid too
					while(index != chars.length() && chars.charAt(index) == 'u')
					{
						index++;
					}
					if(index + 4 <= chars.length())
					{
						try
						{
							int code = Integer.parseInt(chars.substring(index, index + 4), 16);
							//line separators are invalid here
							if(code == 0x000a || code == 0x000d)
							{
								return false;
							}
							c = chars.charAt(index);
							if(c == '+' || c == '-')
							{
								return false;
							}
							outChars.append((char) code);
							index += 4;
						}
						catch(Exception e)
						{
							return false;
						}
					}
					else
					{
						return false;
					}
					break;

				default:
					return false;
			}
			if(sourceOffsets != null)
			{
				sourceOffsets[outChars.length() - outOffset] = index;
			}
		}
		return true;
	}


	private int[] myOutSourceOffsets;

	public CSharpStringLiteralEscaper(@NotNull T host)
	{
		super(host);
	}

	@Override
	public boolean decode(@NotNull final TextRange rangeInsideHost, @NotNull StringBuilder outChars)
	{
		ProperTextRange.assertProperRange(rangeInsideHost);
		String subText = rangeInsideHost.substring(myHost.getText());
		myOutSourceOffsets = new int[subText.length() + 1];
		return parseStringCharacters(subText, outChars, myOutSourceOffsets);
	}

	@Override
	public int getOffsetInHost(int offsetInDecoded, @NotNull final TextRange rangeInsideHost)
	{
		int result = offsetInDecoded < myOutSourceOffsets.length ? myOutSourceOffsets[offsetInDecoded] : -1;
		if(result == -1)
		{
			return -1;
		}
		return (result <= rangeInsideHost.getLength() ? result : rangeInsideHost.getLength()) + rangeInsideHost.getStartOffset();
	}

	@Override
	public boolean isOneLine()
	{
		return true;
	}
}
