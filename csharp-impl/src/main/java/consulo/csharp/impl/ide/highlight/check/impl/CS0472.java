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
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.lang.impl.psi.source.CSharpBinaryExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpConstantExpressionImpl;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.ast.IElementType;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * {@code i == null} of an int - the result is always false, a value type is never equal to null
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0472 extends CompilerCheck<CSharpBinaryExpressionImpl> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpBinaryExpressionImpl element) {
        IElementType operator = element.getOperatorElement().getOperatorElementType();
        if (operator != CSharpTokens.EQEQ && operator != CSharpTokens.NTEQ) {
            return null;
        }
        DotNetExpression left = element.getLeftExpression();
        DotNetExpression right = element.getRightExpression();
        DotNetExpression value = isNull(right) ? left : isNull(left) ? right : null;
        if (value == null || isNull(value) || !(value.toTypeRef(true).resolve().getElement() instanceof DotNetTypeDeclaration type) || !hasLiftedEquality(type)) {
            return null;
        }
        return newBuilder(element, operator == CSharpTokens.NTEQ ? "true" : "false", formatTypeRef(value.toTypeRef(true)));
    }

    private static final Set<String> PREDEFINED = Set.of(DotNetTypes.System.Boolean, DotNetTypes.System.Char, DotNetTypes.System.SByte, DotNetTypes.System.Byte,
        DotNetTypes.System.Int16, DotNetTypes.System.UInt16, DotNetTypes.System.Int32, DotNetTypes.System.UInt32, DotNetTypes.System.Int64, DotNetTypes.System.UInt64,
        DotNetTypes.System.Single, DotNetTypes.System.Double, DotNetTypes.System.Decimal, DotNetTypes.System.IntPtr, DotNetTypes.System.UIntPtr);

    private static boolean isNull(@Nullable DotNetExpression expression) {
        return expression instanceof CSharpConstantExpressionImpl constant && constant.getLiteralType() == CSharpTokens.NULL_LITERAL;
    }

    /**
     * @return true for a value type with an == lifted to nullable - the predefined ones, an enum, a struct with operator ==
     */
    @RequiredReadAction
    private static boolean hasLiftedEquality(DotNetTypeDeclaration type) {
        if (PREDEFINED.contains(type.getVmQName()) || type.isEnum()) {
            return true;
        }
        if (!type.isStruct() || DotNetTypes.System.Nullable$1.equals(type.getVmQName())) {
            return false;
        }
        for (DotNetNamedElement member : type.getMembers()) {
            if (member instanceof CSharpMethodDeclaration method && method.isOperator() && method.getOperatorElementType() == CSharpTokens.EQEQ) {
                return true;
            }
        }
        return false;
    }
}
