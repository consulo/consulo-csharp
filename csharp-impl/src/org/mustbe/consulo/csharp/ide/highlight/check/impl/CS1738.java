/*
 * Copyright 2013-2015 must-be.org
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.ConvertNamedToSimpleArgumentFix;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.CSharpNamedCallArgument;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 28.03.2015
 */
public class CS1738 extends CompilerCheck<CSharpNamedCallArgument>
{
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpNamedCallArgument element)
	{
		if(argumentIsInWrongPosition(element))
		{
			return newBuilder(element.getArgumentNameReference()).addQuickFix(new ConvertNamedToSimpleArgumentFix(element));
		}
		return null;
	}

	public static boolean argumentIsInWrongPosition(CSharpNamedCallArgument element)
	{
		CSharpCallArgumentList parent = (CSharpCallArgumentList) element.getParent();

		CSharpCallArgument[] arguments = parent.getArguments();

		int i = ArrayUtil.indexOf(arguments, element);
		assert i != -1;

		CSharpCallArgument callArgument = ArrayUtil2.safeGet(arguments, i + 1);
		return callArgument != null && !(callArgument instanceof CSharpNamedCallArgument);
	}
}
