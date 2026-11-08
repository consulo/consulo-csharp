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

package consulo.csharp.impl.libraryAnalyzer;

import consulo.dotnet.assembly.summary.DotNetAddableLibrary;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
* @author VISTALL
* @since 08.12.14
*/
public class NamespaceReference
{
	private String myNamespace;
	@Nullable
	private DotNetAddableLibrary myLibrary;

	/**
	 * @param library the library to add with the using - a library the module does not reference yet; null if it does
	 */
	public NamespaceReference(String namespace, @Nullable DotNetAddableLibrary library)
	{
		myNamespace = namespace;
		myLibrary = library;
	}

	public String getNamespace()
	{
		return myNamespace;
	}

	@Nullable
	public String getLibraryName()
	{
		return myLibrary == null ? null : myLibrary.getName();
	}

	@Nullable
	public DotNetAddableLibrary getLibrary()
	{
		return myLibrary;
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

		NamespaceReference that = (NamespaceReference) o;

		// the same namespace from two libraries is two choices
		return myNamespace.equals(that.myNamespace) && Objects.equals(getLibraryName(), that.getLibraryName());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(myNamespace, getLibraryName());
	}
}
