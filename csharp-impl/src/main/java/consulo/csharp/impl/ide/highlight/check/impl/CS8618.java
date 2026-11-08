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
import consulo.csharp.impl.ide.highlight.check.CSharpNullableContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.controlFlow.CSharpControlFlow;
import consulo.csharp.lang.impl.psi.controlFlow.CSharpControlFlowBuilder;
import consulo.csharp.lang.impl.psi.controlFlow.CSharpDefiniteAssignment;
import consulo.csharp.lang.impl.psi.CSharpGenericConstraintUtil;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpGenericConstraintKeywordValue;
import consulo.csharp.lang.psi.CSharpGenericConstraintValue;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpNullableType;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.module.CSharpNullableOption;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpSimpleModuleExtension;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetEventDeclaration;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Non-nullable field must contain a non-null value when exiting constructor - under nullable enable, a field, an
 * auto-property or a field-like event of a reference type or a type parameter, without an initializer: at each
 * constructor not chained to another one which does not assign it on every path (a method with [MemberNotNull] called
 * assigns), or at the member if the type has no constructor
 *
 * @author VISTALL
 */
public class CS8618 extends CompilerCheck<DotNetModifierListOwner> {
    @RequiredReadAction
    @Override
    public List<? extends HighlightInfoFactory> check(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetModifierListOwner element) {
        if (element instanceof CSharpConstructorDeclaration constructor) {
            return constructor(highlightContext, constructor);
        }
        if (element instanceof DotNetVariable variable && element.getParent() instanceof DotNetTypeDeclaration type
            && CSharpControlFlowBuilder.isConstructorMember(variable, variable.hasModifier(CSharpModifier.STATIC))
            && !type.hasModifier(CSharpModifier.PARTIAL)
            && !hasConstructor(type, variable.hasModifier(CSharpModifier.STATIC))
            && isNonNullable(highlightContext, variable)) {
            return List.of(newBuilder(getNameIdentifier(variable), kind(variable), variable.getName()));
        }
        return List.of();
    }

    @RequiredReadAction
    private List<HighlightInfoFactory> constructor(CSharpHighlightContext highlightContext, CSharpConstructorDeclaration constructor) {
        CSharpControlFlow flow = CSharpControlFlow.of(constructor);
        if (flow == null || flow.getConstructorMembers().isEmpty()) {
            return List.of();
        }
        Set<DotNetVariable> unassigned = new LinkedHashSet<>();
        for (CSharpDefiniteAssignment.Leave leave : flow.getDefiniteAssignment().getUnassignedLeaves()) {
            if (flow.getConstructorMembers().contains(leave.parameter()) && isNonNullable(highlightContext, leave.parameter())) {
                unassigned.add(leave.parameter());
            }
        }
        List<HighlightInfoFactory> results = new ArrayList<>();
        for (DotNetVariable member : unassigned) {
            results.add(newBuilder(getNameIdentifier(constructor), kind(member), member.getName()));
        }
        return results;
    }

    @RequiredReadAction
    private static boolean hasConstructor(DotNetTypeDeclaration type, boolean isStatic) {
        for (PsiElement member : type.getMembers()) {
            if (member instanceof CSharpConstructorDeclaration constructor && !constructor.isDeConstructor() && constructor.hasModifier(CSharpModifier.STATIC) == isStatic) {
                return true;
            }
        }
        return false;
    }

    /**
     * Of a reference type or a type parameter, not marked nullable, not required, in a nullable enable context
     */
    @RequiredReadAction
    private static boolean isNonNullable(CSharpHighlightContext highlightContext, DotNetVariable member) {
        CSharpSimpleModuleExtension<?> extension = highlightContext.getCSharpModuleExtension();
        CSharpNullableOption option = CSharpNullableContext.get(highlightContext.getFile())
            .getNullable(member, extension == null ? CSharpNullableOption.UNSPECIFIED : extension.getNullableOption());
        if (option != CSharpNullableOption.ENABLE || member.getType() instanceof CSharpNullableType || isRequired(member)) {
            return false;
        }
        DotNetTypeResolveResult result = member.toTypeRef(false).resolve();
        if (result.getElement() instanceof DotNetGenericParameter parameter) {
            return !isValueTypeParameter(parameter);
        }
        return result.isNullable() && !(result.getElement() instanceof DotNetTypeDeclaration type && DotNetTypes.System.Nullable$1.equals(type.getVmQName()));
    }

    /**
     * where T : struct, unmanaged
     */
    @RequiredReadAction
    private static boolean isValueTypeParameter(DotNetGenericParameter parameter) {
        CSharpGenericConstraint constraint = CSharpGenericConstraintUtil.findGenericConstraint(parameter);
        if (constraint == null) {
            return false;
        }
        for (CSharpGenericConstraintValue value : constraint.getGenericConstraintValues()) {
            if (value instanceof CSharpGenericConstraintKeywordValue keyword
                && (keyword.getKeywordElementType() == CSharpTokens.STRUCT_KEYWORD || "unmanaged".equals(keyword.getText()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * required (C# 11) - set by the object initializer
     */
    @RequiredReadAction
    private static boolean isRequired(DotNetVariable member) {
        DotNetModifierList modifierList = member.getModifierList();
        if (modifierList == null) {
            return false;
        }
        for (PsiElement child = modifierList.getFirstChild(); child != null; child = child.getNextSibling()) {
            if ("required".equals(child.getText())) {
                return true;
            }
        }
        return false;
    }

    private static String kind(DotNetVariable member) {
        return member instanceof CSharpPropertyDeclaration ? "property" : member instanceof DotNetEventDeclaration ? "event" : "field";
    }
}
