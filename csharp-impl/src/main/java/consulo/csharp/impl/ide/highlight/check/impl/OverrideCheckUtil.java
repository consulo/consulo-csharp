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
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.resolve.CSharpResolveContextUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * The member an override overrides - CS0239, CS0505, CS0508, CS0544, CS0545, CS0546, CS1715
 *
 * @author VISTALL
 * @since 2026-11-14
 */
class OverrideCheckUtil {
    /**
     * @return the overridden member of an override - the nearest base member of its signature
     */
    @Nullable
    @RequiredReadAction
    static DotNetModifierListOwner getOverridden(DotNetModifierListOwner element) {
        if (!element.hasModifier(CSharpModifier.OVERRIDE)) {
            return null;
        }
        return CS0108.hiddenMember(element) instanceof DotNetModifierListOwner owner ? owner : null;
    }

    /**
     * @return a base member of the name of an override which is not of its kind - a field for a property, a property for a method
     */
    @Nullable
    @RequiredReadAction
    static PsiElement getOtherKindMember(DotNetModifierListOwner element) {
        if (!element.hasModifier(CSharpModifier.OVERRIDE) || element instanceof CSharpIndexMethodDeclaration || !CS0108.isHiding(element)
            || CS0108.hiddenMember(element) != null || !(element instanceof DotNetNamedElement named) || named.getName() == null) {
            return null;
        }
        CSharpTypeDeclaration type = (CSharpTypeDeclaration) element.getParent();
        MemberByNameSelector selector = new MemberByNameSelector(named.getName());
        Set<DotNetTypeDeclaration> visited = new HashSet<>();
        visited.add(type);
        DotNetTypeRef baseTypeRef = CS0108.baseClass(type);
        while (baseTypeRef != null) {
            DotNetTypeResolveResult result = baseTypeRef.resolve();
            if (!(result.getElement() instanceof DotNetTypeDeclaration base) || !visited.add(base)) {
                return null;
            }
            CSharpResolveContext context = CSharpResolveContextUtil.createContext(result.getGenericExtractor(), element.getResolveScope(), base);
            for (PsiElement member : CSharpResolveUtil.mergeGroupsToIterable(selector.doSelectElement(context, false))) {
                if (member instanceof DotNetModifierListOwner owner && !owner.hasModifier(CSharpModifier.PRIVATE) && !isSameKind(element, member)) {
                    return member;
                }
            }
            DotNetTypeRef next = CS0108.baseClass(base);
            baseTypeRef = next == null ? null : GenericUnwrapTool.exchangeTypeRef(next, result.getGenericExtractor());
        }
        return null;
    }

    private static boolean isSameKind(PsiElement a, PsiElement b) {
        if (a instanceof CSharpMethodDeclaration) {
            return b instanceof CSharpMethodDeclaration;
        }
        if (a instanceof CSharpPropertyDeclaration) {
            return b instanceof CSharpPropertyDeclaration;
        }
        return a.getClass() == b.getClass();
    }

    /**
     * @return the type of a member - the return type of a method, the type of a property
     */
    @RequiredReadAction
    static DotNetTypeRef getTypeRef(PsiElement member) {
        if (member instanceof DotNetLikeMethodDeclaration method) {
            return method.getReturnTypeRef();
        }
        return member instanceof DotNetVariable variable ? variable.toTypeRef(false) : DotNetTypeRef.ERROR_TYPE;
    }

    /**
     * @return true for the type of the overridden member - or, C# 9, a covariant return of a method or a get-only property
     */
    @RequiredReadAction
    static boolean isMatchingType(CSharpLanguageVersion languageVersion, PsiElement override, PsiElement overridden) {
        DotNetTypeRef typeRef = getTypeRef(override);
        DotNetTypeRef overriddenTypeRef = getTypeRef(overridden);
        if (CSharpTypeUtil.isTypeEqual(typeRef, overriddenTypeRef)) {
            return true;
        }
        boolean readOnly = override instanceof CSharpMethodDeclaration
            || override instanceof CSharpPropertyDeclaration property && (property.getAccessors().length == 0
            || property.getAccessors().length == 1 && property.getAccessors()[0].getAccessorKind() == consulo.dotnet.psi.DotNetXAccessor.Kind.GET);
        return languageVersion.isAtLeast(CSharpLanguageVersion._9_0) && readOnly && typeRef.resolve().getElement() instanceof DotNetTypeDeclaration type && !type.isStruct()
            && CSharpTypeUtil.isInheritable(overriddenTypeRef, typeRef);
    }
}
