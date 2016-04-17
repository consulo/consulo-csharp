/*
 * Copyright 2013-2016 must-be.org
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
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 06.03.2016
 */
public class CS0118 extends CompilerCheck<CSharpReferenceExpression>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpReferenceExpression element)
	{
		CSharpReferenceExpression.ResolveToKind kind = element.kind();
		if(kind == CSharpReferenceExpression.ResolveToKind.TYPE_LIKE || kind == CSharpReferenceExpression.ResolveToKind.THIS)
		{
			return null;
		}
		// qualifier of another expression
		if(element.getParent() instanceof CSharpReferenceExpression)
		{
			return null;
		}
		PsiElement psiElement = element.resolve();
		return psiElement instanceof CSharpTypeDeclaration ? newBuilder(element, formatElement(psiElement)) : null;
	}
}
