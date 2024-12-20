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

package consulo.csharp.resolve;

import jakarta.annotation.Nonnull;

import consulo.csharp.CSharpMockModuleDescriptor;
import consulo.language.psi.ResolveResult;
import com.intellij.testFramework.TestModuleDescriptor;
import consulo.ide.impl.idea.util.Function;
import consulo.testFramework.ResolvingTestCase;

/**
 * @author VISTALL
 * @since 06.04.2016
 */
public abstract class OtherResolveTest extends ResolvingTestCase
{
	public OtherResolveTest()
	{
		super("/csharp-impl/testData/resolve/other/", "cs");
	}

	public void testIssue278()
	{
	}

	public void testIssue357()
	{
	}

	public void testIssue372()
	{
	}

	public void testIssue451()
	{
	}

	public void testIssue384_csharp()
	{
	}

	public void testIssue384_msil()
	{
	}

	public void testIssue_consulo187()
	{
	}

	public void testIssue_consulo195()
	{
	}

	public void testIssue_consulo196()
	{
	}

	public void testIssue424()
	{
	}

	@Nonnull
	@Override
	protected TestModuleDescriptor createTestModuleDescriptor()
	{
		String testName = getTestName(false);
		String fullDataPath = myFullDataPath;
		return new CSharpMockModuleDescriptor(fullDataPath, testName);
	}

	@Nonnull
	@Override
	protected Function<ResolveResult, String> createReferenceResultBuilder()
	{
		return ResultElementBuilder.INSTANCE;
	}
}
