/*
 * Copyright 2013-2021 consulo.io
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

package consulo.csharp.lang.impl.psi;

import consulo.csharp.lang.CSharpCastType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.util.lang.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author VISTALL
 * @since 23/12/2021
 */
public class CSharpInheritableCheckerContext
{
	private static class Key
	{
		private final DotNetTypeRef myTop;
		private final DotNetTypeRef myTarget;
		private final Pair<CSharpCastType, GlobalSearchScope> myCastResolvingInfo;

		Key(DotNetTypeRef top, DotNetTypeRef target, Pair<CSharpCastType, GlobalSearchScope> castResolvingInfo)
		{
			myTop = top;
			myTarget = target;
			myCastResolvingInfo = castResolvingInfo;
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
			Key key = (Key) o;
			return Objects.equals(myTop, key.myTop) &&
					Objects.equals(myTarget, key.myTarget) &&
					Objects.equals(myCastResolvingInfo, key.myCastResolvingInfo);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(myTop, myTarget, myCastResolvingInfo);
		}
	}

	public static CSharpInheritableCheckerContext create()
	{
		return new CSharpInheritableCheckerContext();
	}

	private Set<Key> myVisited = new LinkedHashSet<>();

	public boolean mark(@Nonnull DotNetTypeRef top, @Nonnull DotNetTypeRef target, @Nullable Pair<CSharpCastType, GlobalSearchScope> castResolvingInfo)
	{
		Key key = new Key(top, target, castResolvingInfo);

		if(myVisited.contains(key))
		{
			return false;
		}

		myVisited.add(key);
		return true;
	}
}
