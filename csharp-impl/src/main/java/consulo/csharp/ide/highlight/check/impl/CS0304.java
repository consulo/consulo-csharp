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

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpGenericConstraintKeywordValue;
import consulo.csharp.lang.psi.CSharpGenericConstraintOwner;
import consulo.csharp.lang.impl.psi.CSharpGenericConstraintUtil;
import consulo.csharp.lang.psi.CSharpGenericConstraintValue;
import consulo.csharp.lang.psi.CSharpNewExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetType;
import consulo.language.ast.TokenSet;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.psi.PsiElement;

/**
 * @author VISTALL
 * @since 17.05.14
 */
public class CS0304 extends CompilerCheck<CSharpNewExpression>
{
	private static final TokenSet ourGenericConstraintSet = TokenSet.create(CSharpTokens.NEW_KEYWORD, CSharpTokens.STRUCT_KEYWORD);

	@RequiredReadAction
	@Override
	public CompilerCheckBuilder checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpNewExpression element)
	{
		PsiElement resolvedNewElement = element.toTypeRef(false).resolve().getElement();
		if(resolvedNewElement instanceof DotNetGenericParameter)
		{
			DotNetGenericParameterListOwner parent = PsiTreeUtil.getParentOfType(resolvedNewElement, DotNetGenericParameterListOwner.class);
			if(!(parent instanceof CSharpGenericConstraintOwner))
			{
				return null;
			}

			boolean findNewOrStruct = false;
			CSharpGenericConstraint genericConstraint = CSharpGenericConstraintUtil.findGenericConstraint((DotNetGenericParameter) resolvedNewElement);
			if(genericConstraint != null)
			{
				for(CSharpGenericConstraintValue constraintValue : genericConstraint.getGenericConstraintValues())
				{
					if(constraintValue instanceof CSharpGenericConstraintKeywordValue && ourGenericConstraintSet.contains((
							(CSharpGenericConstraintKeywordValue) constraintValue).getKeywordElementType()))
					{
						findNewOrStruct = true;
						break;
					}
				}
			}

			if(!findNewOrStruct)
			{
				DotNetType newType = element.getNewType();
				assert newType != null;
				return newBuilder(newType, formatElement(resolvedNewElement));
			}
		}
		return null;
	}
}
