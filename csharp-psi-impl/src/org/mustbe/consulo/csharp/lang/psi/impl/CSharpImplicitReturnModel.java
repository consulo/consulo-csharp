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
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 27.11.14
 */
public enum CSharpImplicitReturnModel
{
	Async(DotNetTypes2.System.Threading.Tasks.Task$1, DotNetTypes2.System.Threading.Tasks.Task, DotNetTypes.System.Void),
	Yield(DotNetTypes2.System.Collections.Generic.IEnumerable$1, DotNetTypes2.System.Collections.IEnumerable, DotNetTypes.System.Object);

	private final String myGenericVmQName;
	private final String myVmQName;
	private final String myNoGenericTypeVmQName;

	CSharpImplicitReturnModel(String genericVmQName, String vmQName, String noGenericTypeVmQName)
	{
		myGenericVmQName = genericVmQName;
		myVmQName = vmQName;
		myNoGenericTypeVmQName = noGenericTypeVmQName;
	}

	@NotNull
	public DotNetTypeRef extractTypeRef(@NotNull DotNetTypeRef typeRef, @NotNull PsiElement element)
	{
		Pair<DotNetTypeDeclaration, DotNetGenericExtractor> typeInSuper = CSharpTypeUtil.findTypeInSuper(typeRef, myGenericVmQName, element);
		if(typeInSuper != null)
		{
			DotNetGenericParameter genericParameter = ArrayUtil2.safeGet(typeInSuper.getFirst().getGenericParameters(), 0);
			if(genericParameter == null)
			{
				return typeRef;
			}

			DotNetTypeRef extract = typeInSuper.getSecond().extract(genericParameter);
			if(extract == null)
			{
				return typeRef;
			}
			return extract;
		}

		typeInSuper = CSharpTypeUtil.findTypeInSuper(typeRef, myVmQName, element);
		if(typeInSuper != null)
		{
			return new DotNetTypeRefByQName(myNoGenericTypeVmQName, CSharpTransform.INSTANCE, false);
		}
		return typeRef;
	}
}
