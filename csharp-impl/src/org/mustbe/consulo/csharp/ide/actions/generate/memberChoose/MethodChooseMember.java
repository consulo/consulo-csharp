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

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.CSharpElementPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpAccessModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;

/**
 * @author VISTALL
 * @since 16.12.14
 */
public class MethodChooseMember extends CSharpMemberChooseObject<CSharpMethodDeclaration>
{
	public MethodChooseMember(CSharpMethodDeclaration declaration)
	{
		super(declaration);
	}

	@Override
	public String getPresentationText()
	{
		return CSharpElementPresentationUtil.formatMethod(myDeclaration, CSharpElementPresentationUtil.METHOD_WITH_RETURN_TYPE |
				CSharpElementPresentationUtil.METHOD_PARAMETER_NAME);
	}

	public void process(@NotNull StringBuilder builder)
	{

	}

	public void processReturn(@NotNull StringBuilder builder)
	{

	}

	@Override
	public String getText()
	{
		StringBuilder builder = new StringBuilder();
		CSharpAccessModifier modifier = CSharpAccessModifier.findModifier(myDeclaration);
		if(modifier != CSharpAccessModifier.NONE)
		{
			for(CSharpModifier sharpModifier : modifier.getModifiers())
			{
				builder.append(sharpModifier.getPresentableText()).append(" ");
			}
		}

		builder.append(getPresentationText());
		builder.append(" {\n");
		processReturn(builder);
		builder.append("}");
		return builder.toString();
	}
}
