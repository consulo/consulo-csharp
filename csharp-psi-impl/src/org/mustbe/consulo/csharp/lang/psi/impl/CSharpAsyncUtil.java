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

package org.mustbe.consulo.csharp.lang.psi.impl;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetTypeRefByQName;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 27.11.14
 */
public class CSharpAsyncUtil
{
	@NotNull
	public static DotNetTypeRef extractAsyncTypeRef(@NotNull DotNetTypeRef typeRef, @NotNull PsiElement element)
	{
		Pair<DotNetTypeDeclaration, DotNetGenericExtractor> typeInSuper = CSharpTypeUtil.findTypeInSuper(typeRef,
				DotNetTypes2.System.Threading.Tasks.Task$1, element);
		if(typeInSuper != null)
		{
			DotNetTypeRef extract = typeInSuper.getSecond().extract(typeInSuper.getFirst().getGenericParameters()[0]);
			assert extract != null;
			return extract;
		}

		typeInSuper = CSharpTypeUtil.findTypeInSuper(typeRef, DotNetTypes2.System.Threading.Tasks.Task, element);
		if(typeInSuper != null)
		{
			return new DotNetTypeRefByQName(DotNetTypes.System.Void, CSharpTransform.INSTANCE, false);
		}
		return DotNetTypeRef.ERROR_TYPE;
	}
}
