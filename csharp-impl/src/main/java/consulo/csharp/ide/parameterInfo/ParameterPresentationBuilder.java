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

package consulo.csharp.ide.parameterInfo;

import java.util.HashMap;
import java.util.Map;

import org.checkerframework.common.aliasing.qual.NonLeaked;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.UnfairTextRange;
import com.intellij.xml.util.XmlStringUtil;
import consulo.util.lang.ObjectUtil;

/**
 * @author VISTALL
 * @since 2020-05-31
 */
public class ParameterPresentationBuilder<P>
{
	private static final TextRange ourInvalidRange = new UnfairTextRange(-1, -1);

	private StringBuilder myBuilder = new StringBuilder();

	private Map<Integer, TextRange> myRanges = new HashMap<>();

	public ParameterPresentationBuilder()
	{
	}

	public AccessToken beginParameter(int index)
	{
		int start = myBuilder.length();
		return new AccessToken()
		{
			@Override
			public void finish()
			{
				myRanges.put(index, new TextRange(start, myBuilder.length()));
			}
		};
	}

	public void add(String text)
	{
		myBuilder.append(text);
	}

	public void addEscaped(String text)
	{
		myBuilder.append(XmlStringUtil.escapeString(text));
	}

	public void addSpace()
	{
		myBuilder.append(" ");
	}

	@NonLeaked
	public TextRange getParameterRange(int index)
	{
		TextRange textRange = myRanges.get(index);
		return ObjectUtil.notNull(textRange, ourInvalidRange);
	}

	@Override
	public String toString()
	{
		return myBuilder.toString();
	}
}
