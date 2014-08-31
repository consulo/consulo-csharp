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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 31.08.14
 */
public class CSharpConstantTypeRef extends DotNetTypeRef.Delegate implements CSharpChameleonTypeRef
{
	public CSharpConstantTypeRef(DotNetTypeRef defaultTypeRef)
	{
		super(defaultTypeRef);
	}

	@NotNull
	@Override
	public DotNetTypeRef doMirror(@NotNull DotNetTypeRef another)
	{
		int topRank = CSharpTypeUtil.getNumberRank(getDelegate());
		int targetRank = CSharpTypeUtil.getNumberRank(another);
		if(targetRank != -1 && targetRank < topRank)
		{
			return another;
		}
		return this;
	}
}
