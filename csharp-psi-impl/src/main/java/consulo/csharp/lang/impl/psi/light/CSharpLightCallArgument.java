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

package consulo.csharp.lang.impl.psi.light;

import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.dotnet.psi.DotNetExpression;
import consulo.language.impl.psi.LightElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public class CSharpLightCallArgument extends LightElement implements CSharpCallArgument
{
	private final DotNetExpression myExpression;

	public CSharpLightCallArgument(@Nonnull DotNetExpression expression)
	{
		super(expression.getManager(), expression.getLanguage());
		myExpression = expression;
	}

	@Nullable
	@Override
	public DotNetExpression getArgumentExpression()
	{
		return myExpression;
	}

	@Override
	public String toString()
	{
		return "CSharpLightCallArgument: " + myExpression.getText();
	}
}
