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
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpUsingStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 15.12.14
 */
public class CS1674 extends CompilerCheck<CSharpUsingStatementImpl>
{
	private static DotNetTypeRef ourIDisposableTypeRef = new CSharpTypeRefByQName(DotNetTypes.System.IDisposable);

	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpUsingStatementImpl element)
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

		if(!CSharpTypeUtil.isInheritable(ourIDisposableTypeRef, usingTypeRef, element))
		{
			assert highlightElement != null;
			return newBuilder(highlightElement, CSharpTypeRefPresentationUtil.buildText(usingTypeRef, element, CS0029.TYPE_FLAGS));
		}
		return null;
	}
}
