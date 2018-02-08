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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CS0815 extends CompilerCheck<CSharpLocalVariable>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpLocalVariable localVariable)
	{
		DotNetTypeRef dotNetTypeRef = localVariable.toTypeRef(false);
		if(dotNetTypeRef == DotNetTypeRef.AUTO_TYPE)
		{
			DotNetExpression initializer = localVariable.getInitializer();
			if(initializer == null)
			{
				return null;
			}
			if(CS0023.isNullConstant(initializer))
			{
				return newBuilder(localVariable.getInitializer(), "null");
			}
			DotNetTypeRef initializerType = initializer.toTypeRef(false);
			DotNetTypeResolveResult typeResolveResult = initializerType.resolve();
			if(typeResolveResult instanceof CSharpLambdaResolveResult && ((CSharpLambdaResolveResult) typeResolveResult).getTarget() == null)
			{
				return newBuilder(localVariable.getInitializer(), "anonymous method");
			}
		}
		return null;
	}
}