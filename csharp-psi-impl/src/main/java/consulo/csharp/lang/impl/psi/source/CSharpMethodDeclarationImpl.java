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

package consulo.csharp.lang.impl.psi.source;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpStubElementSets;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpOperatorNameHelper;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpMethodImplUtil;
import consulo.csharp.lang.impl.psi.stub.CSharpMethodDeclStub;
import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpGenericConstraintList;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.stub.IStubElementType;
import consulo.util.lang.BitUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpMethodDeclarationImpl extends CSharpStubLikeMethodDeclarationImpl<CSharpMethodDeclStub> implements CSharpMethodDeclaration {
    public CSharpMethodDeclarationImpl(@Nonnull ASTNode node) {
        super(node);
    }

    public CSharpMethodDeclarationImpl(@Nonnull CSharpMethodDeclStub stub, @Nonnull IStubElementType<? extends CSharpMethodDeclStub, ?> nodeType) {
        super(stub, nodeType);
    }

    @Override
    public void accept(@Nonnull CSharpElementVisitor visitor) {
        visitor.visitMethodDeclaration(this);
    }

    @RequiredReadAction
    @Override
    @Nullable
    public PsiElement getNameIdentifier() {
        if (isOperator()) {
            return findChildByFilter(CSharpTokenSets.OVERLOADING_OPERATORS);
        }
        return getStubOrPsiChild(CSharpStubElements.IDENTIFIER);
    }

    @RequiredReadAction
    @Override
    public String getName() {
        if (isOperator()) {
            IElementType operatorElementType = getOperatorElementType();
            if (operatorElementType == null) {
                return "<error-operator>";
            }

            String operatorName = CSharpOperatorNameHelper.getOperatorName(operatorElementType);
            if (isCheckedOperator()) {
                return "checked " + operatorName;
            }
            return operatorName;
        }
        return super.getName();
    }

    @RequiredReadAction
    @Override
    public boolean isDelegate() {
        CSharpMethodDeclStub stub = getGreenStub();
        if (stub != null) {
            return BitUtil.isSet(stub.getOtherModifierMask(), CSharpMethodDeclStub.DELEGATE_MASK);
        }
        return findChildByType(CSharpTokens.DELEGATE_KEYWORD) != null;
    }

    @RequiredReadAction
    @Override
    public boolean isCheckedOperator() {
        CSharpMethodDeclStub stub = getGreenStub();
        if (stub != null) {
            return BitUtil.isSet(stub.getOtherModifierMask(), CSharpMethodDeclStub.CHECKED_OPERATOR);
        }
        return findChildByType(CSharpTokens.CHECKED_KEYWORD) != null;
    }

    @RequiredReadAction
    @Override
    public boolean isOperator() {
        CSharpMethodDeclStub stub = getGreenStub();
        if (stub != null) {
            return stub.getOperator() != null;
        }
        return findChildByType(CSharpTokens.OPERATOR_KEYWORD) != null;
    }

    @Override
    public boolean isExtension() {
        CSharpMethodDeclStub stub = getGreenStub();
        if (stub != null) {
            return BitUtil.isSet(stub.getOtherModifierMask(), CSharpMethodDeclStub.EXTENSION_MASK);
        }
        return CSharpMethodImplUtil.isExtensionMethod(this);
    }

    @RequiredReadAction
    @Nullable
    @Override
    public IElementType getOperatorElementType() {
        CSharpMethodDeclStub stub = getGreenStub();
        if (stub != null) {
            return stub.getOperator();
        }
        PsiElement element = findChildByType(CSharpTokenSets.OVERLOADING_OPERATORS);
        return PsiUtilCore.getElementType(element);
    }

    @Override
    public boolean isEquivalentTo(PsiElement another) {
        return CSharpLikeMethodDeclarationImplUtil.isEquivalentTo(this, another);
    }

    @Nullable
    @Override
    public CSharpGenericConstraintList getGenericConstraintList() {
        return getStubOrPsiChild(CSharpStubElements.GENERIC_CONSTRAINT_LIST);
    }

    @Nonnull
    @Override
    public CSharpGenericConstraint[] getGenericConstraints() {
        CSharpGenericConstraintList genericConstraintList = getGenericConstraintList();
        return genericConstraintList == null ? CSharpGenericConstraint.EMPTY_ARRAY : genericConstraintList.getGenericConstraints();
    }

    @Nullable
    @Override
    public DotNetType getTypeForImplement() {
        return getStubOrPsiChildByIndex(CSharpStubElementSets.TYPE_SET, 1);
    }

    @Nonnull
    @Override
    public DotNetTypeRef getTypeRefForImplement() {
        DotNetType typeForImplement = getTypeForImplement();
        if (typeForImplement == null) {
            return DotNetTypeRef.ERROR_TYPE;
        }
        else {
            return typeForImplement.toTypeRef();
        }
    }
}
