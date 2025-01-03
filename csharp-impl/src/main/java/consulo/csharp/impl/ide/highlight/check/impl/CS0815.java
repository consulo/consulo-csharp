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

package consulo.csharp.impl.ide.highlight.check.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CS0815 extends CompilerCheck<CSharpLocalVariable>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpLocalVariable localVariable)
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

			if(DotNetTypeRefUtil.isVmQNameEqual(initializerType, DotNetTypes.System.Void))
			{
				return newBuilder(localVariable.getInitializer(), "void");
			}

			DotNetTypeResolveResult typeResolveResult = initializerType.resolve();
			if(typeResolveResult instanceof CSharpLambdaResolveResult && ((CSharpLambdaResolveResult) typeResolveResult).getTarget() == null)
			{
				return newBuilder(localVariable.getInitializer(), "anonymous method");
			}
		}
		return null;
	}
}
