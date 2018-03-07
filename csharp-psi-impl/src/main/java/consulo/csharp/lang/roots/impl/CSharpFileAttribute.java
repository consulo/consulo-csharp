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

package consulo.csharp.lang.roots.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.intellij.util.io.DataInputOutputUtil;
import consulo.csharp.module.extension.CSharpLanguageVersion;

/**
 * @author VISTALL
 * @since 2018-03-04
 */
public class CSharpFileAttribute
{
	protected static CSharpFileAttribute read(DataInputStream inputStream) throws IOException
	{
		List<String> oldValue = DataInputOutputUtil.readSeq(inputStream, inputStream::readUTF);
		int ver = DataInputOutputUtil.readINT(inputStream);
		return new CSharpFileAttribute(oldValue, (byte) ver);
	}

	public static void write(DataOutputStream outputStream, CSharpFileAttribute newAttribute) throws IOException
	{
		DataInputOutputUtil.writeSeq(outputStream, newAttribute.values, outputStream::writeUTF);
		DataInputOutputUtil.writeINT(outputStream, newAttribute.languageVersion);
	}

	public static final CSharpFileAttribute DEFAULT = new CSharpFileAttribute(Collections.emptyList(), CSharpLanguageVersion.HIGHEST);

	private final Collection<String> values;
	private final byte languageVersion;

	public CSharpFileAttribute(Collection<String> values, CSharpLanguageVersion languageVersion)
	{
		this(values, (byte) languageVersion.ordinal());
	}

	public CSharpFileAttribute(Collection<String> values, byte languageVersion)
	{
		this.values = values;
		this.languageVersion = languageVersion;
	}

	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof CSharpFileAttribute))
		{
			return false;
		}

		CSharpFileAttribute other = (CSharpFileAttribute) o;

		if(!equal(values, other.values))
		{
			return false;
		}

		return languageVersion == other.languageVersion;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("CSharpFileAttribute{");
		sb.append("values=").append(values);
		sb.append(", languageVersion=").append(languageVersion);
		sb.append('}');
		return sb.toString();
	}

	private static boolean equal(Collection<String> o1, Collection<String> o2)
	{
		if(o1.size() != o2.size())
		{
			return false;
		}

		for(String value : o1)
		{
			if(!o2.contains(value))
			{
				return false;
			}
		}
		return true;
	}
}
