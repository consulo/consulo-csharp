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
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpArrayTypeRef;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * {@code params int a} - the params parameter must have a valid collection type
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0225 extends CompilerCheck<DotNetParameter> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetParameter element) {
        DotNetModifierList modifierList = element.getModifierList();
        PsiElement params = modifierList == null ? null : modifierList.getModifierElement(CSharpModifier.PARAMS);
        if (params == null) {
            return null;
        }
        DotNetTypeRef typeRef = element.toTypeRef(false);
        if (typeRef instanceof CSharpArrayTypeRef array) {
            // int[,] - a single-dimensional array only
            return array.getDimensions() == 0 ? null : newBuilder(params);
        }
        // C# 13 params collections - Span, ReadOnlySpan, a collection type, an interface of them; a struct or an enum of no collection is none
        if (typeRef.resolve().getElement() instanceof DotNetTypeDeclaration type && (type.isEnum() || type.isStruct() && !type.getVmQName().startsWith("System.Span")
            && !type.getVmQName().startsWith("System.ReadOnlySpan") && !type.getVmQName().startsWith("System.Collections.Immutable"))) {
            return newBuilder(params);
        }
        return null;
    }
}
