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
import consulo.application.progress.ProgressManager;
import consulo.application.util.CachedValueProvider;
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.impl.psi.CSharpElementCompareUtil;
import consulo.csharp.lang.impl.psi.source.resolve.overrideSystem.OverrideUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.psi.CSharpCodeBodyProxy;
import consulo.csharp.lang.psi.CSharpEventDeclaration;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.CSharpXAccessorOwner;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.dotnet.psi.DotNetXAccessor;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.util.LanguageCachedValueUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The interface members a class or a struct does not implement - CS0535, CS0736, CS0737, CS0738 at the base type that brings
 * the interface, Roslyn SourceMemberContainerSymbol interface implementation checks
 *
 * @author VISTALL
 * @since 2026-11-14
 */
class InterfaceImplementationUtil {
    /**
     * @param baseType  the type in the base list - the interface or an interface deriving from the one of the member
     * @param member    the interface member - with the type arguments of the base type
     * @param candidate the member of the class with the signature of the interface member which can not implement it
     */
    record Problem(String code, DotNetType baseType, DotNetNamedElement member, @Nullable DotNetNamedElement candidate) {
    }

    @RequiredReadAction
    static List<Problem> getProblems(CSharpTypeDeclaration type) {
        return LanguageCachedValueUtil.getCachedValue(type, () -> CachedValueProvider.Result.create(analyze(type), PsiModificationTracker.MODIFICATION_COUNT));
    }

    @RequiredReadAction
    private static List<Problem> analyze(CSharpTypeDeclaration type) {
        DotNetTypeList extendList = type.getExtendList();
        if (type.isInterface() || extendList == null) {
            return List.of();
        }

        DotNetTypeDeclaration composite = CSharpCompositeTypeDeclaration.selectCompositeOrSelfType(type);

        // the members of the class and its base classes - the overridden ones are merged into the overriding
        List<DotNetNamedElement> classMembers = new ArrayList<>();
        for (PsiElement member : OverrideUtil.getAllMembers(composite, composite.getResolveScope(), DotNetGenericExtractor.EMPTY, false, false)) {
            if (member instanceof DotNetNamedElement named && member.getParent() instanceof DotNetTypeDeclaration owner && !owner.isInterface()) {
                classMembers.add(named);
            }
        }

        List<DotNetVirtualImplementOwner> explicitImplementations = new ArrayList<>();
        for (DotNetNamedElement member : composite.getMembers()) {
            if (member instanceof DotNetVirtualImplementOwner owner && owner.getTypeForImplement() != null) {
                explicitImplementations.add(owner);
            }
        }

        List<Problem> problems = new ArrayList<>();
        Set<PsiElement> visitedMembers = new HashSet<>();
        for (DotNetType baseType : extendList.getTypes()) {
            ProgressManager.checkCanceled();

            List<RequiredMember> required = new ArrayList<>();
            collectRequired(baseType.toTypeRef(), required, new HashSet<>());
            for (RequiredMember requiredMember : required) {
                if (!visitedMembers.add(requiredMember.original())) {
                    continue;
                }
                Problem problem = check(baseType, requiredMember, classMembers, explicitImplementations);
                if (problem != null) {
                    problems.add(problem);
                }
            }
        }
        return problems;
    }

    private record RequiredMember(DotNetNamedElement member, DotNetNamedElement original, String interfaceVmQName) {
    }

    @RequiredReadAction
    private static void collectRequired(DotNetTypeRef interfaceTypeRef, List<RequiredMember> required, Set<String> visitedInterfaces) {
        DotNetTypeResolveResult resolveResult = interfaceTypeRef.resolve();
        if (!(resolveResult.getElement() instanceof DotNetTypeDeclaration iface) || !iface.isInterface() || !visitedInterfaces.add(iface.getVmQName())) {
            return;
        }
        DotNetGenericExtractor extractor = resolveResult.getGenericExtractor();
        for (DotNetNamedElement member : iface.getMembers()) {
            if (requiresImplementation(member)) {
                required.add(new RequiredMember(GenericUnwrapTool.extract(member, extractor), member, iface.getVmQName()));
            }
        }
        for (DotNetTypeRef baseInterface : iface.getExtendTypeRefs()) {
            collectRequired(GenericUnwrapTool.exchangeTypeRef(baseInterface, extractor), required, visitedInterfaces);
        }
    }

