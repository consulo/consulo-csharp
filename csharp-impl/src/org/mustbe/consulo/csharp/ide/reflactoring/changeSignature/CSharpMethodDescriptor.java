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

package org.mustbe.consulo.csharp.ide.reflactoring.changeSignature;

import java.util.ArrayList;
import java.util.List;

import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import com.intellij.refactoring.changeSignature.MethodDescriptor;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class CSharpMethodDescriptor implements MethodDescriptor<CSharpParameterInfo, CSharpModifier>
{
	public static CSharpModifier[] ourAccessModifiers = new CSharpModifier[]{
			CSharpModifier.PUBLIC,
			CSharpModifier.PRIVATE,
			CSharpModifier.PROTECTED,
			CSharpModifier.INTERNAL
	};

	private List<CSharpParameterInfo> myParameters;
	private DotNetLikeMethodDeclaration myMethod;

	public CSharpMethodDescriptor(DotNetLikeMethodDeclaration method)
	{
		myMethod = method;

		DotNetParameter[] parameters = method.getParameters();

		myParameters = new ArrayList<CSharpParameterInfo>(parameters.length);
		for(int i = 0; i < parameters.length; i++)
		{
			DotNetParameter parameter = parameters[i];
			myParameters.add(new CSharpParameterInfo(parameter, i));
		}
	}

	@Override
	public String getName()
	{
		if(myMethod instanceof CSharpMethodDeclaration)
		{
			return myMethod.getName();
		}
		return null;
	}

	@Override
	public List<CSharpParameterInfo> getParameters()
	{
		return myParameters;
	}

	@Override
	public int getParametersCount()
	{
		return myParameters.size();
	}

	@Override
	public CSharpModifier getVisibility()
	{
		for(CSharpModifier accessModifier : ourAccessModifiers)
		{
			if(myMethod.hasModifier(accessModifier))
			{
				return accessModifier;
			}
		}
		return null;
	}

	@Override
	public DotNetLikeMethodDeclaration getMethod()
	{
		return myMethod;
	}

	@Override
	public boolean canChangeVisibility()
	{
		return true;
	}

	@Override
	public boolean canChangeParameters()
	{
		return true;
	}

	@Override
	public boolean canChangeName()
	{
		return myMethod instanceof CSharpMethodDeclaration;
	}

	@Override
	public ReadWriteOption canChangeReturnType()
	{
		return ReadWriteOption.ReadWrite;
	}
}
