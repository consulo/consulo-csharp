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

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import com.intellij.refactoring.changeSignature.ParameterInfo;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class CSharpParameterInfo implements ParameterInfo
{
	private String myName;
	private String myTypeText;
	private final int myIndex;

	public CSharpParameterInfo(DotNetParameter parameter, int index)
	{
		myName = parameter.getName();
		myTypeText = parameter.toTypeRef(false).getQualifiedText();
		myIndex = index;
	}

	public CSharpParameterInfo(String name, int index)
	{
		myName = name;
		myTypeText = "";
		myIndex = index;
	}

	@Override
	public String getName()
	{
		return myName;
	}

	@Override
	public int getOldIndex()
	{
		return myIndex;
	}

	@Nullable
	@Override
	public String getDefaultValue()
	{
		return null;
	}

	@Override
	public void setName(String name)
	{
		myName = name;
	}

	@Override
	public String getTypeText()
	{
		return myTypeText;
	}

	@Override
	public boolean isUseAnySingleVariable()
	{
		return false;
	}

	@Override
	public void setUseAnySingleVariable(boolean b)
	{

	}
}
