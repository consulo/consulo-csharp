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
import consulo.csharp.lang.impl.psi.CSharpElements;
import consulo.csharp.lang.impl.psi.CSharpGenericConstraintUtil;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpGenericExtractor;
import consulo.csharp.lang.impl.psi.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpGenericConstraintKeywordValue;
import consulo.csharp.lang.psi.CSharpGenericConstraintOwner;
import consulo.csharp.lang.psi.CSharpGenericConstraintTypeValue;
import consulo.csharp.lang.psi.CSharpGenericConstraintValue;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.DotNetTypes;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.dotnet.psi.DotNetEventDeclaration;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetPropertyDeclaration;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.util.collection.ArrayUtil;
import org.jspecify.annotations.Nullable;

/**
 * A type argument not satisfying the constraints of its type parameter - CS0310, CS0311, CS0314, CS0315, CS0452, CS0453; the first
 * violated constraint in the order of Roslyn ConstraintsHelper: class / struct, the types, new()
 *
 * @author VISTALL
 * @since 2026-11-14
 */
class ConstraintCheckUtil {
    record Violation(String code, DotNetGenericParameter parameter, PsiElement owner, @Nullable DotNetTypeRef constraintTypeRef) {
    }

    private static final Violation NONE = new Violation("", null, null, null);

    @Nullable
    @RequiredReadAction
    static Violation getViolation(DotNetType argument) {
        Violation violation = LanguageCachedValueUtil.getCachedValue(argument, () -> CachedValueProvider.Result.create(compute(argument), PsiModificationTracker.MODIFICATION_COUNT));
        return violation == NONE ? null : violation;
    }

    @RequiredReadAction
    private static Violation compute(DotNetType argument) {
        if (!(argument.getParent() instanceof DotNetTypeList list) || !(list.getParent() instanceof CSharpReferenceExpression reference)) {
            return NONE;
        }
        IElementType listType = list.getNode().getElementType();
        if (listType != CSharpElements.TYPE_ARGUMENTS && listType != CSharpStubElements.TYPE_ARGUMENTS) {
            return NONE;
        }
        PsiElement owner = reference.resolve();
        if (owner instanceof CSharpConstructorDeclaration constructor) {
            owner = constructor.getParent();
        }
        // M<string>() - the method with the type arguments substituted, the constraints are of the declaration
        if (owner != null && owner.getOriginalElement() != owner && owner.getOriginalElement() instanceof CSharpGenericConstraintOwner) {
            owner = owner.getOriginalElement();
        }
        if (!(owner instanceof CSharpGenericConstraintOwner constraintOwner) || !(owner instanceof DotNetGenericParameterListOwner parameterOwner)) {
            return NONE;
        }
        DotNetGenericParameter[] parameters = parameterOwner.getGenericParameters();
        int index = ArrayUtil.indexOf(list.getTypes(), argument);
        if (index < 0 || index >= parameters.length) {
            return NONE;
        }
        DotNetGenericParameter parameter = parameters[index];
        CSharpGenericConstraint constraint = CSharpGenericConstraintUtil.forParameter(constraintOwner, parameter);
        if (constraint == null) {
            return NONE;
        }

        DotNetTypeRef argumentTypeRef = argument.toTypeRef();
        PsiElement argumentElement = argumentTypeRef.resolve().getElement();
        if (argumentElement == null) {
            return NONE;
        }
        DotNetGenericExtractor extractor = CSharpGenericExtractor.create(parameters, reference.getTypeArgumentListRefs());

        CSharpGenericConstraintValue[] values = constraint.getGenericConstraintValues();
        for (CSharpGenericConstraintValue value : values) {
            if (value instanceof CSharpGenericConstraintKeywordValue keyword) {
                IElementType keywordType = keyword.getKeywordElementType();
                if (keywordType == CSharpTokens.CLASS_KEYWORD && !isReferenceType(argumentElement)) {
                    return new Violation("CS0452", parameter, owner, null);
                }
                if (keywordType == CSharpTokens.STRUCT_KEYWORD && !isNonNullableValueType(argumentElement)) {
                    return new Violation("CS0453", parameter, owner, null);
                }
            }
        }
        for (CSharpGenericConstraintValue value : values) {
            if (value instanceof CSharpGenericConstraintTypeValue typeValue) {
                DotNetTypeRef constraintTypeRef = GenericUnwrapTool.exchangeTypeRef(typeValue.toTypeRef(), extractor);
                boolean satisfied = argumentElement instanceof DotNetGenericParameter argumentParameter
                    ? isSatisfiedByParameter(constraintTypeRef, argumentParameter, 0)
                    : CSharpTypeUtil.isInheritable(constraintTypeRef, argumentTypeRef);
                if (!satisfied) {
                    String code = argumentElement instanceof DotNetGenericParameter ? "CS0314" : isValueType(argumentElement) ? "CS0315" : "CS0311";
                    return new Violation(code, parameter, owner, constraintTypeRef);
                }
            }
        }
        for (CSharpGenericConstraintValue value : values) {
            if (value instanceof CSharpGenericConstraintKeywordValue keyword && keyword.getKeywordElementType() == CSharpTokens.NEW_KEYWORD && !hasDefaultConstructor(argumentElement)) {
                return new Violation("CS0310", parameter, owner, null);
            }
        }
        return NONE;
    }

