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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.impl.psi.source.CSharpIndexAccessExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpOutRefWrapExpressionImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetVariable;
import consulo.language.psi.PsiElement;

/**
 * @author VISTALL
 * @since 13-May-16
 *
 * @see CS0206
 */
public class CS1510 extends CompilerCheck<CSharpOutRefWrapExpressionImpl>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpOutRefWrapExpressionImpl element)
	{
		DotNetExpression innerExpression = element.getInnerExpression();
		if(innerExpression == null || innerExpression instanceof CSharpIndexAccessExpressionImpl)  // ignore index access see CS0206
		{
			return null;
		}

		if(!(innerExpression instanceof CSharpReferenceExpression))
		{
			return newBuilder(innerExpression);
		}

		PsiElement psiElement = ((CSharpReferenceExpression) innerExpression).resolve();
		// reference already highlighted by error
		if(psiElement == null)
		{
			return null;
		}
		if(!(psiElement instanceof DotNetVariable) || ((DotNetVariable) psiElement).isConstant() || ((DotNetVariable) psiElement).hasModifier(CSharpModifier.READONLY))
		{
			return newBuilder(innerExpression);
		}
		return null;
	}
}
