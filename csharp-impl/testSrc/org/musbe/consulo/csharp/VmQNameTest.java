/*
 * Copyright 2013-2016 must-be.org
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

package org.musbe.consulo.csharp;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import consulo.testFramework.OneFileAtProjectTestCase;

/**
 * @author VISTALL
 * @since 08.04.2016
 */
public class VmQNameTest extends OneFileAtProjectTestCase
{
	private static final String ourTarget = "<target>";

	public VmQNameTest()
	{
		super("/csharp-impl/testData/vmqname/", "cs");
	}

	@RequiredReadAction
	public void testNormalType() throws Exception
	{
		assertEqualVmQName("System.Media.Maybe", "Maybe");
	}

	@RequiredReadAction
	public void testGenericType() throws Exception
	{
		assertEqualVmQName("System.Test`4", "Test`4");
	}

	@RequiredReadAction
	public void testNestedGenericType() throws Exception
	{
		assertEqualVmQName("System.Test`4/AA`1", "AA`1");
	}

	@RequiredReadAction
	private void assertEqualVmQName(@NotNull String vmQName, @NotNull String vmName) throws Exception
	{
		String name = getTestName(false);
		String text = loadFile(name + "." + myExtension);

		int index = text.indexOf(ourTarget);
		if(index == -1)
		{
			throw new IllegalArgumentException("no target set");
		}
		text = text.replace(ourTarget, "");

		myFile = createPsiFile(name, text);
		ensureParsed(myFile);

		PsiElement elementAt = myFile.findElementAt(index);
		DotNetTypeDeclaration declaration = PsiTreeUtil.getParentOfType(elementAt, DotNetTypeDeclaration.class);
		if(declaration == null)
		{
			throw new IllegalArgumentException("no type");
		}
		assertEquals("VmQName is not equal", vmQName, declaration.getVmQName());
		assertEquals("VmName is not equal", vmName, declaration.getVmName());
	}
}
