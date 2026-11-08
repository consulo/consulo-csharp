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
import consulo.csharp.lang.impl.psi.source.CSharpAnonymousMethodExpression;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaExpressionImplUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaTypeRef;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

/**
 * Not all code paths return a value in a lambda expression or an anonymous method of a delegate type returning a value
 *
 * @author VISTALL
 */
public class CS1643 extends CompilerCheck<CSharpAnonymousMethodExpression> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpAnonymousMethodExpression element) {
        if (!CS0161.isEndReachable(element)) {
            return null;
        }
        // the delegate converted to - or a natural type
        CSharpLambdaResolveResult delegate = CSharpLambdaExpressionImplUtil.resolveLeftLambdaTypeRef(element);
        DotNetTypeRef typeRef = element.toTypeRef(true);
        if (delegate == null && typeRef.resolve() instanceof CSharpLambdaResolveResult own) {
            delegate = own;
        }
        if (delegate == null || !CS0161.returnsValue(element, delegate.getReturnTypeRef())) {
            return null;
        }
        CSharpMethodDeclaration target = delegate.getTarget();
        if (target != null) {
            typeRef = new CSharpLambdaTypeRef(element.getProject(), element.getResolveScope(), target, delegate.getGenericExtractor());
        }

        boolean lambda = element instanceof CSharpLambdaExpressionImpl;
        PsiElement range = lambda ? ((CSharpLambdaExpressionImpl) element).getDArrow() : findDelegateKeyword(element);
        return newBuilder(range, lambda ? "lambda expression" : "anonymous method", formatTypeRef(typeRef));
    }

    private static PsiElement findDelegateKeyword(PsiElement element) {
        for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (PsiTreeUtil.getDeepestFirst(child).getNode().getElementType() == CSharpTokens.DELEGATE_KEYWORD) {
                return child;
            }
        }
        return element;
    }
}
