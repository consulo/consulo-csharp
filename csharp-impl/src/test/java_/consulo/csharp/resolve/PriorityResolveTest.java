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
import com.intellij.testFramework.TestModuleDescriptor;
import consulo.ide.impl.idea.util.Function;
import consulo.language.psi.ResolveResult;
import consulo.testFramework.ResolvingTestCase;

/**
 * @author VISTALL
 * @since 06.04.2016
 */
public abstract class PriorityResolveTest extends ResolvingTestCase
{
	public PriorityResolveTest()
	{
		super("/csharp-impl/testData/resolve/priority/", "cs");
	}

	public void testIssue208()
	{
	}

	public void testIssue338()
	{
	}

	public void testIssue345()
	{
	}

	public void testIssue367()
	{
	}

	public void testIssue428()
	{
	}

	public void testOperatorWithImplicitCast()
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
