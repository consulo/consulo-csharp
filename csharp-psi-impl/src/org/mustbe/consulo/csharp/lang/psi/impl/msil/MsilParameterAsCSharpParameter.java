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

import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilParameterAsCSharpParameter extends MsilVariableAsCSharpVariable implements DotNetParameter
{
	private final int myIndex;

	public MsilParameterAsCSharpParameter(DotNetVariable variable, int index)
	{
		super(variable);
		myIndex = index;
	}

	@Override
	public String getName()
	{
		String name = super.getName();
		return name == null ? "p" + myIndex : name;
	}
}
