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

package consulo.csharp.lang.impl.psi.source.resolve.overrideSystem;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.progress.ProgressManager;
import consulo.application.util.function.CommonProcessors;
import consulo.application.util.query.Query;
import consulo.csharp.lang.impl.psi.CSharpElementCompareUtil;
import consulo.csharp.lang.impl.psi.msil.CSharpTransform;
import consulo.csharp.lang.impl.psi.resolve.CSharpElementGroupImpl;
import consulo.csharp.lang.impl.psi.resolve.CSharpResolveContextUtil;
import consulo.csharp.lang.impl.psi.source.resolve.ExecuteTarget;
import consulo.csharp.lang.impl.psi.source.resolve.ExecuteTargetUtil;
import consulo.csharp.lang.impl.psi.source.resolve.MemberResolveScopeProcessor;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.resolve.*;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.search.searches.TypeInheritorsSearch;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.logging.Logger;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.SmartList;
import jakarta.annotation.Nonnull;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author VISTALL
 * @since 14.12.14
 */
public class OverrideUtil {
    private static final Logger LOGGER = Logger.getInstance(OverrideUtil.class);

    @RequiredReadAction
    public static CSharpModifier getRequiredOverrideModifier(@Nonnull DotNetModifierListOwner modifierListOwner) {
        DotNetModifierList modifierList = modifierListOwner.getModifierList();
        if (modifierList == null) {
            return null;
        }

        if (modifierList.hasModifier(CSharpModifier.INTERFACE_ABSTRACT)) {
            return null;
        }

        if (modifierList.hasModifierInTree(CSharpModifier.ABSTRACT) || modifierList.hasModifierInTree(CSharpModifier.VIRTUAL) || modifierList.hasModifierInTree(CSharpModifier.OVERRIDE)) {
            return CSharpModifier.OVERRIDE;
        }
        return CSharpModifier.NEW;
    }

    @Nonnull
    public static Collection<PsiElement> filterOverrideElements(@Nonnull PsiScopeProcessor processor,
                                                                @Nonnull PsiElement scopeElement,
                                                                @Nonnull Collection<PsiElement> psiElements,
                                                                @Nonnull OverrideProcessor overrideProcessor) {
        if (psiElements.size() == 0) {
            return psiElements;
        }

        if (!ExecuteTargetUtil.canProcess(processor, ExecuteTarget.ELEMENT_GROUP, ExecuteTarget.EVENT, ExecuteTarget.PROPERTY)) {
            return CSharpResolveUtil.mergeGroupsToIterable(psiElements);
        }

        List<PsiElement> elements = CSharpResolveUtil.mergeGroupsToIterable(psiElements);

        return filterOverrideElements(scopeElement, elements, overrideProcessor);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    @RequiredReadAction
    public static List<PsiElement> filterOverrideElements(@Nonnull PsiElement scopeElement, @Nonnull Collection<PsiElement> elements, @Nonnull OverrideProcessor overrideProcessor) {
        List<PsiElement> copyElements = new ArrayList<>(elements);

        for (PsiElement element : elements) {
            ProgressManager.checkCanceled();

            if (!copyElements.contains(element)) {
                continue;
            }

            if (element instanceof DotNetVirtualImplementOwner) {
                if (element instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) element).isDelegate()) {
                    continue;
                }
                DotNetVirtualImplementOwner virtualImplementOwner = (DotNetVirtualImplementOwner) element;

                DotNetType typeForImplement = virtualImplementOwner.getTypeForImplement();

                for (PsiElement tempIterateElement : elements) {
                    // skip self
                    if (tempIterateElement == element) {
                        continue;
                    }

                    ProgressManager.checkCanceled();

                    if (CSharpElementCompareUtil.isEqual(tempIterateElement, element, CSharpElementCompareUtil.CHECK_RETURN_TYPE)) {
                        if (!overrideProcessor.elementOverride(virtualImplementOwner, (DotNetVirtualImplementOwner) tempIterateElement)) {
                            return Collections.emptyList();
                        }
                        copyElements.remove(tempIterateElement);
                    }
                }

                // if he have hide impl, remove it
                if (typeForImplement != null) {
                    copyElements.remove(element);
                }
            }
        }

