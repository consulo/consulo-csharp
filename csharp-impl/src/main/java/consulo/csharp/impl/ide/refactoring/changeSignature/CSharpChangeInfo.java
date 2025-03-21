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

package consulo.csharp.impl.ide.refactoring.changeSignature;

import java.util.List;

import jakarta.annotation.Nullable;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.CSharpAccessModifier;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.language.Language;
import consulo.language.editor.refactoring.changeSignature.ChangeInfo;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 12.06.14
 */
public class CSharpChangeInfo implements ChangeInfo
{
	private final DotNetLikeMethodDeclaration myMethodDeclaration;
	private final List<CSharpParameterInfo> myParameters;
	private final boolean myParametersChanged;
	private final String myNewName;
	private final String myNewReturnType;
	private final CSharpAccessModifier myNewVisibility;

	public CSharpChangeInfo(
			DotNetLikeMethodDeclaration methodDeclaration,
			List<CSharpParameterInfo> parameters,
			boolean parametersChanged,
			String newName,
			String newReturnType,
			CSharpAccessModifier newVisibility)
	{
		myMethodDeclaration = methodDeclaration;
		myParameters = parameters;
		myParametersChanged = parametersChanged;
		myNewName = newName;
		myNewReturnType = newReturnType;
		myNewVisibility = newVisibility;
	}

	@Nonnull
	@Override
	public CSharpParameterInfo[] getNewParameters()
	{
		return myParameters.toArray(new CSharpParameterInfo[myParameters.size()]);
	}

	@Override
	public boolean isParameterSetOrOrderChanged()
	{
		return myParametersChanged;
	}

	@Override
	public boolean isParameterTypesChanged()
	{
		return myParametersChanged;
	}

	@Override
	public boolean isParameterNamesChanged()
	{
		return myParametersChanged;
	}

	@Override
	public boolean isGenerateDelegate()
	{
		return false;
	}

	@Override
	public boolean isNameChanged()
	{
		return myNewName != null;
	}

	@Override
	public DotNetLikeMethodDeclaration getMethod()
	{
		return myMethodDeclaration;
	}

	@Override
	public boolean isReturnTypeChanged()
	{
		return myNewReturnType != null;
	}

	public String getNewReturnType()
	{
		return myNewReturnType;
	}

	@Override
	public String getNewName()
	{
		return myNewName;
	}

	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}

	@Nullable
	public CSharpAccessModifier getNewVisibility()
	{
		return myNewVisibility;
	}

	public boolean isParametersChanged()
	{
		return myParametersChanged;
	}
}
