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
import consulo.application.util.function.Processor;
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.impl.psi.resolve.CSharpElementGroupImpl;
import consulo.csharp.lang.impl.psi.resolve.CSharpResolveContextUtil;
import consulo.csharp.lang.impl.psi.source.CSharpReferenceExpressionImplUtil;
import consulo.csharp.lang.impl.psi.source.resolve.overrideSystem.OverrideProcessor;
import consulo.csharp.lang.impl.psi.source.resolve.overrideSystem.OverrideUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpGenericExtractor;
import consulo.csharp.lang.impl.psi.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.scope.GlobalSearchScope;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 17.12.13.
 */
public class MemberResolveScopeProcessor extends StubScopeProcessor {
    private final PsiElement myScopeElement;
    protected Processor<ResolveResult> myResultProcessor;
    private final GlobalSearchScope myResolveScope;
    private final OverrideProcessor myOverrideProcessor;

    public MemberResolveScopeProcessor(@Nonnull CSharpResolveOptions options, @Nonnull Processor<ResolveResult> resultProcessor, ExecuteTarget[] targets) {
        myScopeElement = options.getElement();
        myResolveScope = myScopeElement.getResolveScope();
        myResultProcessor = resultProcessor;
        myOverrideProcessor = OverrideProcessor.ALWAYS_TRUE;
        putUserData(ExecuteTargetUtil.EXECUTE_TARGETS, ExecuteTargetUtil.of(targets));
    }

    public MemberResolveScopeProcessor(@Nonnull PsiElement scopeElement,
                                       @Nonnull Processor<ResolveResult> resultProcessor,
                                       @Nullable ExecuteTarget[] targets,
                                       @Nullable OverrideProcessor overrideProcessor) {
        myScopeElement = scopeElement;
        myResultProcessor = resultProcessor;
        myResolveScope = scopeElement.getResolveScope();
        putUserData(ExecuteTargetUtil.EXECUTE_TARGETS, ExecuteTargetUtil.of(targets));
        myOverrideProcessor = overrideProcessor;
    }

    @Override
    public void pushResultExternally(@Nonnull ResolveResult resolveResult) {
        myResultProcessor.process(resolveResult);
    }

    @RequiredReadAction
    @Override
    public boolean execute(@Nonnull PsiElement element, ResolveState state) {
        CSharpResolveSelector selector = state.get(CSharpResolveUtil.SELECTOR);
        if (selector == null) {
            return true;
        }

        DotNetGenericExtractor extractor = state.get(CSharpResolveUtil.EXTRACTOR);
        assert extractor != null;

        CSharpResolveContext context = CSharpResolveContextUtil.createContext(extractor, myResolveScope, element);

        Collection<PsiElement> psiElements = selector.doSelectElement(context, state.get(CSharpResolveUtil.WALK_DEEP) == Boolean.TRUE);
        psiElements = applyTypeArguments(psiElements);
        psiElements = CSharpCompositeTypeDeclaration.wrapPartialTypes(myResolveScope, myScopeElement.getProject(), psiElements);

        for (PsiElement psiElement : OverrideUtil.filterOverrideElements(this, myScopeElement, psiElements, myOverrideProcessor)) {
            ProgressManager.checkCanceled();

            if (!ExecuteTargetUtil.isMyElement(this, psiElement)) {
                continue;
            }

            final CSharpResolveResult result = new CSharpResolveResult(psiElement);
            result.setProvider(element);
            result.setAssignable(myScopeElement);

            if (!myResultProcessor.process(result)) {
                return false;
            }
        }
        return true;
    }

    @RequiredReadAction
    private Collection<PsiElement> applyTypeArguments(Collection<PsiElement> psiElements) {
        if (!(myScopeElement instanceof CSharpReferenceExpression) || psiElements.size() == 0) {
            return psiElements;
        }

        int typeArgumentListSize = CSharpReferenceExpressionImplUtil.getTypeArgumentListSize(myScopeElement);
        if (typeArgumentListSize == 0) {
            return psiElements;
        }

        DotNetTypeRef[] typeArgumentListRefs = ((CSharpReferenceExpression) myScopeElement).getTypeArgumentListRefs();

        List<PsiElement> newPsiElements = new ArrayList<>(psiElements.size());
        for (PsiElement psiElement : psiElements) {
            ProgressManager.checkCanceled();

            PsiElement addItem = psiElement;

            if (psiElement instanceof CSharpElementGroup) {
                @SuppressWarnings("unchecked") CSharpElementGroup<PsiElement> elementGroup = (CSharpElementGroup<PsiElement>) psiElement;

                Collection<PsiElement> elements = elementGroup.getElements();

                boolean changed = false;
                final List<PsiElement> anotherItems = new ArrayList<>(elements.size());
                for (PsiElement temp : elements) {
                    if (temp instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) temp).getGenericParametersCount() == typeArgumentListSize) {
                        DotNetGenericParameter[] genericParameters = ((CSharpMethodDeclaration) temp).getGenericParameters();

                        DotNetGenericExtractor extractor = CSharpGenericExtractor.create(genericParameters, typeArgumentListRefs);

                        anotherItems.add(GenericUnwrapTool.extract((CSharpMethodDeclaration) temp, extractor));
                        changed = true;
                    }
                    else {
                        anotherItems.add(temp);
                    }
                }

                if (changed) {
                    addItem = new CSharpElementGroupImpl<>(elementGroup.getProject(), elementGroup.getKey(), anotherItems);
                }
            }

            newPsiElements.add(addItem);
        }
        return newPsiElements;
    }
}
