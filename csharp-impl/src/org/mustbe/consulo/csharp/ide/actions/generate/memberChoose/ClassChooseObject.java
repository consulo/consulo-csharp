/*
 * Copyright 2013-2014 must-be.org
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

import org.mustbe.consulo.dotnet.ide.DotNetElementPresentationUtil;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;

/**
 * @author VISTALL
 * @since 25.06.14
 */
public class ClassChooseObject extends CSharpMemberChooseObject<DotNetTypeDeclaration>
{
	public ClassChooseObject(DotNetTypeDeclaration declaration)
	{
		super(declaration);
	}

	@Override
	public String getPresentationText()
	{
		return DotNetElementPresentationUtil.formatTypeWithGenericParameters(myDeclaration);
	}

	@Override
	public String getText()
	{
		return null;
	}
}