    /**
     * @return true for an abstract interface member - not a default implementation, a static member with a body or a nested type
     */
    @RequiredReadAction
    private static boolean requiresImplementation(DotNetNamedElement member) {
        if (!(member instanceof CSharpMethodDeclaration || member instanceof CSharpPropertyDeclaration || member instanceof CSharpIndexMethodDeclaration
            || member instanceof CSharpEventDeclaration)) {
            return false;
        }
        if (member instanceof CSharpMethodDeclaration method && method.isDelegate()) {
            return false;
        }
        DotNetModifierListOwner owner = (DotNetModifierListOwner) member;
        if (owner.hasModifier(CSharpModifier.PRIVATE) || owner.hasModifier(CSharpModifier.SEALED)) {
            return false;
        }
        // a library member - by its metadata
        if (!(member.getContainingFile() instanceof CSharpFile)) {
            return owner.hasModifier(CSharpModifier.ABSTRACT);
        }
        // static abstract void S(); - required, static void S() { } - a member of the interface; by the body as the other ones
        if (member instanceof CSharpMethodDeclaration method) {
            return isEmpty(method.getCodeBlock());
        }
        // int P => 0; - an expression body
        if (member.getNode().findChildByType(CSharpTokens.DARROW) != null) {
            return false;
        }
        // int this[int i] => 0; - the body of an indexer with no accessors
        if (member instanceof CSharpIndexMethodDeclaration indexer && indexer.getAccessors().length == 0 && !isEmpty(indexer.getCodeBlock())) {
            return false;
        }
        if (member instanceof CSharpXAccessorOwner accessorOwner) {
            for (DotNetXAccessor accessor : accessorOwner.getAccessors()) {
                if (accessor.getCodeBlock() instanceof CSharpCodeBodyProxy body && !body.isSemicolonOrEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isEmpty(@Nullable CSharpCodeBodyProxy body) {
        return body == null || body.isSemicolonOrEmpty();
    }

    @Nullable
    @RequiredReadAction
    private static Problem check(DotNetType baseType, RequiredMember required, List<DotNetNamedElement> classMembers,
                                 List<DotNetVirtualImplementOwner> explicitImplementations) {
        DotNetNamedElement member = required.member();
        for (DotNetVirtualImplementOwner explicit : explicitImplementations) {
            DotNetType typeForImplement = explicit.getTypeForImplement();
            if (typeForImplement != null && typeForImplement.toTypeRef().resolve().getElement() instanceof DotNetTypeDeclaration iface
                && Objects.equals(iface.getVmQName(), required.interfaceVmQName()) && CSharpElementCompareUtil.isEqual(explicit, member, 0)) {
                return null;
            }
        }

        DotNetNamedElement candidate = null;
        for (DotNetNamedElement classMember : classMembers) {
            // the interface member itself, as a member of the class
            if (classMember.isEquivalentTo(required.original()) || classMember.getNavigationElement() == required.original()) {
                continue;
            }
            if (Objects.equals(classMember.getName(), member.getName()) && CSharpElementCompareUtil.isEqual(classMember, member, 0)) {
                candidate = classMember;
                break;
            }
        }
        if (candidate == null) {
            return new Problem("CS0535", baseType, member, null);
        }

        DotNetModifierListOwner candidateOwner = (DotNetModifierListOwner) candidate;
        boolean staticMember = ((DotNetModifierListOwner) member).hasModifier(CSharpModifier.STATIC);
        if (!staticMember && candidateOwner.hasModifier(CSharpModifier.STATIC)) {
            return new Problem("CS0736", baseType, member, candidate);
        }
        if (!candidateOwner.hasModifier(CSharpModifier.PUBLIC)) {
            return new Problem("CS0737", baseType, member, candidate);
        }
        if (!CSharpElementCompareUtil.isEqual(candidate, member, CSharpElementCompareUtil.CHECK_RETURN_TYPE)) {
            return new Problem("CS0738", baseType, member, candidate);
        }
        return null;
    }

    /**
     * @return the return type of an interface member - for CS0738
     */
    @RequiredReadAction
    static DotNetTypeRef getTypeRef(DotNetNamedElement member) {
        if (member instanceof DotNetLikeMethodDeclaration method) {
            return method.getReturnTypeRef();
        }
        return member instanceof DotNetVariable variable ? variable.toTypeRef(false) : DotNetTypeRef.ERROR_TYPE;
    }

    static Collection<Problem> ofCode(CSharpTypeDeclaration type, String code) {
        List<Problem> result = new ArrayList<>();
        for (Problem problem : getProblems(type)) {
            if (problem.code().equals(code)) {
                result.add(problem);
            }
        }
        return result;
    }
}
