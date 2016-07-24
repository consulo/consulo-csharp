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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.CSharpElementPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpAccessModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.dotnet.psi.DotNetConstructorDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import consulo.lombok.annotations.ArrayFactoryFields;

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

	@RequiredReadAction
	public String getText(Collection<? extends CSharpVariableChooseObject> additionalParameters)
	{
		StringBuilder builder = new StringBuilder();

		CSharpAccessModifier accessModifier = CSharpAccessModifier.findModifier(myDeclaration);
		if(accessModifier != CSharpAccessModifier.NONE)
		{
			builder.append(accessModifier.getPresentableText()).append(" ");
		}

		builder.append("$NAME$(");

		final DotNetParameter[] parameters = myDeclaration.getParameters();

		final List<DotNetVariable> targets = new ArrayList<DotNetVariable>(parameters.length + additionalParameters.size());
		ContainerUtil.addAll(targets, parameters);
		for(CSharpVariableChooseObject additionalParameter : additionalParameters)
		{
			targets.add(additionalParameter.getDeclaration());
		}

		for(int i = 0; i < targets.size(); i++)
		{
			if(i != 0)
			{
				builder.append(", ");
			}
			DotNetVariable parameter = targets.get(i);
			CSharpTypeRefPresentationUtil.appendTypeRef(myDeclaration, builder, parameter.toTypeRef(true), CSharpTypeRefPresentationUtil.QUALIFIED_NAME_WITH_KEYWORD);
			builder.append(" ");
			builder.append(getParameterName(parameter));
		}

		builder.append(")");

		if(!targets.isEmpty())
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
		builder.append(" {\n");
		for(CSharpVariableChooseObject additionalParameter : additionalParameters)
		{
			DotNetVariable declaration = additionalParameter.getDeclaration();
			String parameterName = getParameterName(declaration);
			String declarationName = declaration.getName();

			if(parameterName.equals(declarationName))
			{
				builder.append("this.");
			}
			builder.append(declarationName).append(" = ").append(parameterName).append(";\n");
		}
		builder.append("}");
		return builder.toString();
	}

	@RequiredReadAction
	private static String getParameterName(DotNetVariable variable)
	{
		if(variable instanceof DotNetParameter)
		{
			return variable.getName();
		}

		String name = variable.getName();
		assert name != null;
		char ch = name.charAt(0);
		if(Character.isUpperCase(ch))
		{
			return StringUtil.decapitalize(name);
		}
		else if(ch == '_')
		{
			return name.substring(1, name.length());
		}
		return name;
	}

	@Override
	@RequiredReadAction
	public String getText()
	{
		return getText(Collections.<CSharpVariableChooseObject>emptyList());
	}

	@RequiredReadAction
	@NotNull
	@Override
	public String getPresentationText()
	{
		return CSharpElementPresentationUtil.formatMethod(myDeclaration, CSharpElementPresentationUtil.METHOD_PARAMETER_NAME);
	}
}
