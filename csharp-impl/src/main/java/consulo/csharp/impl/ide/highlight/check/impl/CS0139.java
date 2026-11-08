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
import consulo.csharp.lang.impl.psi.source.*;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetStatement;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * No enclosing loop out of which to break or continue - a break out of no loop or switch, a continue out of no loop,
 * up to the method or local function; at a lambda with one outside it - CS1632
 *
 * @author VISTALL
 */
public class CS0139 extends CompilerCheck<DotNetStatement> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetStatement element) {
        return target(element) == Target.NONE ? newBuilder(element.getFirstChild()) : null;
    }

    enum Target {
        LOOP,
        NONE,
        // a lambda between the jump and its loop
        LAMBDA
    }

    /**
     * Where a break or a continue goes - null for another statement
     */
    @RequiredReadAction
    static @Nullable Target target(DotNetStatement statement) {
        boolean isBreak = statement instanceof CSharpBreakStatementImpl;
        if (!isBreak && !(statement instanceof CSharpContinueStatementImpl) || statement.getParent() instanceof CSharpYieldStatementImpl) {
            return null;
        }
        boolean lambda = false;
        for (PsiElement parent = statement.getParent(); parent != null; parent = parent.getParent()) {
            if (isLoop(parent) || isBreak && parent instanceof CSharpSwitchStatementImpl) {
                return lambda ? Target.LAMBDA : Target.LOOP;
            }
            if (parent instanceof CSharpLambdaExpressionImpl || parent instanceof CSharpDelegateExpressionImpl) {
                lambda = true;
            }
            else if (parent instanceof CSharpSimpleLikeMethodAsElement) {
                break;
            }
        }
        return Target.NONE;
    }

    private static boolean isLoop(PsiElement element) {
        return element instanceof CSharpWhileStatementImpl || element instanceof CSharpDoWhileStatementImpl
            || element instanceof CSharpForStatementImpl || element instanceof CSharpForeachStatementImpl;
    }
}
