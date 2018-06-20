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

package consulo.csharp.parsing;

import consulo.csharp.module.extension.CSharpLanguageVersion;

/**
 * @author VISTALL
 * @since 22.05.2015
 */
public abstract class CSharpFailParsingTest extends CSharpParsingTestCase
{
	public CSharpFailParsingTest()
	{
		super("parsing/failParsing");
	}

	@SetLanguageVersion
	public void testFailParsing()
	{
		doTest(true);
	}

	@SetLanguageVersion
	public void testLocalVarParsing()
	{
		doTest(true);
	}

	@SetLanguageVersion
	public void testLambdaParameterListFailParsing()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testIssue438()
	{
		doTest(true);
	}
}
