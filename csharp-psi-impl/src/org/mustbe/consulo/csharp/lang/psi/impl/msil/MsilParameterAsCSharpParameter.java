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

package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetRefTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilParameterAsCSharpParameter extends MsilVariableAsCSharpVariable implements DotNetParameter
{
	private final DotNetLikeMethodDeclaration myMethodDeclaration;
	private final int myIndex;

	public MsilParameterAsCSharpParameter(DotNetVariable variable, DotNetLikeMethodDeclaration methodDeclaration, int index)
	{
		super(variable);
		myMethodDeclaration = methodDeclaration;
		myIndex = index;
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromInitializer)
	{
		DotNetTypeRef typeRef = super.toTypeRef(resolveFromInitializer);
		// check to ref not needed - it default wrapping to ref
		if(hasModifier(CSharpModifier.OUT))
		{
			if(!(typeRef instanceof DotNetRefTypeRef))
			{
				return typeRef;
			}

			return new CSharpRefTypeRef(CSharpRefTypeRef.Type.out, ((DotNetRefTypeRef) typeRef).getInnerTypeRef());
		}
		return typeRef;
	}

	@Override
	public String getName()
	{
		String name = super.getName();
		return name == null ? "p" + myIndex : name;
	}

	@NotNull
	@Override
	public DotNetLikeMethodDeclaration getMethod()
	{
		return myMethodDeclaration;
	}
}
