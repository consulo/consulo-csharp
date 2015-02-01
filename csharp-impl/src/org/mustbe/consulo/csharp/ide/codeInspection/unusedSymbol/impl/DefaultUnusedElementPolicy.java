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

package org.mustbe.consulo.csharp.ide.codeInspection.unusedSymbol.impl;

import org.mustbe.consulo.csharp.ide.codeInspection.unusedSymbol.UnusedElementPolicy;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpDelegateExpressionImpl;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterListOwner;

/**
 * @author VISTALL
 * @since 01.09.14
 */
public class DefaultUnusedElementPolicy extends UnusedElementPolicy
{
	@Override
	public boolean canMarkAsUnused(DotNetParameter parameter)
	{
		DotNetParameterListOwner parameterOwner = parameter.getOwner();
		if(parameterOwner instanceof CSharpDelegateExpressionImpl)
		{
			return false;
		}

		if(parameterOwner instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) parameterOwner).isDelegate())
		{
			return false;
		}

		if(parameterOwner instanceof DotNetLikeMethodDeclaration)
		{
			if(((DotNetLikeMethodDeclaration) parameterOwner).hasModifier(CSharpModifier.ABSTRACT) || ((DotNetLikeMethodDeclaration) parameterOwner)
					.hasModifier(CSharpModifier.OVERRIDE))
			{
				return false;
			}
		}
		return super.canMarkAsUnused(parameter);
	}
}
