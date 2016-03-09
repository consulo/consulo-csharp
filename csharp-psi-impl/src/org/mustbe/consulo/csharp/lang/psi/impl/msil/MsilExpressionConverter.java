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

package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.msil.lang.psi.MsilConstantValue;
import org.mustbe.consulo.msil.lang.psi.MsilTokens;
import com.intellij.psi.tree.IElementType;

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
