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
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 31.08.14
 */
public class CSharpConstantTypeRef extends DotNetTypeRef.Delegate implements CSharpFastImplicitTypeRef
{
	// this value always cached on JVM ?
	private static final Integer ZERO = 0;

	private static final DotNetTypeRef ourEnumTypeRef = new CSharpTypeRefByQName(DotNetTypes.System.Enum);

	@Nullable
	private final Object myValue;

	public CSharpConstantTypeRef(@NotNull DotNetTypeRef defaultTypeRef, @Nullable Object value)
	{
		super(defaultTypeRef);
		myValue = value;
	}

	@Nullable
	@Override
	public DotNetTypeRef doMirror(@NotNull DotNetTypeRef another, PsiElement scope)
	{
		int topRank = CSharpTypeUtil.getNumberRank(getDelegate(), scope);
		int targetRank = CSharpTypeUtil.getNumberRank(another, scope);
		if(targetRank != -1 && targetRank < topRank)
		{
			return another;
		}

		if(myValue == ZERO && CSharpTypeUtil.isInheritable(ourEnumTypeRef, another, scope))
		{
			return another;
		}
		return null;
	}
}
