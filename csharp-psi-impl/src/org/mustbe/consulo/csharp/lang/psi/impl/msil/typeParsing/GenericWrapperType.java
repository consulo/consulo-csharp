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

package org.mustbe.consulo.csharp.lang.psi.impl.msil.typeParsing;

import java.util.List;

import com.intellij.openapi.util.text.StringUtil;

/**
 * @author VISTALL
 * @since 11.07.14
 */
public class GenericWrapperType implements SomeType
{
	private SomeType myTarget;
	private List<SomeType> myArguments;

	public GenericWrapperType(SomeType target, List<SomeType> arguments)
	{
		myTarget = target;
		myArguments = arguments;
	}

	public SomeType getTarget()
	{
		return myTarget;
	}

	public List<SomeType> getArguments()
	{
		return myArguments;
	}

	@Override
	public String toString()
	{
		return myTarget + "<" + StringUtil.join(myArguments, ",") + ">";
	}

	@Override
	public void accept(SomeTypeVisitor visitor)
	{
		visitor.visitGenericWrapperType(this);
	}
}
