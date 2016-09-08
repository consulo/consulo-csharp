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
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpImplicitArrayInitializationExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpLocalVariableUtil;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 24.01.15
 */
public class CS0820 extends CompilerCheck<CSharpLocalVariable>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpLocalVariable element)
	{
		if(CSharpLocalVariableUtil.isForeachVariable(element))
		{
			return null;
		}
		return element.toTypeRef(false) == DotNetTypeRef.AUTO_TYPE && element.getInitializer() instanceof CSharpImplicitArrayInitializationExpressionImpl ? newBuilder(element) : null;
	}
}
