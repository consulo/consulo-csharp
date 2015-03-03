/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.lang.doc.validation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.consulo.annotations.Immutable;
import org.consulo.lombok.annotations.ApplicationService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author VISTALL
 * @since 03.03.2015
 */
@ApplicationService
public class CSharpDocTagManager
{
	private Map<String, CSharpDocTagInfo> myTags = new HashMap<String, CSharpDocTagInfo>();

	public CSharpDocTagManager()
	{
		addTag(new CSharpDocTagInfo("c"));
		addTag(new CSharpDocTagInfo("code"));
		addTag(new CSharpDocTagInfo("see"));
		addTag(new CSharpDocTagInfo("example"));
		addTag(new CSharpDocTagInfo("exception").add(new CSharpDocAttributeInfo("cref")));
		addTag(new CSharpDocTagInfo("include"));
		addTag(new CSharpDocTagInfo("list"));
		addTag(new CSharpDocTagInfo("para"));
		addTag(new CSharpDocTagInfo("param"));
		addTag(new CSharpDocTagInfo("paramref"));
		addTag(new CSharpDocTagInfo("permission"));
		addTag(new CSharpDocTagInfo("remarks"));
		addTag(new CSharpDocTagInfo("returns"));
		addTag(new CSharpDocTagInfo("seealso"));
		addTag(new CSharpDocTagInfo("summary"));
		addTag(new CSharpDocTagInfo("typeparam"));
		addTag(new CSharpDocTagInfo("typeparamref"));
		addTag(new CSharpDocTagInfo("value"));
	}

	private void addTag(CSharpDocTagInfo tagInfo)
	{
		myTags.put(tagInfo.getName(), tagInfo);
	}

	@NotNull
	@Immutable
	public Collection<CSharpDocTagInfo> getTags()
	{
		return myTags.values();
	}

	@Nullable
	public CSharpDocTagInfo getTag(String tagName)
	{
		return myTags.get(tagName);
	}
}
