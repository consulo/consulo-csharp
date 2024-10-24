/*
 * Copyright 2013-2021 consulo.io
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

import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpEventDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.impl.psi.source.CSharpAssignmentExpressionImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 07/07/2021
 */
public class CS0070 extends CompilerCheck<CSharpReferenceExpression>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpReferenceExpression element)
	{
		PsiElement resolvedTarget = element.resolve();
		if(resolvedTarget instanceof CSharpEventDeclaration)
		{
			CSharpTypeDeclaration type = PsiTreeUtil.getParentOfType(resolvedTarget, CSharpTypeDeclaration.class);
			assert type != null;

			CSharpTypeDeclaration callerType = PsiTreeUtil.getParentOfType(element, CSharpTypeDeclaration.class);
			// owner can call without restriction
			if(callerType == null || callerType.isEquivalentTo(type))
			{
				return null;
			}

			PsiElement parent = element.getParent();

			if(parent instanceof CSharpAssignmentExpressionImpl assign)
			{
				// allow only += and -=
				if(assign.getOperatorElement().getOperatorElementType() == CSharpTokens.PLUSEQ || assign.getOperatorElement().getOperatorElementType() == CSharpTokens.MINUSEQ)
				{
					return null;
				}
			}

			return newBuilder(element.getReferenceElement(), formatElement(resolvedTarget), formatElement(type));
		}
		return super.checkImpl(languageVersion, highlightContext, element);
	}
}
