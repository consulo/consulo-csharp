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

package consulo.csharp.lang.impl.psi.msil;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.dotnet.psi.DotNetExpression;
import consulo.language.ast.IElementType;
import consulo.msil.impl.lang.psi.MsilTokens;
import consulo.msil.lang.psi.MsilConstantValue;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 09.03.2016
 */
public class MsilExpressionConverter
{
	@RequiredReadAction
	public static DotNetExpression convert(@Nullable DotNetExpression expression)
	{
		if(expression instanceof MsilConstantValue)
		{
			IElementType valueType = ((MsilConstantValue) expression).getValueType();
			if(valueType == MsilTokens.NULLREF_KEYWORD)
			{
				return CSharpFileFactory.createExpression(expression.getProject(), "null");
			}
		}

		return expression;
	}
}
