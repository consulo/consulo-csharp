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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpUsingStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 15.12.14
 */
public class CS1674 extends CompilerCheck<CSharpUsingStatementImpl>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpUsingStatementImpl element)
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

		if(!CSharpTypeUtil.isInheritable(new CSharpTypeRefByQName(element, DotNetTypes.System.IDisposable), usingTypeRef, element))
		{
			assert highlightElement != null;
			return newBuilder(highlightElement, formatTypeRef(usingTypeRef, element));
		}
		return null;
	}
}
