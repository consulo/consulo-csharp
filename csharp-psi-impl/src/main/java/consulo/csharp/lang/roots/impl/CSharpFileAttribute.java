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
import java.util.Objects;

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
		int ver = DataInputOutputUtil.readINT(inputStream);
		int varsHashCode = DataInputOutputUtil.readINT(inputStream);
		return new CSharpFileAttribute((byte) ver, varsHashCode);
	}

	public static void write(DataOutputStream outputStream, CSharpFileAttribute newAttribute) throws IOException
	{
		DataInputOutputUtil.writeINT(outputStream, newAttribute.myLanguageVersion);
		DataInputOutputUtil.writeINT(outputStream, newAttribute.myPreprocessorVariablesHashCode);
	}

	public static final CSharpFileAttribute DEFAULT = new CSharpFileAttribute(CSharpLanguageVersion.HIGHEST, 0);

	private final byte myLanguageVersion;
	private final int myPreprocessorVariablesHashCode;

	public CSharpFileAttribute(CSharpLanguageVersion languageVersion, int preprocessorVariablesHashCode)
	{
		this((byte) languageVersion.ordinal(), preprocessorVariablesHashCode);
	}

	public CSharpFileAttribute(byte languageVersion, int preprocessorVariablesHashCode)
	{
		myLanguageVersion = languageVersion;
		myPreprocessorVariablesHashCode = preprocessorVariablesHashCode;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}
		CSharpFileAttribute that = (CSharpFileAttribute) o;
		return myLanguageVersion == that.myLanguageVersion &&
				myPreprocessorVariablesHashCode == that.myPreprocessorVariablesHashCode;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(myLanguageVersion, myPreprocessorVariablesHashCode);
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("CSharpFileAttribute{");
		sb.append("myLanguageVersion=").append(myLanguageVersion);
		sb.append(", myPreprocessorVariablesHashCode=").append(myPreprocessorVariablesHashCode);
		sb.append('}');
		return sb.toString();
	}
}
