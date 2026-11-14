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
import consulo.csharp.lang.impl.psi.CSharpImplicitReturnModel;
import consulo.csharp.lang.impl.psi.controlFlow.CSharpControlFlow;
import consulo.csharp.lang.impl.psi.source.CSharpAnonymousMethodExpression;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaExpressionImplUtil;
import consulo.csharp.lang.impl.psi.source.CSharpReturnStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpTopLevelStatementsImpl;
import consulo.csharp.lang.impl.psi.source.CSharpYieldStatementImpl;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaResolveResult;
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
 * Since the method returns void, a return keyword must not be followed by an object expression - with the other
 * return statements not fitting their method: CS0126 (no value where one is required), CS1997 (a value from an async
 * Task method), CS1622 (a return in an iterator), CS8030 (a value from a lambda converted to a void delegate)
 *
 * @author VISTALL
 */
public class CS0127 extends CompilerCheck<CSharpReturnStatementImpl> {
    /**
     * The error of a return statement - its code, the type of the value required (CS0126)
     */
    record ReturnError(String code, CSharpSimpleLikeMethodAsElement owner, @Nullable DotNetTypeRef required) {
    }

    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpReturnStatementImpl element) {
        ReturnError error = returnError(element);
        return error != null && error.code().equals("CS0127") ? newBuilder(element.getFirstChild(), formatElement(error.owner())) : null;
    }

    @RequiredReadAction
    static @Nullable ReturnError returnError(CSharpReturnStatementImpl statement) {
        // yield return
        if (statement.getParent() instanceof CSharpYieldStatementImpl) {
            return null;
        }
        CSharpSimpleLikeMethodAsElement owner = PsiTreeUtil.getParentOfType(statement, CSharpSimpleLikeMethodAsElement.class);
        if (owner == null || owner instanceof CSharpTopLevelStatementsImpl) {
            return null;
        }
        if (isIterator(owner)) {
            return new ReturnError("CS1622", owner, null);
        }

        boolean lambda = owner instanceof CSharpAnonymousMethodExpression;
        DotNetTypeRef returnTypeRef;
        if (lambda) {
            CSharpLambdaResolveResult delegate = CSharpLambdaExpressionImplUtil.resolveLeftLambdaTypeRef(owner);
            if (delegate == null) {
                return null;
            }
            returnTypeRef = delegate.getReturnTypeRef();
        }
        else {
            returnTypeRef = owner instanceof DotNetConstructorDeclaration ? null : owner.getReturnTypeRef();
        }
        if (returnTypeRef == DotNetTypeRef.ERROR_TYPE || returnTypeRef == DotNetTypeRef.UNKNOWN_TYPE || returnTypeRef == DotNetTypeRef.AUTO_TYPE) {
            return null;
        }

        boolean asyncTask = false;
        if (returnTypeRef != null && owner.hasModifier(CSharpModifier.ASYNC)) {
            DotNetTypeRef awaited = CSharpImplicitReturnModel.Async.extractTypeRefImpl(returnTypeRef);
            asyncTask = awaited != null && isVoid(awaited);
            returnTypeRef = awaited;
        }
        boolean hasValue = statement.getExpression() != null;
        boolean voidReturn = returnTypeRef == null || isVoid(returnTypeRef);
        if (hasValue && voidReturn) {
            return new ReturnError(asyncTask ? "CS1997" : lambda ? "CS8030" : "CS0127", owner, null);
        }
        if (!hasValue && !voidReturn) {
            return new ReturnError("CS0126", owner, returnTypeRef);
        }
        return null;
    }

    @RequiredReadAction
    static boolean isIterator(CSharpSimpleLikeMethodAsElement owner) {
        CSharpControlFlow flow = CSharpControlFlow.of(owner);
        if (flow != null) {
            return flow.isIterator();
        }
        for (CSharpYieldStatementImpl yield : PsiTreeUtil.findChildrenOfType(owner, CSharpYieldStatementImpl.class)) {
            if (PsiTreeUtil.getParentOfType(yield, CSharpSimpleLikeMethodAsElement.class) == owner) {
                return true;
            }
        }
        return false;
    }

    @RequiredReadAction
    private static boolean isVoid(DotNetTypeRef typeRef) {
        return DotNetTypeRefUtil.isVmQNameEqual(typeRef, DotNetTypes.System.Void);
    }
}
