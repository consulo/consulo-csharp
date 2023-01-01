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

package consulo.csharp.lang.psi;

import consulo.dotnet.psi.DotNetElement;
import consulo.util.collection.ArrayFactory;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 30.11.13.
 */
public interface CSharpGenericConstraintValue extends DotNetElement
{
	public static final CSharpGenericConstraintValue[] EMPTY_ARRAY = new CSharpGenericConstraintValue[0];

	public static ArrayFactory<CSharpGenericConstraintValue> ARRAY_FACTORY = new ArrayFactory<CSharpGenericConstraintValue>()
	{
		@Nonnull
		@Override
		public CSharpGenericConstraintValue[] create(int count)
		{
			return count == 0 ? EMPTY_ARRAY : new CSharpGenericConstraintValue[count];
		}
	};
}
