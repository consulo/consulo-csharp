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
import consulo.csharp.lang.impl.psi.CSharpVisibilityUtil;
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.psi.*;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.*;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Inconsistent accessibility: return type is less accessible than method - and the helpers of the other checks of a
 * type of the signature of a member, or a base type, less accessible than the member (CS0051 - CS0061, CS7025)
 *
 * @author VISTALL
 */
public class CS0050 extends CompilerCheck<DotNetLikeMethodDeclaration> {
    enum Kind {
        METHOD,
        OPERATOR,
        INDEXER,
        DELEGATE,
        FIELD,
        PROPERTY,
        EVENT,
        NONE
    }

    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetLikeMethodDeclaration element) {
        return returnType(this, element, Kind.METHOD);
    }

    @RequiredReadAction
    static @Nullable HighlightInfoFactory returnType(CompilerCheck<?> check, DotNetLikeMethodDeclaration method, Kind kind) {
        if (kind(method) != kind || !isLessVisible(method.getReturnType(), method)) {
            return null;
        }
        return check.newBuilder(range(method), formatTypeRef(method.getReturnTypeRef()), formatElement(method));
    }

    @RequiredReadAction
    static List<HighlightInfoFactory> parameterTypes(CompilerCheck<?> check, DotNetLikeMethodDeclaration method, Kind kind) {
        if (kind(method) != kind) {
            return List.of();
        }
        List<HighlightInfoFactory> results = new ArrayList<>();
        for (DotNetParameter parameter : method.getParameters()) {
            if (isLessVisible(parameter.getType(), method)) {
                results.add(check.newBuilder(range(method), formatTypeRef(parameter.toTypeRef(false)), formatElement(method)));
            }
        }
        return results;
    }

    @RequiredReadAction
    static @Nullable HighlightInfoFactory variableType(CompilerCheck<?> check, DotNetVariable variable, Kind kind) {
        if (kind(variable) != kind || !isLessVisible(variable.getType(), variable)) {
            return null;
        }
        return check.newBuilder(range(variable), formatTypeRef(variable.toTypeRef(false)), formatElement(variable));
    }

    /**
     * The base class (CS0060) or the base interfaces of an interface (CS0061) less accessible than the type
     */
    @RequiredReadAction
    static List<HighlightInfoFactory> baseTypes(CompilerCheck<?> check, CSharpTypeDeclaration type, boolean interfaces) {
        DotNetTypeList extendList = type.getExtendList();
        if (extendList == null || (interfaces ? !type.isInterface() || declared(type) == CSharpAccessModifier.PRIVATE : type.isInterface() || type.isStruct() || type.isEnum())) {
            return List.of();
        }
        List<HighlightInfoFactory> results = new ArrayList<>();
        for (DotNetType baseType : extendList.getTypes()) {
            boolean isInterface = baseType instanceof CSharpUserType userType && userType.getReferenceExpression().resolve() instanceof DotNetTypeDeclaration declaration
                && declaration.isInterface();
            if (isInterface == interfaces && isLessVisible(baseType, type)) {
                results.add(check.newBuilder(getNameIdentifier(type), formatTypeRef(baseType.toTypeRef()), formatElement(type)));
            }
        }
        return results;
    }

    /**
     * What a member is checked as - none for a private one, an explicit implementation, not a member
     */
    @RequiredReadAction
    private static Kind kind(DotNetModifierListOwner element) {
        if (!isMember(element) || declared(element) == CSharpAccessModifier.PRIVATE
            || element instanceof DotNetVirtualImplementOwner implementOwner && implementOwner.getTypeForImplement() != null) {
            return Kind.NONE;
        }
        if (element instanceof CSharpMethodDeclaration method && method.isDelegate()) {
            return Kind.DELEGATE;
        }
        if (element instanceof CSharpMethodDeclaration method && method.isOperator() || element instanceof CSharpConversionMethodDeclaration) {
            return Kind.OPERATOR;
        }
        if (element instanceof CSharpIndexMethodDeclaration) {
            return Kind.INDEXER;
        }
        if (element instanceof CSharpConstructorDeclaration || element instanceof CSharpMethodDeclaration) {
            return Kind.METHOD;
        }
        if (element instanceof CSharpPropertyDeclaration) {
            return Kind.PROPERTY;
        }
        if (element instanceof CSharpEventDeclaration) {
            return Kind.EVENT;
        }
        return element instanceof CSharpFieldDeclaration ? Kind.FIELD : Kind.NONE;
    }

    /**
     * The name; the target type of a conversion, this of an indexer
     */
    @RequiredReadAction
    private static PsiElement range(DotNetModifierListOwner element) {
        if (element instanceof CSharpConversionMethodDeclaration conversion && conversion.getReturnType() != null) {
            return conversion.getReturnType();
        }
        if (element instanceof CSharpIndexMethodDeclaration indexer) {
            return CS0111.range(indexer);
        }
        return getNameIdentifier(element);
    }

    @RequiredReadAction
    private static boolean isLessVisible(@Nullable DotNetType type, PsiElement member) {
        return type != null && findLessVisible(type, member) != null;
    }

    /**
     * A type the type is made of - itself, its type arguments, element types, the types it is nested in - less
     * accessible than the member (Roslyn's FindTypeLessVisibleThan)
     */
    @RequiredReadAction
    private static @Nullable DotNetTypeDeclaration findLessVisible(DotNetType type, PsiElement member) {
        List<CSharpUserType> userTypes = new ArrayList<>();
        if (type instanceof CSharpUserType userType) {
            userTypes.add(userType);
        }
        userTypes.addAll(PsiTreeUtil.findChildrenOfType(type, CSharpUserType.class));
        for (CSharpUserType userType : userTypes) {
            if (!(userType.getReferenceExpression().resolve() instanceof DotNetTypeDeclaration declaration)) {
                continue;
            }
            for (DotNetTypeDeclaration part = declaration; part != null; part = containingType(part)) {
                if (!isAsRestrictive(part, member)) {
                    return part;
                }
            }
        }
        return null;
    }

    /**
     * The member, and each type it is in, is not more accessible than the type (Roslyn's IsAsRestrictive)
     */
    @RequiredReadAction
    private static boolean isAsRestrictive(DotNetTypeDeclaration type, PsiElement member) {
        CSharpAccessModifier access = declared(type);
        if (access == CSharpAccessModifier.PUBLIC) {
            return true;
        }
        DotNetTypeDeclaration parent = containingType(type);
        for (PsiElement owner = member; owner != null; owner = containingType(owner)) {
            CSharpAccessModifier ownerAccess = declared((DotNetModifierListOwner) owner);
            boolean privateOrInternal = ownerAccess == CSharpAccessModifier.PRIVATE || ownerAccess == CSharpAccessModifier.INTERNAL
                || ownerAccess == CSharpAccessModifier.PRIVATE_PROTECTED;
            switch (access) {
                case INTERNAL:
                    if (privateOrInternal && CSharpVisibilityUtil.hasInternalAccess(type, owner)) {
                        return true;
                    }
                    break;
                case PRIVATE_PROTECTED:
                    if (privateOrInternal && CSharpVisibilityUtil.hasInternalAccess(type, owner) && isAsRestrictiveAsProtected(parent, owner, ownerAccess)) {
                        return true;
                    }
                    break;
                case PROTECTED:
                    if (isAsRestrictiveAsProtected(parent, owner, ownerAccess)) {
                        return true;
                    }
                    break;
                case PROTECTED_INTERNAL:
                    if (parent == null) {
                        break;
                    }
                    boolean internalAccess = CSharpVisibilityUtil.hasInternalAccess(type, owner);
                    switch (ownerAccess) {
                        case PRIVATE:
                            if (internalAccess || isWithinInheritor(parent, owner)) {
                                return true;
                            }
                            break;
                        case INTERNAL:
                            if (internalAccess) {
                                return true;
                            }
                            break;
                        case PROTECTED:
                            if (isInheritor(parent, containingType(owner))) {
                                return true;
                            }
                            break;
                        case PRIVATE_PROTECTED:
                            if (internalAccess || isInheritor(parent, containingType(owner))) {
                                return true;
                            }
                            break;
                        case PROTECTED_INTERNAL:
                            if (internalAccess && isInheritor(parent, containingType(owner))) {
                                return true;
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                case PRIVATE:
                    if (ownerAccess == CSharpAccessModifier.PRIVATE && parent != null) {
                        for (DotNetTypeDeclaration ownerParent = containingType(owner); ownerParent != null; ownerParent = containingType(ownerParent)) {
                            if (composite(ownerParent).isEquivalentTo(composite(parent))) {
                                return true;
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return false;
    }

    @RequiredReadAction
    private static boolean isAsRestrictiveAsProtected(@Nullable DotNetTypeDeclaration parent, PsiElement owner, CSharpAccessModifier ownerAccess) {
        if (parent == null) {
            return false;
        }
        if (ownerAccess == CSharpAccessModifier.PRIVATE) {
            return isWithinInheritor(parent, owner);
        }
        if (ownerAccess == CSharpAccessModifier.PROTECTED || ownerAccess == CSharpAccessModifier.PRIVATE_PROTECTED) {
            return isInheritor(parent, containingType(owner));
        }
        return false;
    }

    /**
     * The owner is in the type, or in a type derived from it
     */
    @RequiredReadAction
    private static boolean isWithinInheritor(DotNetTypeDeclaration type, PsiElement owner) {
        for (DotNetTypeDeclaration ownerParent = containingType(owner); ownerParent != null; ownerParent = containingType(ownerParent)) {
            if (isInheritor(type, ownerParent)) {
                return true;
            }
        }
        return false;
    }

    @RequiredReadAction
    private static boolean isInheritor(DotNetTypeDeclaration type, @Nullable DotNetTypeDeclaration other) {
        String vmQName = type.getVmQName();
        return other != null && vmQName != null && DotNetInheritUtil.isParentOrSelf(vmQName, composite(other), true);
    }

    /**
     * The accessibility as declared, or of no modifier: internal at a namespace, public in an interface or an enum,
     * private in a class or a struct
     */
    @RequiredReadAction
    private static CSharpAccessModifier declared(DotNetModifierListOwner owner) {
        DotNetModifierListOwner declaration = owner instanceof DotNetTypeDeclaration type ? composite(type) : owner;
        CSharpAccessModifier access = CSharpAccessModifier.findModifier(declaration);
        if (access != CSharpAccessModifier.NONE) {
            return access;
        }
        DotNetTypeDeclaration parent = containingType(owner);
        if (parent == null) {
            return CSharpAccessModifier.INTERNAL;
        }
        return parent.isInterface() || parent.isEnum() ? CSharpAccessModifier.PUBLIC : CSharpAccessModifier.PRIVATE;
    }

    @RequiredReadAction
    private static boolean isMember(PsiElement element) {
        return element.getParent() instanceof DotNetTypeDeclaration
            || element instanceof CSharpMethodDeclaration method && method.isDelegate() && containingType(element) == null;
    }

    @RequiredReadAction
    private static @Nullable DotNetTypeDeclaration containingType(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, DotNetTypeDeclaration.class, true);
    }

    @RequiredReadAction
    private static DotNetTypeDeclaration composite(DotNetTypeDeclaration type) {
        return CSharpCompositeTypeDeclaration.selectCompositeOrSelfType(type);
    }
}
