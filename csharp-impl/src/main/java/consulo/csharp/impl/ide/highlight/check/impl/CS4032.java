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
import consulo.csharp.lang.impl.psi.source.CSharpAwaitExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaExpressionImpl;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetConstructorDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

/**
 * The 'await' operator can only be used within an async method - of a type, changed to Task&lt;T&gt;; CS4033 for a
 * void one, CS4034 in a lambda or an anonymous method
 *
 * @author VISTALL
 */
public class CS4032 extends CompilerCheck<CSharpAwaitExpressionImpl> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpAwaitExpressionImpl element) {
        CSharpSimpleLikeMethodAsElement owner = notAsyncOwner(element);
        if (owner == null || owner instanceof CSharpAnonymousMethodExpression) {
            return null;
        }
        DotNetTypeRef returnTypeRef = returnTypeRef(owner);
        return returnTypeRef != null ? newBuilder(element.getFirstChild(), formatTypeRef(returnTypeRef)) : null;
    }

    /**
     * The owner of the await, not async - null if it is
     */
    @RequiredReadAction
    static @Nullable CSharpSimpleLikeMethodAsElement notAsyncOwner(CSharpAwaitExpressionImpl element) {
        CSharpSimpleLikeMethodAsElement owner = PsiTreeUtil.getParentOfType(element, CSharpSimpleLikeMethodAsElement.class);
        return owner == null || owner.hasModifier(CSharpModifier.ASYNC) ? null : owner;
    }

    /**
     * The type returned - null for void
     */
    @RequiredReadAction
    static @Nullable DotNetTypeRef returnTypeRef(CSharpSimpleLikeMethodAsElement owner) {
        if (owner instanceof DotNetConstructorDeclaration) {
            return null;
        }
        DotNetTypeRef returnTypeRef = owner.getReturnTypeRef();
        return DotNetTypeRefUtil.isVmQNameEqual(returnTypeRef, DotNetTypes.System.Void) ? null : returnTypeRef;
    }

    static String kind(CSharpSimpleLikeMethodAsElement owner) {
        return owner instanceof CSharpLambdaExpressionImpl ? "lambda expression" : "anonymous method";
    }
}
