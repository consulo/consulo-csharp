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

package consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterList;
import consulo.dotnet.util.ArrayUtil2;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CS1737 extends CompilerCheck<DotNetParameter>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull DotNetParameter dotNetParameter)
	{
		if(dotNetParameter.getInitializer() == null)
		{
			return null;
		}

		DotNetParameterList parent = (DotNetParameterList) dotNetParameter.getParent();

		DotNetParameter[] parameters = parent.getParameters();

		int i = ArrayUtil.indexOf(parameters, dotNetParameter);

		DotNetParameter nextParameter = ArrayUtil2.safeGet(parameters, i + 1);
		if(nextParameter != null && !nextParameter.hasModifier(CSharpModifier.PARAMS) && nextParameter.getInitializer() == null)
		{
			return newBuilder(dotNetParameter);
		}
		return null;
	}
}
