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
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraint;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintKeywordValue;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintValue;
import org.mustbe.consulo.csharp.lang.psi.CSharpNewExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 17.05.14
 */
public class CS0304 extends CompilerCheck<CSharpNewExpression>
{
	private static final TokenSet ourGenericConstraintSet = TokenSet.create(CSharpTokens.NEW_KEYWORD, CSharpTokens.STRUCT_KEYWORD);

	@Override
	public CompilerCheckBuilder checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpNewExpression element)
	{
		PsiElement resolve = element.toTypeRef(false).resolve(element).getElement();
		if(resolve instanceof DotNetGenericParameter)
		{
			DotNetGenericParameterListOwner parent = PsiTreeUtil.getParentOfType(resolve, DotNetGenericParameterListOwner.class);
			if(!(parent instanceof CSharpGenericConstraintOwner))
			{
				return null;
			}

			boolean findNewOrStruct = false;
			CSharpGenericConstraint genericConstraint = CSharpGenericConstraintUtil.findGenericConstraint((DotNetGenericParameter) resolve);
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
				return newBuilder(newType, ((DotNetGenericParameter) resolve).getName());
			}
		}
		return null;
	}
}
