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

import consulo.csharp.CSharpMockModuleDescriptor;
import com.intellij.testFramework.TestModuleDescriptor;
import consulo.ide.impl.idea.util.Function;
import consulo.language.psi.ResolveResult;
import consulo.testFramework.ResolvingTestCase;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 06.04.2016
 */
public abstract class MethodReferenceResolveTest extends ResolvingTestCase
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
	protected consulo.ide.impl.idea.util.Function<ResolveResult, String> createReferenceResultBuilder()
	{
		return ResultElementBuilder.INSTANCE;
	}
}
