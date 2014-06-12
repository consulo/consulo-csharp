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

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import com.intellij.lang.Language;
import com.intellij.refactoring.changeSignature.ChangeInfo;
import com.intellij.refactoring.changeSignature.ParameterInfo;

/**
 * @author VISTALL
 * @since 12.06.14
 */
public class CSharpChangeInfo implements ChangeInfo
{
	private final DotNetLikeMethodDeclaration myMethodDeclaration;
	private final String myNewName;

	public CSharpChangeInfo(DotNetLikeMethodDeclaration methodDeclaration, String newName)
	{
		myMethodDeclaration = methodDeclaration;
		myNewName = newName;
	}

	@NotNull
	@Override
	public ParameterInfo[] getNewParameters()
	{
		return new ParameterInfo[0];
	}

	@Override
	public boolean isParameterSetOrOrderChanged()
	{
		return false;
	}

	@Override
	public boolean isParameterTypesChanged()
	{
		return false;
	}

	@Override
	public boolean isParameterNamesChanged()
	{
		return false;
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
		return false;
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
}
