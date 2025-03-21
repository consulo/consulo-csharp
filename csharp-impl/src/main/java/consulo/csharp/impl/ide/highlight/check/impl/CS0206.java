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

import jakarta.annotation.Nullable;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.source.CSharpIndexAccessExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpOutRefWrapExpressionImpl;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpArrayTypeRef;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 29-Jul-16
 */
public class CS0206 extends CompilerCheck<CSharpOutRefWrapExpressionImpl>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpOutRefWrapExpressionImpl element)
	{
		DotNetExpression innerExpression = element.getInnerExpression();
		if(innerExpression instanceof CSharpIndexAccessExpressionImpl)
		{
			DotNetExpression qualifier = ((CSharpIndexAccessExpressionImpl) innerExpression).getQualifier();

			DotNetTypeRef typeRef = qualifier.toTypeRef(true);
			if(!(typeRef instanceof CSharpArrayTypeRef))
			{
				return newBuilder(innerExpression);
			}
		}
		return null;
	}
}
