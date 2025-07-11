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

package consulo.csharp.lang.impl.psi.source.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.resolve.CSharpNamedResolveSelector;
import consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.resolve.ResolveState;
import jakarta.annotation.Nonnull;

import java.util.function.Predicate;

/**
 * @author VISTALL
 * @since 09.10.14
 */
public class SimpleNamedScopeProcessor extends StubScopeProcessor {
    private Predicate<ResolveResult> myCompletionProcessor;
    private boolean myCompletion;

    public SimpleNamedScopeProcessor(@Nonnull final StubScopeProcessor completionProcessor, boolean completion, ExecuteTarget... targets) {
        this(resolveResult ->
        {
            completionProcessor.pushResultExternally(resolveResult);
            return true;
        }, completion, targets);
    }

    public SimpleNamedScopeProcessor(@Nonnull Predicate<ResolveResult> completionProcessor, boolean completion, ExecuteTarget... targets) {
        myCompletionProcessor = completionProcessor;
        myCompletion = completion;
        putUserData(ExecuteTargetUtil.EXECUTE_TARGETS, ExecuteTargetUtil.of(targets));
    }

    @RequiredReadAction
    @Override
    public boolean execute(@Nonnull PsiElement element, ResolveState state) {
        if (!(element instanceof PsiNamedElement) || !ExecuteTargetUtil.isMyElement(this, element)) {
            return true;
        }

        String name = ((PsiNamedElement) element).getName();
        if (name == null) {
            return true;
        }

        if (myCompletion) {
            return myCompletionProcessor.test(new CSharpResolveResult(element));
        }
        else {
            CSharpResolveSelector selector = state.get(CSharpResolveUtil.SELECTOR);
            if (!(selector instanceof CSharpNamedResolveSelector)) {
                return true;
            }

            if (((CSharpNamedResolveSelector) selector).isNameEqual(name)) {
                myCompletionProcessor.test(new CSharpResolveResult(element));
                return false;
            }
        }
        return true;
    }
}
