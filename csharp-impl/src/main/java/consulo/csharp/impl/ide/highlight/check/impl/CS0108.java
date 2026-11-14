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
import consulo.application.util.CachedValueProvider;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.CSharpElementCompareUtil;
import consulo.csharp.lang.impl.psi.resolve.CSharpResolveContextUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import consulo.csharp.lang.psi.resolve.StaticResolveSelectors;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.util.LanguageCachedValueUtil;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The member hides inherited member - use the new keyword if hiding was intended: a member of a class without new
 * nor override of the name and parameters of an accessible member of a base class; a virtual one - CS0114
 *
 * @author VISTALL
 */
public class CS0108 extends CompilerCheck<DotNetModifierListOwner> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetModifierListOwner element) {
        PsiElement hidden = hiddenWithoutModifier(element);
        return hidden != null && !isVirtual(hidden) ? newBuilder(getNameIdentifier(element), formatElement(element), formatElement(hidden)) : null;
    }

    /**
     * The member hidden by one without new or override - null if none
     */
    @RequiredReadAction
    static @Nullable PsiElement hiddenWithoutModifier(DotNetModifierListOwner element) {
        if (element.hasModifier(CSharpModifier.NEW) || element.hasModifier(CSharpModifier.OVERRIDE)) {
            return null;
        }
        return hiddenMember(element);
    }

    /**
     * Virtual, abstract or override - one to override, not to hide
     */
    @RequiredReadAction
    static boolean isVirtual(PsiElement member) {
        return member instanceof DotNetModifierListOwner owner
            && (owner.hasModifier(CSharpModifier.VIRTUAL) || owner.hasModifier(CSharpModifier.ABSTRACT) || owner.hasModifier(CSharpModifier.OVERRIDE));
    }

    /**
     * The member of the nearest base class of the same name and parameters, not private - null for none, or for a member
     * which can not hide (of an interface, an explicit implementation, a constructor, an operator, a delegate)
     */
    @RequiredReadAction
    static @Nullable PsiElement hiddenMember(DotNetModifierListOwner element) {
        if (!isHiding(element)) {
            return null;
        }
        return LanguageCachedValueUtil.getCachedValue(element, () -> CachedValueProvider.Result.create(findHidden(element), PsiModificationTracker.MODIFICATION_COUNT));
    }

    /**
     * A member of a class which can hide one of a base class
     */
    @RequiredReadAction
    static boolean isHiding(DotNetModifierListOwner element) {
        if (!(element.getParent() instanceof CSharpTypeDeclaration type) || type.isInterface()) {
            return false;
        }
        if (element instanceof DotNetVirtualImplementOwner implementOwner && implementOwner.getTypeForImplement() != null) {
            return false;
        }
        if (element instanceof CSharpMethodDeclaration method) {
            return !method.isDelegate() && !method.isOperator();
        }
        return element instanceof CSharpPropertyDeclaration || element instanceof CSharpIndexMethodDeclaration
            || element instanceof CSharpEventDeclaration || element instanceof CSharpFieldDeclaration;
    }

    @RequiredReadAction
    private static @Nullable PsiElement findHidden(DotNetModifierListOwner element) {
        CSharpTypeDeclaration type = (CSharpTypeDeclaration) element.getParent();
        CSharpResolveSelector selector;
        if (element instanceof CSharpIndexMethodDeclaration) {
            selector = StaticResolveSelectors.INDEX_METHOD_GROUP;
        }
        else if (element instanceof DotNetNamedElement named && named.getName() != null) {
            selector = new MemberByNameSelector(named.getName());
        }
        else {
            return null;
        }

        Set<DotNetTypeDeclaration> visited = new HashSet<>();
        visited.add(type);
        DotNetTypeRef baseTypeRef = baseClass(type);
        while (baseTypeRef != null) {
            DotNetTypeResolveResult result = baseTypeRef.resolve();
            if (!(result.getElement() instanceof DotNetTypeDeclaration base) || !visited.add(base)) {
                return null;
            }
            DotNetGenericExtractor extractor = result.getGenericExtractor();
            CSharpResolveContext context = CSharpResolveContextUtil.createContext(extractor, element.getResolveScope(), base);
            List<PsiElement> members = CSharpResolveUtil.mergeGroupsToIterable(selector.doSelectElement(context, false));
            for (PsiElement member : members) {
                if (member instanceof DotNetModifierListOwner owner && !owner.hasModifier(CSharpModifier.PRIVATE)
                    && CSharpElementCompareUtil.isEqual(member, element, 0)) {
                    return member;
                }
            }
            DotNetTypeRef next = baseClass(base);
            baseTypeRef = next == null ? null : GenericUnwrapTool.exchangeTypeRef(next, extractor);
        }
        return null;
    }

    /**
     * The class it extends - null for none
     */
    @RequiredReadAction
    static @Nullable DotNetTypeRef baseClass(DotNetTypeDeclaration type) {
        for (DotNetTypeRef typeRef : type.getExtendTypeRefs()) {
            if (typeRef.resolve().getElement() instanceof DotNetTypeDeclaration base && !base.isInterface()) {
                return typeRef;
            }
        }
        return null;
    }
}
