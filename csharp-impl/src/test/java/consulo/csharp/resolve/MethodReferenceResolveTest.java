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

import javax.annotation.Nonnull;

import consulo.csharp.CSharpMockModuleDescriptor;
import com.intellij.psi.ResolveResult;
import com.intellij.testFramework.TestModuleDescriptor;
import com.intellij.util.Function;
import consulo.testFramework.ResolvingTestCase;

/**
 * @author VISTALL
 * @since 06.04.2016
 */
public class MethodReferenceResolveTest extends ResolvingTestCase
{
	public MethodReferenceResolveTest()
	{
		super("/csharp-impl/testData/resolve/methodReference/", "cs");
	}

	public void testIssue134()
	{
	}

	public void testIssue194()
	{
	}

	public void testIssue231()
	{
	}

	public void testIssue292()
	{
	}

	public void testIssue355()
	{
	}

	public void testIssue461()
	{
	}

	public void testIssue406()
	{
	}

	@Nonnull
	@Override
	protected TestModuleDescriptor createTestModuleDescriptor()
	{
		return new CSharpMockModuleDescriptor();
	}

	@Nonnull
	@Override
	protected Function<ResolveResult, String> createReferenceResultBuilder()
	{
		return ResultElementBuilder.INSTANCE;
	}
}
