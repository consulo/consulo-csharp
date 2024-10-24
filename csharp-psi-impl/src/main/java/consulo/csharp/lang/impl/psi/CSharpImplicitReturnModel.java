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

package consulo.csharp.lang.impl.psi;

import consulo.language.psi.PsiElement;
import consulo.util.lang.ObjectUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.lang.impl.psi.source.CSharpReturnStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpYieldStatementImpl;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.util.ArrayUtil2;
import consulo.util.lang.Pair;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

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
					return returnStatement.getParent() instanceof CSharpYieldStatementImpl && extractTypeRefImpl(methodAsElement.getReturnTypeRef()
					) != null;
				}
			},
	YieldEnumerable(DotNetTypes2.System.Collections.Generic.IEnumerable$1, DotNetTypes2.System.Collections.IEnumerable, DotNetTypes.System.Object)
			{
				@RequiredReadAction
				@Override
				public boolean canHandle(CSharpSimpleLikeMethodAsElement methodAsElement, CSharpReturnStatementImpl returnStatement)
				{
					return returnStatement.getParent() instanceof CSharpYieldStatementImpl && extractTypeRefImpl(methodAsElement.getReturnTypeRef()
					) != null;
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
				@Nonnull
				@Override
				public DotNetTypeRef extractTypeRef(@Nonnull DotNetTypeRef expectedTypeRef, @Nonnull PsiElement scope)
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

	@Nonnull
	@RequiredReadAction
	public DotNetTypeRef extractTypeRef(@Nonnull DotNetTypeRef expectedTypeRef, @Nonnull PsiElement scope)
	{
		return ObjectUtil.notNull(extractTypeRefImpl(expectedTypeRef), DotNetTypeRef.ERROR_TYPE);
	}

	@Nullable
	@RequiredReadAction
	public DotNetTypeRef extractTypeRefImpl(@Nonnull DotNetTypeRef expectedTypeRef)
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
			return new CSharpTypeRefByQName(expectedTypeRef.getProject(), expectedTypeRef.getResolveScope(), myNoGenericTypeVmQName);
		}
		return null;
	}

	@Nonnull
	@RequiredReadAction
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
