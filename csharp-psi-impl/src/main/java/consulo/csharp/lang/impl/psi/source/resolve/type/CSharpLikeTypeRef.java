/*
 * Copyright 2013-2023 consulo.io
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

package consulo.csharp.lang.impl.psi.source.resolve.type;

import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;

import java.util.Objects;

/**
 * @author VISTALL
 * @since 2023-12-31
 */
public interface CSharpLikeTypeRef extends DotNetTypeRef
{
	static int hashCode(CSharpLikeTypeRef typeRef)
	{
		return Objects.hash(typeRef.getVmQName(), typeRef.getExtractor());
	}

	static boolean equals(CSharpLikeTypeRef o1, Object o2)
	{
		if(o2 instanceof CSharpLikeTypeRef typeRef2)
		{
			return Objects.equals(o1.getVmQName(), typeRef2.getVmQName()) && Objects.equals(o1.getExtractor(), typeRef2.getExtractor());
		}
		return false;
	}

	DotNetGenericExtractor getExtractor();
}
