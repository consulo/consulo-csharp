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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import gnu.trove.THashSet;

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpLambdaParameterList;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetElement;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class CS0100 extends CompilerCheck<DotNetElement>
{
	@Nullable
	@Override
	public CompilerCheckResult checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull DotNetElement element)
	{
		DotNetVariable[] parameters = getParameters(element);
		if(parameters == null)
		{
			return null;
		}

		Set<String> defList = new THashSet<String>();
		for(DotNetVariable parameter : parameters)
		{
			String name = parameter.getName();
			if(defList.contains(name))
			{
				PsiElement nameIdentifier = parameter.getNameIdentifier();
				assert nameIdentifier != null;
				return result(nameIdentifier, name);
			}
			else
			{
				defList.add(name);
			}
		}
		return null;
	}

	private static DotNetVariable[] getParameters(DotNetElement element)
	{
		if(element instanceof CSharpLambdaParameterList)
		{
			return ((CSharpLambdaParameterList) element).getParameters();
		}
		else if(element instanceof DotNetParameterList)
		{
			return ((DotNetParameterList) element).getParameters();
		}
		return null;
	}
}
