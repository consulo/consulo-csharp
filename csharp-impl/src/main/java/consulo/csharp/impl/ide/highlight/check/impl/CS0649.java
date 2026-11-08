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
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpArrayTypeRef;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Field is never assigned to, and will always have its default value - a private field used, never written; not one
 * Unity's [SerializeField], set by its serializer
 *
 * @author VISTALL
 */
public class CS0649 extends CompilerCheck<CSharpFieldDeclaration> {
    private static final Set<String> ourZero = Set.of(DotNetTypes.System.Int32, DotNetTypes.System.Int64, DotNetTypes.System.Int16,
        DotNetTypes.System.Byte, DotNetTypes.System.SByte, DotNetTypes.System.UInt32, DotNetTypes.System.UInt64, DotNetTypes.System.UInt16,
        DotNetTypes.System.Single, DotNetTypes.System.Double, DotNetTypes.System.Decimal);

    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpFieldDeclaration element) {
        CS0414.Usage usage = CS0414.usage(element);
        if (usage == null || !usage.referenced() || usage.written() || CS0414.isSerialized(element)) {
            return null;
        }
        return newBuilder(element.getNameIdentifier(), formatElement(element), defaultValue(element.toTypeRef(false)));
    }

    /**
     * As Roslyn writes it - 0, false, null, nothing for a struct, an enum, a char
     */
    @RequiredReadAction
    private static String defaultValue(DotNetTypeRef typeRef) {
        if (typeRef instanceof CSharpArrayTypeRef) {
            return "null";
        }
        if (!(typeRef.resolve().getElement() instanceof DotNetTypeDeclaration type)) {
            return "";
        }
        String vmQName = type.getVmQName();
        if (ourZero.contains(vmQName)) {
            return "0";
        }
        if (DotNetTypes.System.Boolean.equals(vmQName)) {
            return "false";
        }
        return type.isStruct() || type.isEnum() ? "" : "null";
    }
}
