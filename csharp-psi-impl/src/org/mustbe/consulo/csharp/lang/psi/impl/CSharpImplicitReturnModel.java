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
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReturnStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpYieldStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.util.ObjectUtil;

/**
 * @author VISTALL
 * @since 27.11.14
 */
public enum CSharpImplicitReturnModel
{
	Async(DotNetTypes2.System.Threading.Tasks.Task$1, DotNetTypes2.System.Threading.Tasks.Task, DotNetTypes.System.Void)
			{
				@RequiredReadAction
				@Override
				public boolean canHandle(CSharpSimpleLikeMethodAsElement methodAsElement, CSharpReturnStatementImpl returnStatement)
				{
					return methodAsElement.hasModifier(CSharpModifier.ASYNC);
				}
			},
	YieldEnumerator(DotNetTypes2.System.Collections.Generic.IEnumerator$1, DotNetTypes2.System.Collections.IEnumerator, DotNetTypes.System.Object)
			{
				@RequiredReadAction
				@Override
				public boolean canHandle(CSharpSimpleLikeMethodAsElement methodAsElement, CSharpReturnStatementImpl returnStatement)
				{
					return returnStatement.getParent() instanceof CSharpYieldStatementImpl && extractTypeRefImpl(methodAsElement.getReturnTypeRef(),
							returnStatement) != null;
				}
			},
	YieldEnumerable(DotNetTypes2.System.Collections.Generic.IEnumerable$1, DotNetTypes2.System.Collections.IEnumerable, DotNetTypes.System.Object)
			{
				@RequiredReadAction
				@Override
				public boolean canHandle(CSharpSimpleLikeMethodAsElement methodAsElement, CSharpReturnStatementImpl returnStatement)
				{
					return returnStatement.getParent() instanceof CSharpYieldStatementImpl && extractTypeRefImpl(methodAsElement.getReturnTypeRef(),
							returnStatement) != null;
				}
			},
	None(null, null, null)
			{
				@RequiredReadAction
				@Override
				public boolean canHandle(CSharpSimpleLikeMethodAsElement methodAsElement, CSharpReturnStatementImpl returnStatement)
				{
					return true;
				}

				@RequiredReadAction
				@NotNull
				@Override
				public DotNetTypeRef extractTypeRef(@NotNull DotNetTypeRef expectedTypeRef, @NotNull PsiElement scope)
				{
					return expectedTypeRef;
				}
			};

	private final String myGenericVmQName;
	private final String myVmQName;
	private final String myNoGenericTypeVmQName;

	CSharpImplicitReturnModel(String genericVmQName, String vmQName, String noGenericTypeVmQName)
	{
		myGenericVmQName = genericVmQName;
		myVmQName = vmQName;
		myNoGenericTypeVmQName = noGenericTypeVmQName;
	}

	@RequiredReadAction
	public abstract boolean canHandle(CSharpSimpleLikeMethodAsElement methodAsElement, CSharpReturnStatementImpl returnStatement);

	@Nullable
	public String getGenericVmQName()
	{
		return myGenericVmQName;
	}

	@Nullable
	public String getNoGenericTypeVmQName()
	{
		return myNoGenericTypeVmQName;
	}

	@NotNull
	@RequiredReadAction
	public DotNetTypeRef extractTypeRef(@NotNull DotNetTypeRef expectedTypeRef, @NotNull PsiElement scope)
	{
		return ObjectUtil.notNull(extractTypeRefImpl(expectedTypeRef, scope), DotNetTypeRef.ERROR_TYPE);
	}

	@Nullable
	@RequiredReadAction
	public DotNetTypeRef extractTypeRefImpl(@NotNull DotNetTypeRef expectedTypeRef, @NotNull PsiElement scope)
	{
		Pair<DotNetTypeDeclaration, DotNetGenericExtractor> typeInSuper = CSharpTypeUtil.findTypeInSuper(expectedTypeRef, myGenericVmQName);
		if(typeInSuper != null)
		{
			DotNetGenericParameter genericParameter = ArrayUtil2.safeGet(typeInSuper.getFirst().getGenericParameters(), 0);
			if(genericParameter == null)
			{
				return null;
			}

			DotNetTypeRef extract = typeInSuper.getSecond().extract(genericParameter);
			if(extract == null)
			{
				return null;
			}
			return extract;
		}

		typeInSuper = CSharpTypeUtil.findTypeInSuper(expectedTypeRef, myVmQName);
		if(typeInSuper != null)
		{
			return new CSharpTypeRefByQName(scope, myNoGenericTypeVmQName);
		}
		return null;
	}

	@NotNull
	public static CSharpImplicitReturnModel getImplicitReturnModel(CSharpReturnStatementImpl element, CSharpSimpleLikeMethodAsElement pseudoMethod)
	{
		for(CSharpImplicitReturnModel implicitReturnModel : CSharpImplicitReturnModel.values())
		{
			if(implicitReturnModel.canHandle(pseudoMethod, element))
			{
				return implicitReturnModel;
			}
		}
		throw new IllegalArgumentException("CSharpImplicitReturnModel is broken");
	}
}
