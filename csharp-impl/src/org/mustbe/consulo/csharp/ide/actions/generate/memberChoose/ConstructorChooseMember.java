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

import org.consulo.lombok.annotations.ArrayFactoryFields;
import org.mustbe.consulo.csharp.ide.CSharpElementPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpAccessModifier;
import org.mustbe.consulo.dotnet.psi.DotNetConstructorDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import com.intellij.codeInsight.generation.ClassMember;

/**
 * @author VISTALL
 * @since 25.06.14
 */
@ArrayFactoryFields
public class ConstructorChooseMember extends CSharpMemberChooseObject<DotNetConstructorDeclaration> implements ClassMember
{
	public ConstructorChooseMember(DotNetConstructorDeclaration declaration)
	{
		super(declaration);
	}

	@Override
	public String getText()
	{
		StringBuilder builder = new StringBuilder();

		CSharpAccessModifier accessModifier = CSharpAccessModifier.findModifier(myDeclaration);
		if(accessModifier != CSharpAccessModifier.NONE)
		{
			builder.append(accessModifier.getPresentableText()).append(" ");
		}

		builder.append("$NAME$(");

		final DotNetParameter[] parameters = myDeclaration.getParameters();

		for(int i = 0; i < parameters.length; i++)
		{
			if(i != 0)
			{
				builder.append(", ");
			}
			DotNetParameter parameter = parameters[i];
			builder.append(parameter.toTypeRef(false).getPresentableText());
			builder.append(" ");
			builder.append(parameter.getName());
		}

		builder.append(")");

		if(parameters.length != 0)
		{
			builder.append(" : base(");
			for(int i = 0; i < parameters.length; i++)
			{
				if(i != 0)
				{
					builder.append(", ");
				}
				DotNetParameter parameter = parameters[i];
				builder.append(parameter.getName());
			}
			builder.append(")");
		}
		builder.append(" {}");
		return builder.toString();
	}

	@Override
	public String getPresentationText()
	{
		return CSharpElementPresentationUtil.formatMethod(myDeclaration, CSharpElementPresentationUtil.METHOD_PARAMETER_NAME);
	}
}
