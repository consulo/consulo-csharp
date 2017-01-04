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

package consulo.csharp.lang.psi.impl.source.resolve.cache;

import java.util.Arrays;

import com.intellij.openapi.util.Comparing;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public class Quaternary<A, B, C, D>
{
	public final A first;
	public final B second;
	public final C third;
	public final D four;

	public Quaternary(A first, B second, C third, D four)
	{
		this.first = first;
		this.second = second;
		this.third = third;
		this.four = four;
	}

	public final A getFirst()
	{
		return first;
	}

	public final B getSecond()
	{
		return second;
	}

	public C getThird()
	{
		return third;
	}

	public D getFour()
	{
		return four;
	}

	public static <A, B, C, D> Quaternary<A, B, C, D> create(A first, B second, C third, D four)
	{
		return new Quaternary<A, B, C, D>(first, second, third, four);
	}

	@Override
	public final boolean equals(Object o)
	{
		return o instanceof Quaternary && Comparing.equal(first, ((Quaternary) o).first) && Comparing.equal(second,
				((Quaternary) o).second) && Comparing.equal(third, ((Quaternary) o).third) && Comparing.equal(four, ((Quaternary) o).four);
	}

	@Override
	public final int hashCode()
	{
		int hashCode = 0;
		if(first != null)
		{
			hashCode += hashCode(first);
		}
		if(second != null)
		{
			hashCode += hashCode(second);
		}
		if(third != null)
		{
			hashCode += hashCode(third);
		}
		if(four != null)
		{
			hashCode += hashCode(four);
		}
		return hashCode;
	}

	private static int hashCode(final Object o)
	{
		return o instanceof Object[] ? Arrays.hashCode((Object[]) o) : o.hashCode();
	}

	@Override
	public String toString()
	{
		return "<" + first + "," + second + "," + third + "," + four + ">";
	}
}