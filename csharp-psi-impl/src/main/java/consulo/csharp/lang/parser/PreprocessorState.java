/*
 * Copyright 2013-2019 consulo.io
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * @author VISTALL
 * @since 2019-08-09
 */
class PreprocessorState
{
	public static class SubState
	{
		Deque<Boolean> ifDirectives = new ArrayDeque<>();

		public SubState(@Nonnull Boolean initialValue)
		{
			ifDirectives.add(initialValue);
		}

		boolean haveActive()
		{
			for(Boolean state : ifDirectives)
			{
				if(state)
				{
					return true;
				}
			}
			return false;
		}

		boolean isActive()
		{
			Boolean value = ifDirectives.peekLast();
			return value != null && value;
		}
	}

	private Deque<SubState> myStates = new ArrayDeque<>();

	@Nonnull
	public SubState newState(@Nonnull Boolean value)
	{
		SubState e = new SubState(value);
		myStates.addLast(e);
		return e;
	}

	@Nullable
	public SubState last()
	{
		return myStates.peekLast();
	}

	@Nullable
	public SubState removeLast()
	{
		return myStates.pollLast();
	}

	public boolean isDisabled(boolean skipCurrent)
	{
		if(myStates.isEmpty())
		{
			return false;
		}

		SubState last = last();

		Iterator<SubState> iterator = myStates.descendingIterator();

		while(iterator.hasNext())
		{
			SubState next = iterator.next();

			if(skipCurrent && last == next)
			{
				continue;
			}

			if(!next.isActive())
			{
				return true;
			}
		}

		return false;
	}
}
