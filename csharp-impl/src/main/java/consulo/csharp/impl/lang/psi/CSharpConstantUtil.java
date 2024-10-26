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

package consulo.csharp.impl.lang.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.source.*;
import consulo.csharp.lang.psi.CSharpEnumConstantDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetVariable;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.Contract;

/**
 * @author VISTALL
 * @since 02-Nov-17
 */
public class CSharpConstantUtil
{
	@RequiredReadAction
	@Contract("null -> false")
	public static boolean isConstant(@Nullable PsiElement expression)
	{
		if(expression instanceof CSharpConstantExpressionImpl)
		{
			return true;
		}

		if(expression instanceof CSharpPrefixExpressionImpl)
		{
			return ((CSharpPrefixExpressionImpl) expression).getOperatorElement().getOperatorElementType() == CSharpTokens.MINUS && isConstant(((CSharpPrefixExpressionImpl) expression).getExpression
					());
		}

		return false;
	}

	@RequiredReadAction
	@Contract("null -> false")
	public static boolean isCompileTimeConstant(@Nullable PsiElement element)
	{
		if(element == null)
		{
			return false;
		}

		if(isConstant(element))
		{
			return true;
		}

		if(element instanceof CSharpReferenceExpression)
		{
			PsiElement target = ((CSharpReferenceExpression) element).resolve();
			if(target instanceof DotNetVariable && ((DotNetVariable) target).isConstant())
			{
				return true;
			}
			if(target instanceof CSharpEnumConstantDeclaration)
			{
				return true;
			}
		}

		if(element instanceof CSharpBinaryExpressionImpl)
		{
			DotNetExpression leftExpression = ((CSharpBinaryExpressionImpl) element).getLeftExpression();
			DotNetExpression rightExpression = ((CSharpBinaryExpressionImpl) element).getRightExpression();
			if(leftExpression == null || rightExpression == null)
			{
				return false;
			}
			return isCompileTimeConstant(leftExpression) && isCompileTimeConstant(rightExpression);
		}

		if(element instanceof CSharpDefaultExpressionImpl)
		{
			return true;
		}

		if(element instanceof CSharpTypeCastExpressionImpl)
		{
			return isCompileTimeConstant(((CSharpTypeCastExpressionImpl) element).getInnerExpression());
		}

		if(element instanceof CSharpCheckedExpressionImpl)
		{
			return isCompileTimeConstant(((CSharpCheckedExpressionImpl) element).getInnerExpression());
		}

		if(element instanceof CSharpParenthesesExpressionImpl)
		{
			return isCompileTimeConstant(((CSharpParenthesesExpressionImpl) element).getInnerExpression());
		}
		return false;
	}
}
