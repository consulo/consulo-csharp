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

package consulo.csharp.ide.refactoring.changeSignature;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.util.collection.ContainerUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.editor.refactoring.changeSignature.ParameterInfo;

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
		myTypeText = CSharpTypeRefPresentationUtil.buildText(parameter.toTypeRef(false), CSharpTypeRefPresentationUtil.TYPE_KEYWORD | CSharpTypeRefPresentationUtil.NO_REF);
		myTypeRef = parameter.toTypeRef(false);
		myNewIndex = newIndex;
		myOldIndex = parameter.getIndex();
		myModifier = findModifier(parameter);
	}

	public CSharpParameterInfo(String name, @Nullable DotNetParameter parameter, @Nonnull DotNetTypeRef parameterTypeRef, int newIndex)
	{
		myParameter = parameter;
		myName = name;
		myTypeText = "";
		myTypeRef = parameterTypeRef;
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
