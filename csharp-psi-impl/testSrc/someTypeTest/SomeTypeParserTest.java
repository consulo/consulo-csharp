/*
 * Copyright 2013-2014 must-be.org
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

package someTypeTest;

import org.junit.Assert;
import org.junit.Test;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.typeParsing.SomeType;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.typeParsing.SomeTypeParser;

/**
 * @author VISTALL
 * @since 11.07.14
 */
public class SomeTypeParserTest extends Assert
{
	@Test
	public void test1()
	{
		String s = "System.Collections.Generic.List<System.Collections.Generic.Dictionary<int,System.String>,System.String>";

		SomeType someType = SomeTypeParser.parseType(s);

		assertEquals(someType.toString(), s);
	}

	@Test
	public void test2()
	{
		String s = "System.Collections.Generic.List<System.String>";

		SomeType someType = SomeTypeParser.parseType(s);

		assertEquals(someType.toString(), s);
	}

	@Test
	public void test3()
	{
		String s = "System.Collections.Generic.List";

		SomeType someType = SomeTypeParser.parseType(s);

		assertEquals(someType.toString(), s);
	}
}