    /**
     * @return true for a type parameter which converts to the constraint type - by its own constraint types, a type parameter
     * without them converts to object only
     */
    @RequiredReadAction
    private static boolean isSatisfiedByParameter(DotNetTypeRef constraintTypeRef, DotNetGenericParameter parameter, int depth) {
        PsiElement constraintElement = constraintTypeRef.resolve().getElement();
        if (constraintElement == parameter || constraintElement instanceof DotNetTypeDeclaration type && DotNetTypes.System.Object.equals(type.getVmQName())) {
            return true;
        }
        CSharpGenericConstraint constraint = CSharpGenericConstraintUtil.findGenericConstraint(parameter);
        if (constraint == null || depth > 8) {
            return false;
        }
        for (CSharpGenericConstraintValue value : constraint.getGenericConstraintValues()) {
            if (!(value instanceof CSharpGenericConstraintTypeValue typeValue)) {
                continue;
            }
            DotNetTypeRef typeRef = typeValue.toTypeRef();
            if (typeRef.resolve().getElement() instanceof DotNetGenericParameter other ? isSatisfiedByParameter(constraintTypeRef, other, depth + 1)
                : CSharpTypeUtil.isInheritable(constraintTypeRef, typeRef)) {
                return true;
            }
        }
        return false;
    }

    @RequiredReadAction
    private static boolean isValueType(PsiElement element) {
        return element instanceof DotNetTypeDeclaration type && (type.isStruct() || type.isEnum());
    }

    @RequiredReadAction
    private static boolean isReferenceType(PsiElement element) {
        if (element instanceof DotNetGenericParameter parameter) {
            // a type parameter with class or with a class type constraint
            CSharpGenericConstraint constraint = CSharpGenericConstraintUtil.findGenericConstraint(parameter);
            if (constraint == null) {
                return false;
            }
            for (CSharpGenericConstraintValue value : constraint.getGenericConstraintValues()) {
                if (value instanceof CSharpGenericConstraintKeywordValue keyword && keyword.getKeywordElementType() == CSharpTokens.CLASS_KEYWORD) {
                    return true;
                }
                if (value instanceof CSharpGenericConstraintTypeValue typeValue && typeValue.toTypeRef().resolve().getElement() instanceof DotNetTypeDeclaration type
                    && !type.isInterface() && !type.isStruct()) {
                    return true;
                }
            }
            return false;
        }
        return !isValueType(element);
    }

    @RequiredReadAction
    private static boolean isNonNullableValueType(PsiElement element) {
        if (element instanceof DotNetGenericParameter parameter) {
            return hasKeyword(parameter, CSharpTokens.STRUCT_KEYWORD);
        }
        return isValueType(element) && !DotNetTypes.System.Nullable$1.equals(((DotNetTypeDeclaration) element).getVmQName());
    }

    @RequiredReadAction
    private static boolean hasKeyword(DotNetGenericParameter parameter, IElementType keywordType) {
        CSharpGenericConstraint constraint = CSharpGenericConstraintUtil.findGenericConstraint(parameter);
        if (constraint == null) {
            return false;
        }
        for (CSharpGenericConstraintValue value : constraint.getGenericConstraintValues()) {
            if (value instanceof CSharpGenericConstraintKeywordValue keyword && keyword.getKeywordElementType() == keywordType) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true for new T() - a struct, a non-abstract class with a public parameterless constructor, a type parameter with new() or struct
     */
    @RequiredReadAction
    private static boolean hasDefaultConstructor(PsiElement element) {
        if (element instanceof DotNetGenericParameter parameter) {
            return hasKeyword(parameter, CSharpTokens.NEW_KEYWORD) || hasKeyword(parameter, CSharpTokens.STRUCT_KEYWORD);
        }
        if (!(element instanceof DotNetTypeDeclaration type) || type.isInterface() || type.hasModifier(CSharpModifier.ABSTRACT)) {
            return false;
        }
        if (type.isStruct() || type.isEnum()) {
            return true;
        }
        boolean anyConstructor = false;
        for (DotNetNamedElement member : type.getMembers()) {
            if (member instanceof CSharpConstructorDeclaration constructor && !constructor.isDeConstructor() && !constructor.hasModifier(CSharpModifier.STATIC)) {
                anyConstructor = true;
                if (constructor.getParameters().length == 0 && constructor.hasModifier(CSharpModifier.PUBLIC)) {
                    return true;
                }
            }
        }
        return !anyConstructor;
    }

    /**
     * @return the range of a violation as Roslyn - the name of the declaration of a field, property, method, parameter or base
     * list type, the name of a generic method called, else the type argument
     */
    @RequiredReadAction
    static PsiElement getLocation(DotNetType argument, Violation violation) {
        DotNetTypeList list = (DotNetTypeList) argument.getParent();
        CSharpReferenceExpression reference = (CSharpReferenceExpression) list.getParent();
        if (violation.owner() instanceof DotNetLikeMethodDeclaration) {
            PsiElement name = reference.getReferenceElement();
            return name == null ? argument : name;
        }

        PsiElement element = argument;
        while (element.getParent() instanceof DotNetType || element.getParent() instanceof CSharpReferenceExpression
            || element.getParent() instanceof DotNetTypeList list2 && isTypeArguments(list2)) {
            element = element.getParent();
        }
        PsiElement declaration = element.getParent();
        if (declaration instanceof DotNetTypeList extendList && extendList.getParent() instanceof DotNetTypeDeclaration type) {
            return CompilerCheck.getNameIdentifier(type);
        }
        if (declaration instanceof DotNetFieldDeclaration || declaration instanceof DotNetPropertyDeclaration || declaration instanceof DotNetEventDeclaration
            || declaration instanceof DotNetLikeMethodDeclaration || declaration instanceof DotNetParameter) {
            return CompilerCheck.getNameIdentifier(declaration);
        }
        return argument;
    }

    private static boolean isTypeArguments(DotNetTypeList list) {
        IElementType listType = list.getNode().getElementType();
        return listType == CSharpElements.TYPE_ARGUMENTS || listType == CSharpStubElements.TYPE_ARGUMENTS;
    }
}
