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

import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.util.collection.ArrayFactory;
import jakarta.annotation.Nonnull;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 11.12.14
 */
public interface CSharpUsingTypeStatement extends CSharpUsingListChild
{
	public static final CSharpUsingTypeStatement[] EMPTY_ARRAY = new CSharpUsingTypeStatement[0];

	public static ArrayFactory<CSharpUsingTypeStatement> ARRAY_FACTORY = count -> count == 0 ? EMPTY_ARRAY : new CSharpUsingTypeStatement[count];

	@Nonnull
	DotNetTypeRef getTypeRef();

	@Nullable
	DotNetType getType();
}
