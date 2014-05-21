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
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheckForWithNameArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CS1100 extends CompilerCheckForWithNameArgument<CSharpMethodDeclaration>
{
	@Override
	public boolean accept(@NotNull CSharpMethodDeclaration methodDeclaration)
	{
		DotNetParameter[] parameters = methodDeclaration.getParameters();
		for(int i = 0; i < parameters.length; i++)
		{
			DotNetParameter parameter = parameters[i];
			if(i == 0)
			{
				continue;
			}

			if(parameter.hasModifier(CSharpModifier.THIS))
			{
				return true;
			}
		}
		return false;
	}
}
