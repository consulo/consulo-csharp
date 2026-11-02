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

package consulo.csharp.impl.ide.highlight.check.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.CSharpTargetTypeUtil;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.CSharpConditionalExpressionImpl;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpElementGroupTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpNullTypeRef;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.scope.GlobalSearchScope;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 04-Nov-17
 */
public class CS0173 extends CompilerCheck<CSharpConditionalExpressionImpl> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpConditionalExpressionImpl element) {
        DotNetExpression trueExpression = element.getTrueExpression();
        DotNetExpression falseExpression = element.getFalseExpression();
        if (trueExpression == null || falseExpression == null) {
            return null;
        }

        return isWithoutType(element, languageVersion) ? newBuilder(element, formatTypeRef(trueExpression.toTypeRef(true)), formatTypeRef(falseExpression.toTypeRef(true))) : null;
    }

    /**
     * @return true if neither branch converts to the other, nor both to the type of the position (C# 9)
     */
    @RequiredReadAction
    public static boolean isWithoutType(CSharpConditionalExpressionImpl element, CSharpLanguageVersion languageVersion) {
        DotNetExpression trueExpression = element.getTrueExpression();
        DotNetExpression falseExpression = element.getFalseExpression();
        if (trueExpression == null || falseExpression == null) {
            return false;
        }

        DotNetTypeRef trueTypeRef = trueExpression.toTypeRef(true);
        DotNetTypeRef falseTypeRef = falseExpression.toTypeRef(true);
        if (trueTypeRef instanceof CSharpNullTypeRef && falseTypeRef instanceof CSharpNullTypeRef) {
            return true;
        }

        // null, throw, lambdas and method groups have no type of their own - they take the one of the other branch
        if (!hasOwnType(trueTypeRef) || !hasOwnType(falseTypeRef)) {
            return false;
        }

        GlobalSearchScope scope = element.getResolveScope();
        if (CSharpTypeUtil.isInheritableWithImplicit(trueTypeRef, falseTypeRef, scope) || CSharpTypeUtil.isInheritableWithImplicit(falseTypeRef, trueTypeRef, scope)) {
            return false;
        }

        // C# 9: with no natural type, both branches may convert to the type of the position - Shape s = c ? circle : square
        if (languageVersion.isAtLeast(CSharpLanguageVersion._9_0)) {
            DotNetTypeRef targetTypeRef = CSharpTargetTypeUtil.targetTypeRef(element);
            if (!CSharpTypeUtil.isErrorTypeRef(targetTypeRef)
                && CSharpTypeUtil.isInheritableWithImplicit(targetTypeRef, trueTypeRef, scope)
                && CSharpTypeUtil.isInheritableWithImplicit(targetTypeRef, falseTypeRef, scope)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasOwnType(DotNetTypeRef typeRef) {
        return !(typeRef instanceof CSharpNullTypeRef)
            && !(typeRef instanceof CSharpLambdaTypeRef)
            && !(typeRef instanceof CSharpElementGroupTypeRef)
            && typeRef != DotNetTypeRef.AUTO_TYPE
            && !CSharpTypeUtil.isErrorTypeRef(typeRef);
    }
}
