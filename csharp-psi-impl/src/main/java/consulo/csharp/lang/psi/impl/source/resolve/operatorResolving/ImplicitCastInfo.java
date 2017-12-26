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

package consulo.csharp.lang.psi.impl.source.resolve.operatorResolving;

import org.jetbrains.annotations.NotNull;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Key;

/**
 * @author VISTALL
 * @since 13.12.14
 */
public class ImplicitCastInfo
{
	public static final Key<ImplicitCastInfo> IMPLICIT_CAST_INFO = Key.create("implicit.to_type.ref");

	private final DotNetTypeRef myFromTypeRef;
	private final DotNetTypeRef myToTypeRef;

	public ImplicitCastInfo(@NotNull DotNetTypeRef fromTypeRef, @NotNull DotNetTypeRef toTypeRef)
	{
		myFromTypeRef = fromTypeRef;
		myToTypeRef = toTypeRef;
	}

	@NotNull
	public DotNetTypeRef getFromTypeRef()
	{
		return myFromTypeRef;
	}

	@NotNull
	public DotNetTypeRef getToTypeRef()
	{
		return myToTypeRef;
	}
}
