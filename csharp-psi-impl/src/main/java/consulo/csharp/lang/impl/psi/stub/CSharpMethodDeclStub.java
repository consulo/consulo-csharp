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

package consulo.csharp.lang.impl.psi.stub;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.language.ast.IElementType;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubElement;
import consulo.util.collection.ArrayUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 18.12.13.
 */
public class CSharpMethodDeclStub extends MemberStub<CSharpMethodDeclaration> {
    public static final int DELEGATE_MASK = 1 << 0;
    public static final int EXTENSION_MASK = 1 << 1;
    public static final int DE_CONSTRUCTOR_MASK = 1 << 2;
    public static final int CHECKED_OPERATOR = 1 << 3;

    private final int myOperatorIndex;

    public CSharpMethodDeclStub(StubElement parent, @Nullable String qname, int otherModifierMask, int operatorIndex) {
        super(parent, CSharpStubElements.METHOD_DECLARATION, qname, otherModifierMask);
        myOperatorIndex = operatorIndex;
    }

    public CSharpMethodDeclStub(StubElement parent, IStubElementType elementType, String qname, int otherModifierMask, int operatorIndex) {
        super(parent, elementType, qname, otherModifierMask);
        myOperatorIndex = operatorIndex;
    }

    public int getOperatorIndex() {
        return myOperatorIndex;
    }

    @Nullable
    public IElementType getOperator() {
        return myOperatorIndex == -1 ? null : CSharpTokenSets.OVERLOADING_OPERATORS_AS_ARRAY[myOperatorIndex];
    }

    @RequiredReadAction
    public static int getOperatorIndex(@Nonnull CSharpMethodDeclaration methodDeclaration) {
        IElementType operatorElementType = methodDeclaration.getOperatorElementType();
        return operatorElementType == null ? -1 : ArrayUtil.indexOf(CSharpTokenSets.OVERLOADING_OPERATORS_AS_ARRAY, operatorElementType);
    }

    @RequiredReadAction
    public static int getOtherModifierMask(@Nonnull DotNetLikeMethodDeclaration methodDeclaration) {
        int i = 0;
        if (methodDeclaration instanceof CSharpMethodDeclaration method && method.isDelegate()) {
            i |= DELEGATE_MASK;
        }
        if (methodDeclaration instanceof CSharpMethodDeclaration method && method.isExtension()) {
            i |= EXTENSION_MASK;
        }
        if (methodDeclaration instanceof CSharpConstructorDeclaration constructor && constructor.isDeConstructor()) {
            i |= DE_CONSTRUCTOR_MASK;
        }

        if (methodDeclaration instanceof CSharpMethodDeclaration method && method.isCheckedOperator()) {
            i |= CHECKED_OPERATOR;
        }
        return i;
    }

    public boolean isNested() {
        return getParentStub() instanceof CSharpTypeDeclStub;
    }
}
