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
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.lang.impl.psi.CSharpNullableTypeUtil;
import consulo.csharp.lang.impl.psi.CSharpPropertyUtil;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * {@code struct S { S self; }} - a struct member causes a cycle in the struct layout
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0523 extends CompilerCheck<DotNetVariable> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetVariable element) {
        if (!isLayoutField(element) || !(element.getParent() instanceof DotNetTypeDeclaration struct) || !struct.isStruct()) {
            return null;
        }
        DotNetTypeDeclaration fieldType = structOf(element.toTypeRef(false));
        if (fieldType == null || !reaches(fieldType, struct.getVmQName(), new HashSet<>())) {
            return null;
        }
        return newBuilder(getNameIdentifier(element), formatElement(element), formatTypeRef(element.toTypeRef(false)));
    }

    /**
     * @return true for an instance field or auto property - a part of the layout of its struct
     */
    @RequiredReadAction
    private static boolean isLayoutField(DotNetVariable variable) {
        if (variable.hasModifier(CSharpModifier.STATIC) || variable.isConstant()) {
            return false;
        }
        return variable instanceof DotNetFieldDeclaration || variable instanceof CSharpPropertyDeclaration property && CSharpPropertyUtil.isAutoProperty(property);
    }

    /**
     * @return the struct of a type - of a nullable, its value
     */
    @Nullable
    @RequiredReadAction
    private static DotNetTypeDeclaration structOf(DotNetTypeRef typeRef) {
        DotNetTypeRef unboxed = CSharpNullableTypeUtil.unbox(typeRef);
        return unboxed.resolve().getElement() instanceof DotNetTypeDeclaration type && type.isStruct() && !type.isEnum() ? type : null;
    }

    /**
     * @return true if the layout of the struct contains the target struct
     */
    @RequiredReadAction
    private static boolean reaches(DotNetTypeDeclaration struct, String targetVmQName, Set<String> visited) {
        if (Objects.equals(struct.getVmQName(), targetVmQName)) {
            return true;
        }
        if (!visited.add(struct.getVmQName()) || visited.size() > 64) {
            return false;
        }
        for (DotNetNamedElement member : struct.getMembers()) {
            if (member instanceof DotNetVariable variable && isLayoutField(variable)) {
                DotNetTypeDeclaration memberStruct = structOf(variable.toTypeRef(false));
                if (memberStruct != null && reaches(memberStruct, targetVmQName, visited)) {
                    return true;
                }
            }
        }
        return false;
    }
}
