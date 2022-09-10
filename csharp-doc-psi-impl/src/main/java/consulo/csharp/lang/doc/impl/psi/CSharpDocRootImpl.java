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

package consulo.csharp.lang.doc.impl.psi;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.csharp.lang.doc.psi.CSharpDocRoot;
import consulo.language.ast.IElementType;
import consulo.util.collection.ContainerUtil;
import consulo.language.impl.psi.LazyParseablePsiElement;

/**
 * @author VISTALL
 * @since 03.03.2015
 */
public class CSharpDocRootImpl extends LazyParseablePsiElement implements CSharpDocRoot
{
	public CSharpDocRootImpl(@Nonnull IElementType type, @Nullable CharSequence buffer)
	{
		super(type, buffer);
	}

	@Nullable
	public String getTagText(@Nonnull String tagName)
	{
		CSharpDocTagImpl tagElement = findTagElement(tagName);
		return tagElement == null ? null : tagElement.getInnerText();
	}

	@Nullable
	public CSharpDocTagImpl findTagElement(@Nonnull final String tagName)
	{
		List<CSharpDocTagImpl> tags = findTagElements(tagName);
		return ContainerUtil.getFirstItem(tags);
	}

	@Nonnull
	public List<CSharpDocTagImpl> findTagElements(@Nonnull final String tagName)
	{
		return ContainerUtil.filter(getTagElements(), docTag -> tagName.equals(docTag.getName()));
	}

	@Nonnull
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
