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

package org.mustbe.consulo.csharp.lang.psi.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpAssignmentExpressionImpl;
import org.mustbe.consulo.dotnet.psi.DotNetEventDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 31.05.14
 */
public class CSharpEventUtil
{
	@Nullable
	public static DotNetEventDeclaration resolveEvent(@NotNull PsiElement parent)
	{
		if(parent instanceof CSharpAssignmentExpressionImpl)
		{
			IElementType operatorElementType = ((CSharpAssignmentExpressionImpl) parent).getOperatorElement().getOperatorElementType();

			if(operatorElementType == CSharpTokens.PLUSEQ || operatorElementType == CSharpTokens.MINUSEQ)
			{
				DotNetExpression expression = ((CSharpAssignmentExpressionImpl) parent).getExpressions()[0];
				if(expression instanceof CSharpReferenceExpression)
				{
					PsiElement resolve = ((CSharpReferenceExpression) expression).resolve();
					if(resolve instanceof DotNetEventDeclaration)
					{
						return (DotNetEventDeclaration) resolve;
					}
				}
			}
		}
		return null;
	}
}
