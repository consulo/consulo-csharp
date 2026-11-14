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
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetPointerType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * A pointer to a managed type - CS0208, CS8500
 *
 * @author VISTALL
 * @since 2026-11-14
 */
class ManagedTypeUtil {
    /**
     * @return the managed type a pointer type points to, null for an unmanaged one
     */
    @Nullable
    @RequiredReadAction
    static DotNetTypeRef getManagedTarget(DotNetPointerType pointer) {
        if (pointer.getInnerType() == null) {
            return null;
        }
        DotNetTypeRef inner = pointer.getInnerType().toTypeRef();
        return isManaged(inner, new HashSet<>()) ? inner : null;
    }

    @RequiredReadAction
    private static boolean isManaged(DotNetTypeRef typeRef, Set<String> visited) {
        PsiElement element = typeRef.resolve().getElement();
        // void*, T* - a type parameter is not looked at
        if (element == null || element instanceof DotNetGenericParameter || !(element instanceof DotNetTypeDeclaration type)) {
            return false;
        }
        if (type.isEnum() || "System.Void".equals(type.getVmQName())) {
            return false;
        }
        if (!type.isStruct()) {
            return true;
        }
        if (!visited.add(type.getVmQName())) {
            return false;
        }
        for (DotNetNamedElement member : type.getMembers()) {
            if (member instanceof DotNetFieldDeclaration field && !field.hasModifier(CSharpModifier.STATIC) && !field.isConstant()
                && isManaged(field.toTypeRef(false), visited)) {
                return true;
            }
        }
        return false;
    }
}
