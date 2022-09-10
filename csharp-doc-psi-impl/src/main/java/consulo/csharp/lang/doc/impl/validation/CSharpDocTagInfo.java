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

package consulo.csharp.lang.doc.impl.validation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 03.03.2015
 */
public class CSharpDocTagInfo
{
	private Map<String, CSharpDocAttributeInfo> myAttributes = new HashMap<String, CSharpDocAttributeInfo>();

	private final String myName;

	public CSharpDocTagInfo(String name)
	{
		myName = name;
	}

	@Nonnull
	public CSharpDocTagInfo add(@Nonnull CSharpDocAttributeInfo a)
	{
		myAttributes.put(a.getName(), a);
		return this;
	}

	@Nonnull
	public Collection<CSharpDocAttributeInfo> getAttributes()
	{
		return myAttributes.values();
	}

	@Nullable
	public CSharpDocAttributeInfo getAttribute(String tagName)
	{
		return myAttributes.get(tagName);
	}

	public String getName()
	{
		return myName;
	}
}
