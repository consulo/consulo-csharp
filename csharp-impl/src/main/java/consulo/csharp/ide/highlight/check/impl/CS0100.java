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

package consulo.csharp.ide.highlight.check.impl;

import com.intellij.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpLambdaParameterList;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetElement;
import consulo.dotnet.psi.DotNetParameterList;
import consulo.dotnet.psi.DotNetVariable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class CS0100 extends CompilerCheck<DotNetElement>
{
	@RequiredReadAction
	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull DotNetElement element)
	{
		DotNetVariable[] parameters = getParameters(element);
		if(parameters == null)
		{
			return null;
		}

		Set<String> defList = new HashSet<>();
		for(DotNetVariable parameter : parameters)
		{
			String name = parameter.getName();
			if(defList.contains(name))
			{
				PsiElement nameIdentifier = parameter.getNameIdentifier();
				assert nameIdentifier != null;
				return newBuilder(nameIdentifier, name);
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
