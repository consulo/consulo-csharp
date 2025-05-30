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
import consulo.csharp.lang.impl.psi.source.CSharpConditionalExpressionImpl;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpNullTypeRef;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 04-Nov-17
 */
public class CS0173 extends CompilerCheck<CSharpConditionalExpressionImpl>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpConditionalExpressionImpl element)
	{
		DotNetExpression trueExpression = element.getTrueExpression();
		DotNetExpression falseExpression = element.getFalseExpression();
		if(trueExpression == null || falseExpression == null)
		{
			return null;
		}

		DotNetTypeRef typeRef = element.toTypeRef(true);
		if(typeRef instanceof CSharpNullTypeRef)
		{
			return newBuilder(element, formatTypeRef(trueExpression.toTypeRef(true)), formatTypeRef(falseExpression.toTypeRef(true)));
		}
		return null;
	}
}