        List<PsiElement> groupElements = new SmartList<>();
        List<PsiElement> elseElements = new SmartList<>();

        for (PsiElement copyElement : copyElements) {
            ProgressManager.checkCanceled();

            if (copyElement instanceof DotNetLikeMethodDeclaration) {
                groupElements.add(copyElement);
            }
            else {
                elseElements.add(copyElement);
            }
        }

        if (elseElements.isEmpty() && groupElements.isEmpty()) {
            return Collections.emptyList();
        }
        else if (elseElements.isEmpty()) {
            return Collections.singletonList(new CSharpElementGroupImpl<>(scopeElement.getProject(), getNameForGroup(groupElements), groupElements));
        }
        else if (groupElements.isEmpty()) {
            return elseElements;
        }
        else {
            elseElements.add(new CSharpElementGroupImpl<>(scopeElement.getProject(), getNameForGroup(groupElements), groupElements));
            return elseElements;
        }
    }

    @Nonnull
    private static String getNameForGroup(List<PsiElement> elements) {
        assert !elements.isEmpty();
        PsiElement element = elements.get(0);
        if (element instanceof DotNetVariable) {
            return ((DotNetVariable) element).getName();
        }
        else if (element instanceof CSharpIndexMethodDeclaration) {
            return "this[]";
        }
        else if (element instanceof DotNetLikeMethodDeclaration) {
            String name = ((DotNetLikeMethodDeclaration) element).getName();
            assert name != null : element.getClass().getName();
            return name;
        }
        else {
            LOGGER.error(element.getClass() + " is not handled");
            return "override";
        }
    }

    @RequiredReadAction
    public static boolean isAllowForOverride(@Nonnull PsiElement parent) {
        if (!(parent instanceof DotNetVirtualImplementOwner)) {
            return false;
        }

        if (parent instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) parent).isDelegate()) {
            return false;
        }

        if (parent instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) parent).hasModifier(CSharpModifier.STATIC)) {
            return false;
        }
        return true;
    }

    @Nonnull
    @RequiredReadAction
    public static Collection<DotNetVirtualImplementOwner> collectOverridingMembers(final DotNetVirtualImplementOwner target) {
        PsiElement parent = target.getParent();
        if (parent == null) {
            return Collections.emptyList();
        }
        OverrideProcessor.Collector overrideProcessor = new OverrideProcessor.Collector();

        MemberResolveScopeProcessor processor = new MemberResolveScopeProcessor(parent, CommonProcessors.<ResolveResult>alwaysTrue(), new ExecuteTarget[]{
            ExecuteTarget.MEMBER,
            ExecuteTarget.ELEMENT_GROUP
        }, overrideProcessor);

        ResolveState state = ResolveState.initial();
        if (target instanceof CSharpIndexMethodDeclaration) {
            state = state.put(CSharpResolveUtil.SELECTOR, StaticResolveSelectors.INDEX_METHOD_GROUP);
        }
        else {
            String name = ((PsiNamedElement) target).getName();
            if (name == null) {
                return Collections.emptyList();
            }
            state = state.put(CSharpResolveUtil.SELECTOR, new MemberByNameSelector(name));
        }

        CSharpResolveUtil.walkChildren(processor, parent, false, true, state);

        List<DotNetVirtualImplementOwner> results = overrideProcessor.getResults();

        // need filter result due it ill return all elements with target selector
        ListIterator<DotNetVirtualImplementOwner> listIterator = results.listIterator();
        while (listIterator.hasNext()) {
            ProgressManager.checkCanceled();

            DotNetVirtualImplementOwner next = listIterator.next();
            if (!CSharpElementCompareUtil.isEqual(next, target, CSharpElementCompareUtil.CHECK_RETURN_TYPE)) {
                listIterator.remove();
            }
        }
        return results;
    }

    @Nonnull
    @RequiredReadAction
    public static Collection<DotNetVirtualImplementOwner> collectOverridenMembers(final DotNetVirtualImplementOwner target) {
        PsiElement parent = target.getParent();
        if (!(parent instanceof DotNetTypeDeclaration)) {
            return Collections.emptyList();
        }
        final CSharpResolveSelector selector;
        if (target instanceof CSharpIndexMethodDeclaration) {
            selector = StaticResolveSelectors.INDEX_METHOD_GROUP;
        }
        else {
            String name = ((PsiNamedElement) target).getName();
            if (name != null) {
                selector = new MemberByNameSelector(name);
            }
            else {
                selector = null;
            }
        }

        if (selector == null) {
            return Collections.emptyList();
        }
        final GlobalSearchScope resolveScope = target.getResolveScope();

        final List<DotNetVirtualImplementOwner> list = new ArrayList<>();
        Query<DotNetTypeDeclaration> search = TypeInheritorsSearch.search((DotNetTypeDeclaration) parent, true, CSharpTransform.INSTANCE);
        search.forEach(new Predicate<>() {
            @Override
            @RequiredReadAction
            public boolean test(DotNetTypeDeclaration typeDeclaration) {
                CSharpResolveContext context = CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, resolveScope, typeDeclaration);

                Collection<PsiElement> elements = selector.doSelectElement(context, false);
                for (PsiElement element : CSharpResolveUtil.mergeGroupsToIterable(elements)) {
                    if (element == target) {
                        continue;
                    }
                    if (CSharpElementCompareUtil.isEqual(element, target, CSharpElementCompareUtil.CHECK_RETURN_TYPE)) {
                        list.add((DotNetVirtualImplementOwner) element);
                    }
                }
                return true;
            }
        });

        return list;
    }

    @Nonnull
    @RequiredReadAction
    public static Collection<DotNetModifierListOwner> collectMembersWithModifier(@Nonnull PsiElement element, @Nonnull DotNetGenericExtractor extractor, @Nonnull CSharpModifier modifier) {
        List<DotNetModifierListOwner> psiElements = new SmartList<>();
        for (PsiElement psiElement : getAllMembers(element, element.getResolveScope(), extractor, false, true)) {
            ProgressManager.checkCanceled();

            if (psiElement instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) psiElement).hasModifier(modifier)) {
                psiElements.add((DotNetModifierListOwner) psiElement);
            }
        }
        return psiElements;
    }

    @Nonnull
    @RequiredReadAction
    public static Collection<PsiElement> getAllMembers(@Nonnull final PsiElement targetTypeDeclaration,
                                                       @Nonnull GlobalSearchScope scope,
                                                       @Nonnull DotNetGenericExtractor extractor,
                                                       boolean completion,
                                                       boolean overrideTool) {
        final CommonProcessors.CollectProcessor<PsiElement> collectProcessor = new CommonProcessors.CollectProcessor<>();
        CSharpResolveContext context = CSharpResolveContextUtil.createContext(extractor, scope, targetTypeDeclaration);
        // process method & properties
        context.processElements(collectProcessor, true);
        // process index methods
        CSharpElementGroup<CSharpIndexMethodDeclaration> group = context.indexMethodGroup(true);
        if (group != null) {
            group.process(collectProcessor);
        }

        Collection<PsiElement> results = collectProcessor.getResults();

        List<PsiElement> mergedElements = CSharpResolveUtil.mergeGroupsToIterable(results);
        List<PsiElement> psiElements = OverrideUtil.filterOverrideElements(targetTypeDeclaration, mergedElements, OverrideProcessor.ALWAYS_TRUE);

        List<PsiElement> elements = CSharpResolveUtil.mergeGroupsToIterable(psiElements);
        if (overrideTool) {
            // filter self methods, we need it in circular extends
            elements = ContainerUtil.filter(elements, element ->
            {
                ProgressManager.checkCanceled();
                return element.getParent() != targetTypeDeclaration;
            });
        }
        return completion ? elements : ContainerUtil.filter(elements, element ->
        {
            ProgressManager.checkCanceled();
            return !(element instanceof DotNetTypeDeclaration);
        });
    }
}
