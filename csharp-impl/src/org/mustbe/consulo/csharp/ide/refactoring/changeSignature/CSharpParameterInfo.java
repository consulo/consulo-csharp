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

package org.mustbe.consulo.csharp.ide.refactoring.changeSignature;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.refactoring.changeSignature.ParameterInfo;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class CSharpParameterInfo implements ParameterInfo
{
	public static final List<CSharpModifier> ourParameterModifiers = ContainerUtil.newArrayList(CSharpModifier.REF, CSharpModifier.OUT, CSharpModifier.PARAMS);

	@RequiredReadAction
	public static CSharpModifier findModifier(DotNetParameter parameter)
	{
		for(CSharpModifier mod : ourParameterModifiers)
		{
			if(parameter.hasModifier(mod))
			{
				return mod;
			}
		}
		return null;
	}

	private final DotNetParameter myParameter;
	private String myName;
	private String myTypeText;
	private String myDefaultValue;
	private final int myNewIndex;
	private final int myOldIndex;

	private DotNetTypeRef myTypeRef;
	private CSharpModifier myModifier;

	@RequiredReadAction
	public CSharpParameterInfo(DotNetParameter parameter, int newIndex)
	{
		myParameter = parameter;
		myName = parameter.getName();
		myTypeText = CSharpTypeRefPresentationUtil.buildText(parameter.toTypeRef(false), parameter, CSharpTypeRefPresentationUtil.TYPE_KEYWORD | CSharpTypeRefPresentationUtil.NO_REF);
		myTypeRef = parameter.toTypeRef(false);
		myNewIndex = newIndex;
		myOldIndex = parameter.getIndex();
		myModifier = findModifier(parameter);
	}

	public CSharpParameterInfo(String name, @Nullable DotNetParameter parameter, int newIndex)
	{
		myParameter = parameter;
		myName = name;
		myTypeText = "";
		myTypeRef = new CSharpTypeRefByQName(parameter, DotNetTypes.System.Object);
		myNewIndex = newIndex;
		myOldIndex = parameter == null ? -1 : parameter.getIndex();
	}

	@Override
	public String getName()
	{
		return myName;
	}

	@Override
	public int getOldIndex()
	{
		return myOldIndex;
	}

	@Nullable
	@Override
	public String getDefaultValue()
	{
		return myDefaultValue;
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

	public CSharpModifier getModifier()
	{
		return myModifier;
	}

	public void setModifier(CSharpModifier modifier)
	{
		myModifier = modifier;
	}

	public int getNewIndex()
	{
		return myNewIndex;
	}

	public void setTypeText(String typeText)
	{
		myTypeText = typeText;
	}

	@Nullable
	public DotNetParameter getParameter()
	{
		return myParameter;
	}

	public void setDefaultValue(String defaultValue)
	{
		myDefaultValue = defaultValue;
	}

	public void setTypeRef(@Nullable DotNetTypeRef typeRef)
	{
		myTypeRef = typeRef;
	}

	@Nullable
	public DotNetTypeRef getTypeRef()
	{
		return myTypeRef;
	}
}
