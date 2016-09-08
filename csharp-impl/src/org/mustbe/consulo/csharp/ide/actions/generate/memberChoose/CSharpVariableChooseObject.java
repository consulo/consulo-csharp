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

package org.mustbe.consulo.csharp.ide.actions.generate.memberChoose;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.CSharpElementPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.lombok.annotations.ArrayFactoryFields;

/**
 * @author VISTALL
 * @since 24-Jul-16
 */
@ArrayFactoryFields
public class CSharpVariableChooseObject extends CSharpMemberChooseObject<DotNetVariable>
{
	public CSharpVariableChooseObject(DotNetVariable declaration)
	{
		super(declaration);
	}

	@RequiredReadAction
	@NotNull
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

	@Override
	@RequiredReadAction
	public String getText()
	{
		return myDeclaration.getName();
	}
}
