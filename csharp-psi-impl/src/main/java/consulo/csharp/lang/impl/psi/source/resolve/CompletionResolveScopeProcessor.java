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
import consulo.application.progress.ProgressManager;
import consulo.csharp.lang.impl.psi.CSharpContextUtil;
import consulo.csharp.lang.impl.psi.CSharpVisibilityUtil;
import consulo.csharp.lang.impl.psi.resolve.CSharpResolveContextUtil;
import consulo.csharp.lang.impl.psi.source.resolve.overrideSystem.OverrideUtil;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.scope.GlobalSearchScope;
import jakarta.annotation.Nonnull;

import java.util.function.Predicate;

/**
 * @author VISTALL
 * @since 23.10.14
 */
public class CompletionResolveScopeProcessor extends StubScopeProcessor {
    @Nonnull
    private final GlobalSearchScope myScope;
    @Nonnull
    private final PsiElement myPlace;
    @Nonnull
    private CSharpContextUtil.ContextType myContextType;
    @Nonnull
    private Predicate<ResolveResult> myProcessor;

    @RequiredReadAction
    public CompletionResolveScopeProcessor(@Nonnull CSharpResolveOptions options,
                                           @Nonnull Predicate<ResolveResult> processor,
                                           @Nonnull ExecuteTarget[] targets) {
        myProcessor = processor;
        myPlace = options.getElement();

        myScope = myPlace.getResolveScope();
        CSharpContextUtil.ContextType completionContextType = options.getCompletionContextType();
        if (completionContextType != null) {
            myContextType = completionContextType;
        }
        else {
            myContextType = myPlace instanceof CSharpReferenceExpression ? CSharpContextUtil.getParentContextTypeForReference((CSharpReferenceExpression) myPlace) : CSharpContextUtil.ContextType.ANY;
        }
        putUserData(ExecuteTargetUtil.EXECUTE_TARGETS, ExecuteTargetUtil.of(targets));
    }

    @Override
    public void pushResultExternally(@Nonnull ResolveResult resolveResult) {
        myProcessor.test(resolveResult);
    }

    @Override
    @RequiredReadAction
    public boolean execute(@Nonnull PsiElement element, ResolveState state) {
        DotNetGenericExtractor extractor = state.get(CSharpResolveUtil.EXTRACTOR);
        assert extractor != null;

        if (element instanceof CSharpTypeDeclaration) {
            for (PsiElement psiElement : OverrideUtil.getAllMembers(element, myScope, extractor, true, false)) {
                ProgressManager.checkCanceled();

                processElement(psiElement);
            }
        }
        else {
            CSharpResolveContext context = CSharpResolveContextUtil.createContext(extractor, myScope, element);

            return context.processElements(it ->
            {
                processElement(it);
                return true;
            }, true);
        }

        return true;
    }

    @RequiredReadAction
    private void processElement(@Nonnull PsiElement element) {
        if (!ExecuteTargetUtil.isMyElement(this, element)) {
            return;
        }

        if (element instanceof DotNetModifierListOwner && !CSharpVisibilityUtil.isVisible((DotNetModifierListOwner) element, myPlace)) {
            return;
        }

        if (myContextType != CSharpContextUtil.ContextType.ANY) {
            CSharpContextUtil.ContextType contextForResolved = CSharpContextUtil.getContextForResolved(element);
            switch (myContextType) {
                case INSTANCE:
                    if (contextForResolved == CSharpContextUtil.ContextType.STATIC) {
                        return;
                    }
                    break;
                case STATIC:
                    if (contextForResolved.isAllowInstance()) {
                        return;
                    }
                    break;
            }
        }
        myProcessor.test(new CSharpResolveResult(element));
    }
}
