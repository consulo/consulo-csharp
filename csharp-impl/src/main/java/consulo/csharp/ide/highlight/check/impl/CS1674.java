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

import consulo.language.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.CSharpUsingStatementImpl;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 15.12.14
 */
public class CS1674 extends CompilerCheck<CSharpUsingStatementImpl>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpUsingStatementImpl element)
	{
		DotNetTypeRef usingTypeRef = DotNetTypeRef.ERROR_TYPE;
		PsiElement highlightElement = null;

		DotNetVariable variable = element.getVariable();
		if(variable != null)
		{
			usingTypeRef = variable.toTypeRef(true);
			highlightElement = variable;
		}
		else
		{
			DotNetExpression expression = element.getExpression();
			if(expression != null)
			{
				usingTypeRef = expression.toTypeRef(true);
				highlightElement = expression;
			}
		}

		if(usingTypeRef == DotNetTypeRef.ERROR_TYPE)
		{
			return null;
		}

		if(!CSharpTypeUtil.isInheritable(new CSharpTypeRefByQName(element, DotNetTypes.System.IDisposable), usingTypeRef))
		{
			assert highlightElement != null;
			return newBuilder(highlightElement, formatTypeRef(usingTypeRef));
		}
		return null;
	}
}
