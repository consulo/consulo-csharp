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

package consulo.dotnet.libraryAnalyzer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
* @author VISTALL
* @since 08.12.14
*/
public class NamespaceReference
{
	private String myNamespace;
	private String myLibraryName;

	public NamespaceReference(@Nonnull String namespace, @Nullable String libraryName)
	{
		myNamespace = namespace;
		myLibraryName = libraryName;
	}

	public String getNamespace()
	{
		return myNamespace;
	}

	public String getLibraryName()
	{
		return myLibraryName;
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

		if(!myNamespace.equals(that.myNamespace))
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		return myNamespace.hashCode();
	}
}
