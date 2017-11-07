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

package consulo.csharp.lang.util;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 08-Nov-17
 */
public class ContainerUtil2
{
	@NotNull
	@Contract(pure = true)
	public static <T> Collection<T> concat(@NotNull final List<Collection<? extends T>> collections)
	{
		int size = 0;
		for(Collection<? extends T> each : collections)
		{
			size += each.size();
		}

		if(size == 0)
		{
			return Collections.emptyList();
		}

		Collection[] iterables = collections.toArray(new Collection[collections.size()]);
		Iterable<T> iterable = ContainerUtil.<T>concat(iterables);
		final int finalSize = size;
		return new AbstractCollection<T>()
		{
			@NotNull
			@Override
			public Iterator<T> iterator()
			{
				return iterable.iterator();
			}

			@Override
			public int size()
			{
				return finalSize;
			}
		};
	}

	@NotNull
	@Contract(pure = true)
	@SafeVarargs
	public static <T> Collection<T> concat(@NotNull final Collection<? extends T>... collections)
	{
		int size = 0;
		for(Collection<? extends T> each : collections)
		{
			size += each.size();
		}

		if(size == 0)
		{
			return Collections.emptyList();
		}

		Iterable<T> iterable = ContainerUtil.concat(collections);
		final int finalSize = size;
		return new AbstractCollection<T>()
		{
			@NotNull
			@Override
			public Iterator<T> iterator()
			{
				return iterable.iterator();
			}

			@Override
			public int size()
			{
				return finalSize;
			}
		};
	}
}
