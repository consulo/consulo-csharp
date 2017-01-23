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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.util.ArrayFactory;
import consulo.dotnet.psi.DotNetElement;
import consulo.dotnet.psi.DotNetExpression;

/**
 * @author VISTALL
 * @since 15.09.14
 */
public interface CSharpCallArgument extends DotNetElement
{
	public static final CSharpCallArgument[] EMPTY_ARRAY = new CSharpCallArgument[0];

	public static ArrayFactory<CSharpCallArgument> ARRAY_FACTORY = new ArrayFactory<CSharpCallArgument>()
	{
		@NotNull
		@Override
		public CSharpCallArgument[] create(int count)
		{
			return count == 0 ? EMPTY_ARRAY : new CSharpCallArgument[count];
		}
	};

	@Nullable
	DotNetExpression getArgumentExpression();
}
