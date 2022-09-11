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

package consulo.csharp.impl.ide.actions.generate.memberChoose;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.CSharpElementPresentationUtil;
import consulo.csharp.impl.ide.completion.expected.ExpectedUsingInfo;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.psi.CSharpAccessModifier;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetConstructorDeclaration;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.language.editor.generation.ClassMember;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ArrayFactory;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 25.06.14
 */
public class ConstructorChooseMember extends CSharpMemberChooseObject<DotNetConstructorDeclaration> implements ClassMember
{
	public static final ConstructorChooseMember[] EMPTY_ARRAY = new ConstructorChooseMember[0];

	public static ArrayFactory<ConstructorChooseMember> ARRAY_FACTORY = count -> count == 0 ? EMPTY_ARRAY : new ConstructorChooseMember[count];

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
			CSharpTypeRefPresentationUtil.appendTypeRef(builder, parameter.toTypeRef(true), CSharpTypeRefPresentationUtil.TYPE_KEYWORD);
			builder.append(" ");
			builder.append(getParameterName(parameter));
		}

		builder.append(")");

		if(!targets.isEmpty())
		{
			boolean needGenerateBase = true;
			PsiElement parent = myDeclaration.getParent();
			if(parent instanceof DotNetTypeDeclaration)
			{
				String vmQName = ((DotNetTypeDeclaration) parent).getVmQName();
				needGenerateBase = !DotNetTypes.System.Object.equals(vmQName);
			}

			if(needGenerateBase)
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
	@Nonnull
	@Override
	public String getPresentationText()
	{
		return CSharpElementPresentationUtil.formatMethod(myDeclaration, CSharpElementPresentationUtil.METHOD_PARAMETER_NAME);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public ExpectedUsingInfo getExpectedUsingInfo()
	{
		return ExpectedUsingInfo.calculateFrom(myDeclaration);
	}
}
