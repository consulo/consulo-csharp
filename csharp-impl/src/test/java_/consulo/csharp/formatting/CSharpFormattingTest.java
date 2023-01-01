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

package consulo.csharp.formatting;


import consulo.testFramework.FormattingTestCase;

/**
 * @author VISTALL
 * @since 19.04.2015
 */
public abstract class CSharpFormattingTest extends FormattingTestCase
{
	public CSharpFormattingTest()
	{
		super("/csharp-impl/testData/formatting/", "cs");
	}

	@Override
	protected boolean shouldContainTempFiles()
	{
		return false;
	}
	public void testIssue270()
	{
	}

	public void testIssue291()
	{
	}
}
