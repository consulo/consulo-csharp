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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.codeInsight.actions.ConvertNamedToSimpleArgumentFix;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpCallArgumentList;
import consulo.csharp.lang.psi.CSharpNamedCallArgument;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.util.ArrayUtil2;
import consulo.util.collection.ArrayUtil;

/**
 * @author VISTALL
 * @since 28.03.2015
 */
public class CS1738 extends CompilerCheck<CSharpNamedCallArgument>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpNamedCallArgument element)
	{
		if(argumentIsInWrongPosition(element))
		{
			return newBuilder(element.getArgumentNameReference()).withQuickFix(new ConvertNamedToSimpleArgumentFix(element));
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
