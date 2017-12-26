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

package consulo.csharp.lang.doc.psi;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.impl.source.tree.LazyParseablePsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 03.03.2015
 */
public class CSharpDocRootImpl extends LazyParseablePsiElement implements CSharpDocRoot
{
	public CSharpDocRootImpl(@NotNull IElementType type, @Nullable CharSequence buffer)
	{
		super(type, buffer);
	}

	@Nullable
	public String getTagText(@NotNull String tagName)
	{
		CSharpDocTagImpl tagElement = findTagElement(tagName);
		return tagElement == null ? null : tagElement.getInnerText();
	}

	@Nullable
	public CSharpDocTagImpl findTagElement(@NotNull final String tagName)
	{
		List<CSharpDocTagImpl> tags = findTagElements(tagName);
		return ContainerUtil.getFirstItem(tags);
	}

	@NotNull
	public List<CSharpDocTagImpl> findTagElements(@NotNull final String tagName)
	{
		return ContainerUtil.filter(getTagElements(), docTag -> tagName.equals(docTag.getName()));
	}

	@NotNull
	public CSharpDocTagImpl[] getTagElements()
	{
		return findChildrenByClass(CSharpDocTagImpl.class);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "(" + getElementType().toString() + ")";
	}
}
