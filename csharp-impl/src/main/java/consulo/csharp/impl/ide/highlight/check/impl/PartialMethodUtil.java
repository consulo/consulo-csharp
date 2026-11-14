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
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpGenericConstraintValue;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.util.collection.ArrayUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The parts of a partial method - the defining declaration has no body, the implementing one has; Roslyn
 * SourceOrdinaryMethodSymbol partial checks
 *
 * @author VISTALL
 * @since 2026-11-14
 */
class PartialMethodUtil {
    static final CSharpModifier[] ACCESS_MODIFIERS = {CSharpModifier.PUBLIC, CSharpModifier.PRIVATE, CSharpModifier.PROTECTED, CSharpModifier.INTERNAL};

    @RequiredReadAction
    static boolean isPartial(CSharpMethodDeclaration method) {
        return method.hasModifier(CSharpModifier.PARTIAL) && method.getParent() instanceof DotNetTypeDeclaration;
    }

    @RequiredReadAction
    static boolean isImplementing(CSharpMethodDeclaration method) {
        return method.getCodeBlock().isNotSemicolonAndNotEmpty();
    }

    /**
     * @return the parts of the same signature in the type, the partial declarations included, in the order of the type
     */
    @RequiredReadAction
    static List<CSharpMethodDeclaration> getParts(CSharpMethodDeclaration method) {
        DotNetTypeDeclaration type = CSharpCompositeTypeDeclaration.selectCompositeOrSelfType((DotNetTypeDeclaration) method.getParent());
        List<CSharpMethodDeclaration> parts = new ArrayList<>();
        for (DotNetNamedElement member : type.getMembers()) {
            if (member instanceof CSharpMethodDeclaration other && other.hasModifier(CSharpModifier.PARTIAL) && isSameSignature(method, other)) {
                parts.add(other);
            }
        }
        return parts;
    }

    /**
     * @return the other part of the pair - the first implementing for a defining one, the first defining for an implementing one
     */
    @Nullable
    @RequiredReadAction
    static CSharpMethodDeclaration getOtherPart(CSharpMethodDeclaration method) {
        boolean implementing = isImplementing(method);
        for (CSharpMethodDeclaration part : getParts(method)) {
            if (part != method && isImplementing(part) != implementing) {
                return part;
            }
        }
        return null;
    }

    @RequiredReadAction
    static boolean isSameSignature(CSharpMethodDeclaration a, CSharpMethodDeclaration b) {
        if (a == b) {
            return true;
        }
        if (!Objects.equals(a.getName(), b.getName()) || a.getGenericParametersCount() != b.getGenericParametersCount()) {
            return false;
        }
        DotNetParameter[] aParameters = a.getParameters();
        DotNetParameter[] bParameters = b.getParameters();
        if (aParameters.length != bParameters.length) {
            return false;
        }
        for (int i = 0; i < aParameters.length; i++) {
            if (refKind(aParameters[i]) != refKind(bParameters[i]) || !isSameType(aParameters[i].toTypeRef(false), a, bParameters[i].toTypeRef(false), b)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the same type - a type parameter of the method by its index, F<T>(T t) of both parts
     */
    @RequiredReadAction
    static boolean isSameType(DotNetTypeRef aTypeRef, CSharpMethodDeclaration a, DotNetTypeRef bTypeRef, CSharpMethodDeclaration b) {
        if (aTypeRef.resolve().getElement() instanceof DotNetGenericParameter aParameter && bTypeRef.resolve().getElement() instanceof DotNetGenericParameter bParameter) {
            int aIndex = ArrayUtil.indexOf(a.getGenericParameters(), aParameter);
            int bIndex = ArrayUtil.indexOf(b.getGenericParameters(), bParameter);
            if (aIndex >= 0 || bIndex >= 0) {
                return aIndex == bIndex;
            }
        }
        return CSharpTypeUtil.isTypeEqual(aTypeRef, bTypeRef);
    }

    @Nullable
    @RequiredReadAction
    private static CSharpModifier refKind(DotNetParameter parameter) {
        for (CSharpModifier modifier : new CSharpModifier[]{CSharpModifier.REF, CSharpModifier.OUT, CSharpModifier.IN}) {
            if (parameter.hasModifier(modifier)) {
                return modifier;
            }
        }
        return null;
    }

    /**
     * @return the accessibility written in the source - none for a private partial method of C# 2
     */
    @RequiredReadAction
    static List<CSharpModifier> getExplicitAccess(CSharpMethodDeclaration method) {
        List<CSharpModifier> result = new ArrayList<>();
        DotNetModifierList modifierList = method.getModifierList();
        if (modifierList != null) {
            for (CSharpModifier modifier : ACCESS_MODIFIERS) {
                if (modifierList.getModifierElement(modifier) != null) {
                    result.add(modifier);
                }
            }
        }
        return result;
    }

    @RequiredReadAction
    static boolean hasExplicitModifier(CSharpMethodDeclaration method, DotNetModifier modifier) {
        DotNetModifierList modifierList = method.getModifierList();
        return modifierList != null && modifierList.getModifierElement(modifier) != null;
    }

    /**
     * @return the constraints of a type parameter as text - the parts must have the same
     */
    @RequiredReadAction
    static String constraintsText(CSharpMethodDeclaration method, int index) {
        DotNetGenericParameter parameter = method.getGenericParameters()[index];
        StringBuilder builder = new StringBuilder();
        for (CSharpGenericConstraint constraint : method.getGenericConstraints()) {
            if (constraint.resolve() == parameter) {
                for (CSharpGenericConstraintValue value : constraint.getGenericConstraintValues()) {
                    builder.append(value.getText().replace(" ", "")).append(',');
                }
            }
        }
        return builder.toString();
    }
}
