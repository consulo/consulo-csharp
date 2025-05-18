/*
 * Copyright 2013-2019 consulo.io
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

package consulo.csharp.lang.impl.psi.source;

import consulo.annotation.access.RequiredReadAction;
import consulo.content.scope.SearchScope;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpGenericConstraintList;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.scope.LocalSearchScope;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2019-10-01
 */
public class CSharpLocalMethodDeclarationImpl extends CSharpMemberImpl implements CSharpMethodDeclaration {
    public CSharpLocalMethodDeclarationImpl(@Nonnull IElementType elementType) {
        super(elementType);
    }

    @Nonnull
    @Override
    public SearchScope getUseScope() {
        return new LocalSearchScope(getParent().getParent());
    }

    @RequiredReadAction
    @Override
    public boolean isDelegate() {
        return false;
    }

    @RequiredReadAction
    @Override
    public boolean isOperator() {
        return false;
    }

    @Override
    public boolean isExtension() {
        return false;
    }

    @RequiredReadAction
    @Override
    public boolean isLocal() {
        return true;
    }

    @RequiredReadAction
    @Nullable
    @Override
    public IElementType getOperatorElementType() {
        return null;
    }

    @Nullable
    @Override
    public CSharpGenericConstraintList getGenericConstraintList() {
        return null;
    }

    @Nonnull
    @Override
    public CSharpGenericConstraint[] getGenericConstraints() {
        return CSharpGenericConstraint.EMPTY_ARRAY;
    }

    @RequiredReadAction
    @Nonnull
    @Override
    public CSharpSimpleParameterInfo[] getParameterInfos() {
        return CSharpLikeMethodDeclarationImplUtil.getParametersInfos(this);
    }

    @Override
    public void accept(@Nonnull CSharpElementVisitor visitor) {
        visitor.visitMethodDeclaration(this);
    }

    @RequiredReadAction
    @Nullable
    @Override
    public DotNetType getReturnType() {
        return findChildByClass(DotNetType.class);
    }

    @RequiredReadAction
    @Nonnull
    @Override
    public DotNetTypeRef getReturnTypeRef() {
        DotNetType returnType = getReturnType();
        return returnType == null ? DotNetTypeRef.ERROR_TYPE : returnType.toTypeRef();
    }

    @Nonnull
    @Override
    public CSharpCodeBodyProxyImpl getCodeBlock() {
        return CSharpStubLikeMethodDeclarationImpl.getCodeBlockElement(this);
    }

    @Nullable
    @Override
    @RequiredReadAction
    public DotNetGenericParameterList getGenericParameterList() {
        return findChildByClass(DotNetGenericParameterList.class);
    }

    @Nonnull
    @Override
    public DotNetGenericParameter[] getGenericParameters() {
        DotNetGenericParameterList genericParameterList = getGenericParameterList();
        return genericParameterList == null ? DotNetGenericParameter.EMPTY_ARRAY : genericParameterList.getParameters();
    }

    @Override
    public int getGenericParametersCount() {
        DotNetGenericParameterList genericParameterList = getGenericParameterList();
        return genericParameterList == null ? 0 : genericParameterList.getGenericParametersCount();
    }

    @Nullable
    @Override
    public DotNetParameterList getParameterList() {
        return findChildByClass(DotNetParameterList.class);
    }

    @Nonnull
    @Override
    public DotNetParameter[] getParameters() {
        DotNetParameterList parameterList = getParameterList();
        return parameterList == null ? DotNetParameter.EMPTY_ARRAY : parameterList.getParameters();
    }

    @Nonnull
    @Override
    public DotNetTypeRef[] getParameterTypeRefs() {
        DotNetParameterList parameterList = getParameterList();
        return parameterList == null ? DotNetTypeRef.EMPTY_ARRAY : parameterList.getParameterTypeRefs();
    }

    @RequiredReadAction
    @Nullable
    @Override
    public String getPresentableParentQName() {
        return null;
    }

    @RequiredReadAction
    @Nullable
    @Override
    public String getPresentableQName() {
        return getName();
    }

    @Nullable
    @Override
    public DotNetType getTypeForImplement() {
        return null;
    }

    @Nonnull
    @Override
    public DotNetTypeRef getTypeRefForImplement() {
        return DotNetTypeRef.ERROR_TYPE;
    }

    @Override
    public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState state, PsiElement lastParent, @Nonnull PsiElement place) {
        return CSharpLikeMethodDeclarationImplUtil.processDeclarations(this, processor, state, lastParent, place);
    }
}
