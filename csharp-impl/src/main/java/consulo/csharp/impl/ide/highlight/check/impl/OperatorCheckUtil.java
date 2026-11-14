/*
 * Copyright 2013-2026 consulo.io
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
package consulo.csharp.impl.ide.highlight.check.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpNullableTypeUtil;
import consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import org.jspecify.annotations.Nullable;

/**
 * The user-defined operators of the CS0215 - CS0590 checks
 *
 * @author VISTALL
 * @since 2026-11-14
 */
class OperatorCheckUtil {
    static final TokenSet SHIFTS = TokenSet.create(CSharpTokens.LTLT, CSharpTokens.GTGT, CSharpTokens.GTGTGT);

    static final TokenSet INCREMENTS = TokenSet.create(CSharpTokens.PLUSPLUS, CSharpTokens.MINUSMINUS);

    // C# 14 user-defined compound assignments - instance operators
    static final TokenSet COMPOUND_ASSIGNMENTS = TokenSet.create(CSharpTokens.PLUSEQ, CSharpTokens.MINUSEQ, CSharpTokens.MULEQ, CSharpTokens.DIVEQ, CSharpTokens.PERCEQ,
        CSharpTokens.ANDEQ, CSharpTokens.OREQ, CSharpTokens.XOREQ, CSharpTokens.LTLTEQ, CSharpTokens.GTGTEQ, CSharpTokens.GTGTGTEQ);

    /**
     * @return the operator token of a static user-defined operator of a class or a struct, null for another method
     */
    @Nullable
    @RequiredReadAction
    static IElementType getStaticOperator(CSharpMethodDeclaration method) {
        if (!method.isOperator() || isInstanceOperator(method) || !(method.getParent() instanceof DotNetTypeDeclaration type) || type.isInterface()) {
            return null;
        }
        return method.getOperatorElementType();
    }

    /**
     * @return true for a C# 14 instance operator - {@code void operator +=(C c)}, {@code void operator ++()}
     */
    @RequiredReadAction
    static boolean isInstanceOperator(CSharpMethodDeclaration method) {
        if (method.hasModifier(DotNetModifier.STATIC)) {
            return false;
        }
        IElementType operator = method.getOperatorElementType();
        return COMPOUND_ASSIGNMENTS.contains(operator) || INCREMENTS.contains(operator) && method.getParameters().length == 0;
    }

    /**
     * @return true for the containing type or, of a struct, its nullable
     */
    @RequiredReadAction
    static boolean isContainingType(@Nullable DotNetTypeRef typeRef, DotNetTypeDeclaration type) {
        if (typeRef == null) {
            return false;
        }
        String vmQName = type.getVmQName();
        return DotNetTypeRefUtil.isVmQNameEqual(typeRef, vmQName) || DotNetTypeRefUtil.isVmQNameEqual(CSharpNullableTypeUtil.unbox(typeRef), vmQName);
    }

    /**
     * @return the other type of a user-defined conversion of a class or a struct - the type that is not the containing one, null
     * for a conversion of neither (CS0556) or of an interface
     */
    @Nullable
    @RequiredReadAction
    static DotNetTypeRef getOtherConversionType(CSharpConversionMethodDeclaration conversion) {
        if (!(conversion.getParent() instanceof DotNetTypeDeclaration type) || type.isInterface()) {
            return null;
        }
        DotNetTypeRef[] parameters = conversion.getParameterTypeRefs();
        if (parameters.length != 1) {
            return null;
        }
        DotNetTypeRef to = conversion.getReturnTypeRef();
        if (isContainingType(to, type)) {
            return parameters[0];
        }
        return isContainingType(parameters[0], type) ? to : null;
    }
}
