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

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.ide.ServiceManager;
import jakarta.annotation.Nullable;
import jakarta.inject.Singleton;

import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 03.03.2015
 */
@Singleton
@ServiceAPI(ComponentScope.APPLICATION)
@ServiceImpl
public class CSharpDocTagManager
{
	@Nonnull
	public static CSharpDocTagManager getInstance()
	{
		return ServiceManager.getService(CSharpDocTagManager.class);
	}

	private Map<String, CSharpDocTagInfo> myTags = new HashMap<String, CSharpDocTagInfo>();

	public CSharpDocTagManager()
	{
		addTag(new CSharpDocTagInfo("c"));
		addTag(new CSharpDocTagInfo("code"));
		addTag(new CSharpDocTagInfo("see").add(new CSharpDocAttributeInfo("cref", CSharpDocAttributeInfo.ValueType.REFERENCE)));
		addTag(new CSharpDocTagInfo("example"));
		addTag(new CSharpDocTagInfo("exception").add(new CSharpDocAttributeInfo("cref", CSharpDocAttributeInfo.ValueType.REFERENCE)));
		addTag(new CSharpDocTagInfo("include").add(new CSharpDocAttributeInfo("file", CSharpDocAttributeInfo.ValueType.TEXT)).add(new
				CSharpDocAttributeInfo("path", CSharpDocAttributeInfo.ValueType.TEXT)));
		addTag(new CSharpDocTagInfo("list"));
		addTag(new CSharpDocTagInfo("para"));
		addTag(new CSharpDocTagInfo("param").add(new CSharpDocAttributeInfo("name", CSharpDocAttributeInfo.ValueType.PARAMETER)));
		addTag(new CSharpDocTagInfo("paramref").add(new CSharpDocAttributeInfo("name", CSharpDocAttributeInfo.ValueType.PARAMETER)));
		addTag(new CSharpDocTagInfo("permission").add(new CSharpDocAttributeInfo("member", CSharpDocAttributeInfo.ValueType.TEXT)));
		addTag(new CSharpDocTagInfo("remarks"));
		addTag(new CSharpDocTagInfo("returns"));
		addTag(new CSharpDocTagInfo("seealso").add(new CSharpDocAttributeInfo("member", CSharpDocAttributeInfo.ValueType.TEXT)));
		addTag(new CSharpDocTagInfo("summary"));
		addTag(new CSharpDocTagInfo("typeparam").add(new CSharpDocAttributeInfo("name", CSharpDocAttributeInfo.ValueType.TYPE_PARAMETER)));
		addTag(new CSharpDocTagInfo("typeparamref").add(new CSharpDocAttributeInfo("name", CSharpDocAttributeInfo.ValueType.TYPE_PARAMETER)));
		addTag(new CSharpDocTagInfo("value"));
	}

	private void addTag(CSharpDocTagInfo tagInfo)
	{
		myTags.put(tagInfo.getName(), tagInfo);
	}

	@Nonnull
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
