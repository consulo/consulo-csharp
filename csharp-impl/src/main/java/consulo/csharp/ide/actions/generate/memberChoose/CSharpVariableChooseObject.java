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

package consulo.csharp.ide.actions.generate.memberChoose;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.util.ArrayFactory;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.CSharpElementPresentationUtil;
import consulo.csharp.ide.completion.expected.ExpectedUsingInfo;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetVariable;

/**
 * @author VISTALL
 * @since 24-Jul-16
 */
public class CSharpVariableChooseObject extends CSharpMemberChooseObject<DotNetVariable>
{
	public static final CSharpVariableChooseObject[] EMPTY_ARRAY = new CSharpVariableChooseObject[0];

	public static ArrayFactory<CSharpVariableChooseObject> ARRAY_FACTORY = count -> count == 0 ? EMPTY_ARRAY : new CSharpVariableChooseObject[count];

	public CSharpVariableChooseObject(DotNetVariable declaration)
	{
		super(declaration);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String getPresentationText()
	{
		if(myDeclaration instanceof CSharpFieldDeclaration)
		{
			return CSharpElementPresentationUtil.formatField((DotNetFieldDeclaration) myDeclaration);
		}
		else if(myDeclaration instanceof CSharpPropertyDeclaration)
		{
			return CSharpElementPresentationUtil.formatProperty((CSharpPropertyDeclaration) myDeclaration, CSharpElementPresentationUtil.SCALA_FORMAT);
		}
		throw new IllegalArgumentException(myDeclaration.getClass().getName());
	}

	@RequiredReadAction
	@Nullable
	@Override
	public ExpectedUsingInfo getExpectedUsingInfo()
	{
		return ExpectedUsingInfo.calculateFrom(myDeclaration);
	}

	@Override
	@RequiredReadAction
	public String getText()
	{
		return myDeclaration.getName();
	}
}
