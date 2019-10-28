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

package consulo.csharp.lang.psi.impl.source;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.stubs.IStubElementType;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.csharp.lang.psi.CSharpStubElements;
import consulo.csharp.lang.psi.impl.stub.CSharpMethodDeclStub;
import consulo.dotnet.psi.*;
import consulo.dotnet.resolve.DotNetTypeRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public abstract class CSharpStubLikeMethodDeclarationImpl<T extends CSharpMethodDeclStub> extends CSharpStubMemberImpl<T> implements DotNetLikeMethodDeclaration
{
	public CSharpStubLikeMethodDeclarationImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpStubLikeMethodDeclarationImpl(@Nonnull T stub, @Nonnull IStubElementType<? extends T, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Nullable
	@Override
	public DotNetParameterList getParameterList()
	{
		return getStubOrPsiChild(CSharpStubElements.PARAMETER_LIST);
	}

	@Nonnull
	@Override
	public DotNetParameter[] getParameters()
	{
		DotNetParameterList parameterList = getParameterList();
		return parameterList == null ? DotNetParameter.EMPTY_ARRAY : parameterList.getParameters();
	}

	@Nonnull
	@Override
	public DotNetTypeRef[] getParameterTypeRefs()
	{
		DotNetParameterList parameterList = getParameterList();
		return parameterList == null ? DotNetTypeRef.EMPTY_ARRAY : parameterList.getParameterTypeRefs();
	}

	@Nonnull
	@Override
	public CSharpCodeBodyProxyImpl getCodeBlock()
	{
		return getCodeBlockElement(this);
	}

	@RequiredReadAction
	@Nonnull
	public static CSharpCodeBodyProxyImpl getCodeBlockElement(PsiElement element)
	{
		return new CSharpCodeBodyProxyImpl((CSharpSimpleLikeMethodAsElement) element);
	}

	@Nullable
	@Override
	public DotNetType getReturnType()
	{
		return getStubOrPsiChildByIndex(CSharpStubElements.TYPE_SET, 0);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		DotNetType type = getReturnType();
		return type == null ? DotNetTypeRef.ERROR_TYPE : type.toTypeRef();
	}

	@RequiredReadAction
	@Nonnull
	public CSharpSimpleParameterInfo[] getParameterInfos()
	{
		return CSharpLikeMethodDeclarationImplUtil.getParametersInfos(this);
	}

	@Nullable
	@Override
	public DotNetGenericParameterList getGenericParameterList()
	{
		return getStubOrPsiChild(CSharpStubElements.GENERIC_PARAMETER_LIST);
	}

	@Nonnull
	@Override
	public DotNetGenericParameter[] getGenericParameters()
	{
		DotNetGenericParameterList genericParameterList = getGenericParameterList();
		return genericParameterList == null ? DotNetGenericParameter.EMPTY_ARRAY : genericParameterList.getParameters();
	}

	@Override
	public int getGenericParametersCount()
	{
		DotNetGenericParameterList genericParameterList = getGenericParameterList();
		return genericParameterList == null ? 0 : genericParameterList.getGenericParametersCount();
	}

	@Override
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor,
									   @Nonnull ResolveState state,
									   PsiElement lastParent,
									   @Nonnull PsiElement place)
	{
		return CSharpLikeMethodDeclarationImplUtil.processDeclarations(this, processor, state, lastParent, place);
	}
}
