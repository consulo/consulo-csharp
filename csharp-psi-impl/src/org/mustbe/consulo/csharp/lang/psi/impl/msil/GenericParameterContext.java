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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author VISTALL
 * @since 15.03.2016
 */
public class GenericParameterContext
{
	@Nullable
	private GenericParameterContext myParent;

	private int myCount;

	public GenericParameterContext(@Nullable GenericParameterContext parent)
	{
		myParent = parent;
	}

	public void setGenericParameterCount(int count)
	{
		myCount = count;
	}

	@NotNull
	public GenericParameterContext gemmate()
	{
		return new GenericParameterContext(this);
	}

	public boolean isImplicitParameter(int i)
	{
		if(myParent != null)
		{
			int count = myParent.myCount;
			return i < count;
		}
		return false;
	}
}
