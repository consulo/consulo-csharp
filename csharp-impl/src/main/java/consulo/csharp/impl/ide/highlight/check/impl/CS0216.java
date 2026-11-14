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
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpOperatorNameHelper;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetNamedElement;
import java.util.Map;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import org.jspecify.annotations.Nullable;

/**
 * {@code operator ==} without {@code operator !=} - the comparison operators and true / false are pairs
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0216 extends CompilerCheck<CSharpMethodDeclaration> {
    private static final Map<IElementType, IElementType> PAIRS = Map.of(CSharpTokens.EQEQ, CSharpTokens.NTEQ, CSharpTokens.NTEQ, CSharpTokens.EQEQ,
        CSharpTokens.LT, CSharpTokens.GT, CSharpTokens.GT, CSharpTokens.LT, CSharpTokens.LTEQ, CSharpTokens.GTEQ, CSharpTokens.GTEQ, CSharpTokens.LTEQ,
        CSharpTokens.TRUE_KEYWORD, CSharpTokens.FALSE_KEYWORD, CSharpTokens.FALSE_KEYWORD, CSharpTokens.TRUE_KEYWORD);

    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpMethodDeclaration element) {
        IElementType operator = OperatorCheckUtil.getStaticOperator(element);
        IElementType pair = operator == null ? null : PAIRS.get(operator);
        if (pair == null) {
            return null;
        }
        DotNetTypeDeclaration type = CSharpCompositeTypeDeclaration.selectCompositeOrSelfType((DotNetTypeDeclaration) element.getParent());
        for (DotNetNamedElement member : type.getMembers()) {
            // the pair has the same signature - int operator true(C) and bool operator false(C) are no pair
            if (member instanceof CSharpMethodDeclaration method && method.isOperator() && method.getOperatorElementType() == pair && isSameSignature(method, element)) {
                return null;
            }
        }
        return newBuilder(getNameIdentifier(element), formatElement(element), CSharpOperatorNameHelper.getOperatorName(pair));
    }

    @RequiredReadAction
    private static boolean isSameSignature(CSharpMethodDeclaration a, CSharpMethodDeclaration b) {
        DotNetTypeRef[] aParameters = a.getParameterTypeRefs();
        DotNetTypeRef[] bParameters = b.getParameterTypeRefs();
        if (aParameters.length != bParameters.length || !CSharpTypeUtil.isTypeEqual(a.getReturnTypeRef(), b.getReturnTypeRef())) {
            return false;
        }
        for (int i = 0; i < aParameters.length; i++) {
            if (!CSharpTypeUtil.isTypeEqual(aParameters[i], bParameters[i])) {
                return false;
            }
        }
        return true;
    }
}
