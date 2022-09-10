/*
 * Copyright 2013-2018 consulo.io
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

package preprocessorTest;

import org.junit.Assert;
import org.junit.Test;
import consulo.csharp.lang.impl.parser.preprocessor.IfPreprocessorDirective;
import consulo.csharp.lang.impl.parser.preprocessor.PreprocessorDirective;
import consulo.csharp.lang.impl.parser.preprocessor.PreprocessorLightParser;

/**
 * @author VISTALL
 * @since 2018-02-08
 */
public class PreprocessorTest extends Assert
{
	@Test
	public void testSimpleIf()
	{
		PreprocessorDirective directive = PreprocessorLightParser.parse("#if FOO_BAR");

		assertTrue(directive instanceof IfPreprocessorDirective);

		assertEquals(((IfPreprocessorDirective)directive).getValue().trim(), "FOO_BAR");
	}

	@Test
	public void testNegativeIf()
	{
		PreprocessorDirective directive = PreprocessorLightParser.parse("#if !FOO_BAR");

		assertTrue(directive instanceof IfPreprocessorDirective);

		assertEquals(((IfPreprocessorDirective)directive).getValue().trim(), "!FOO_BAR");
	}
}
