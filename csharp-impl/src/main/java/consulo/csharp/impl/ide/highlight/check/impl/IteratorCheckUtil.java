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
import consulo.csharp.lang.impl.psi.source.CSharpAnonymousMethodExpression;
import consulo.csharp.lang.impl.psi.source.CSharpBreakStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpCatchStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpFinallyStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpTryStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpYieldStatementImpl;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * The iterators of the CS1621 - CS1631 and CS8403 checks
 *
 * @author VISTALL
 * @since 2026-11-14
 */
class IteratorCheckUtil {
    static final Set<String> ITERATOR_INTERFACES = Set.of("System.Collections.IEnumerable", "System.Collections.IEnumerator",
        "System.Collections.Generic.IEnumerable`1", "System.Collections.Generic.IEnumerator`1");

    static final Set<String> ASYNC_ITERATOR_INTERFACES = Set.of("System.Collections.Generic.IAsyncEnumerable`1", "System.Collections.Generic.IAsyncEnumerator`1");

    /**
     * The constructs around a yield statement in its method - Roslyn InFinallyBlock, InTryBlockOfTryCatch, InCatchBlock
     */
    record Context(boolean inFinally, boolean inTryOfTryCatch, boolean inCatch) {
    }

    @RequiredReadAction
    static Context context(CSharpYieldStatementImpl yield) {
        boolean inFinally = false, inTryOfTryCatch = false, inCatch = false;
        PsiElement prev = yield;
        for (PsiElement parent = yield.getParent(); parent != null && !(parent instanceof CSharpSimpleLikeMethodAsElement); parent = parent.getParent()) {
            if (parent instanceof CSharpFinallyStatementImpl) {
                inFinally = true;
            }
            else if (parent instanceof CSharpCatchStatementImpl) {
                inCatch = true;
            }
            // the block of the try - not its catches or finally
            else if (parent instanceof CSharpTryStatementImpl tryStatement && !(prev instanceof CSharpCatchStatementImpl) && !(prev instanceof CSharpFinallyStatementImpl)
                && tryStatement.getCatchStatements().length > 0) {
                inTryOfTryCatch = true;
            }
            prev = parent;
        }
        return new Context(inFinally, inTryOfTryCatch, inCatch);
    }

    static boolean isYieldBreak(CSharpYieldStatementImpl yield) {
        return PsiTreeUtil.getChildOfType(yield, CSharpBreakStatementImpl.class) != null;
    }

    /**
     * @return the method or the local function of a yield statement - null in a lambda or an anonymous method (CS1621)
     */
    @Nullable
    @RequiredReadAction
    static CSharpSimpleLikeMethodAsElement getIteratorOwner(CSharpYieldStatementImpl yield) {
        CSharpSimpleLikeMethodAsElement owner = PsiTreeUtil.getParentOfType(yield, CSharpSimpleLikeMethodAsElement.class);
        return owner instanceof CSharpAnonymousMethodExpression ? null : owner;
    }

    @Nullable
    @RequiredReadAction
    static String getVmQName(DotNetTypeRef typeRef) {
        return typeRef.resolve().getElement() instanceof DotNetTypeDeclaration type ? type.getVmQName() : null;
    }
}
