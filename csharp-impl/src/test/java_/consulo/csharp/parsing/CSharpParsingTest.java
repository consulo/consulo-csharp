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
 * @since 30.11.13.
 */
public abstract class CSharpParsingTest extends CSharpParsingTestCase
{
	public CSharpParsingTest()
	{
		super("parsing");
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testVarVar()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testUsingStatic()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._5_0)
	public void testSoftKeywords()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._5_0)
	public void testGenericParameters()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testNameOf()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testIssue40()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testIssue70()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testIssue89()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testIssue121()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testIssue191()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testIssue233()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testIssue240()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testIssue246()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testIssue248()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testIssue251()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testIssue267()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testIssue469()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testIssue470()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._5_0)
	public void testNameOfNotAllowed()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testNamespaceInsideClass()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testEnumParsing()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testRootAttribute()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testAssemblyAttributeBeforeAndAfterMember()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testWhereWhere()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testIssue438()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testIssue441()
	{
		doTest(true);
	}

	@SetLanguageVersion(version = CSharpLanguageVersion._6_0)
	public void testIssue475()
	{
		doTest(true);
	}
}
