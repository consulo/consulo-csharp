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

import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.impl.source.CSharpAssignmentExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpLocalVariableUtil;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetVariable;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 08-Jun-16
 */
public class CS1656 extends CompilerCheck<CSharpAssignmentExpressionImpl>
{
	private static final String ourForeachVariable = "foreach iteration variable";

	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpAssignmentExpressionImpl element)
	{
		DotNetExpression leftExpression = element.getLeftExpression();
		if(leftExpression instanceof CSharpReferenceExpression)
		{
			PsiElement resolvedElement = ((CSharpReferenceExpression) leftExpression).resolve();

			if(resolvedElement instanceof DotNetVariable && CSharpLocalVariableUtil.isForeachVariable((DotNetVariable) resolvedElement))
			{
				return newBuilder(leftExpression, formatElement(resolvedElement), ourForeachVariable);
			}
		}

		return super.checkImpl(languageVersion, highlightContext, element);
	}
}
