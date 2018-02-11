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

package consulo.csharp.lang.parser;

import gnu.trove.THashSet;

import java.util.Arrays;
import java.util.Set;

import javax.annotation.Nonnull;

import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 27.10.2015
 */
public class ModifierSet
{
	public static final ModifierSet EMPTY = new ModifierSet(null);

	@Nonnull
	public static ModifierSet create(IElementType... set)
	{
		return set.length == 0 ? EMPTY : new ModifierSet(new THashSet<>(Arrays.asList(set)));
	}

	@Nonnull
	public static ModifierSet create(@Nonnull Set<IElementType> set)
	{
		return set.isEmpty() ? EMPTY : new ModifierSet(set);
	}

	private Set<IElementType> mySet;

	private boolean myAllowShortObjectInitializer;

	private ModifierSet(Set<IElementType> set)
	{
		mySet = set;
	}

	@Nonnull
	public ModifierSet setAllowShortObjectInitializer()
	{
		ModifierSet set = new ModifierSet(mySet == null ? null : new THashSet<>(mySet));
		set.myAllowShortObjectInitializer = true;
		return set;
	}

	public boolean isAllowShortObjectInitializer()
	{
		return myAllowShortObjectInitializer;
	}

	@Nonnull
	public ModifierSet add(IElementType e)
	{
		Set<IElementType> elementTypes = mySet == null ? new THashSet<>() : new THashSet<>(mySet);
		elementTypes.add(e);
		return create(elementTypes);
	}

	@Nonnull
	public ModifierSet remove(IElementType e)
	{
		if(mySet == null)
		{
			return EMPTY;
		}

		Set<IElementType> elementTypes = new THashSet<>(mySet);
		elementTypes.remove(e);
		return create(elementTypes);
	}

	public boolean isEmpty()
	{
		return mySet == null;
	}

	public boolean contains(IElementType e)
	{
		return mySet != null && mySet.contains(e);
	}
}
