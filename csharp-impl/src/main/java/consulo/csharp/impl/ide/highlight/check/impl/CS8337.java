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
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * {@code this ref string s} - the this parameter of a ref extension method must be a value type
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS8337 extends CompilerCheck<CSharpMethodDeclaration> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpMethodDeclaration element) {
        DotNetParameter[] parameters = element.getParameters();
        if (!element.isExtension() || parameters.length == 0 || !parameters[0].hasModifier(CSharpModifier.REF)) {
            return null;
        }
        PsiElement type = parameters[0].toTypeRef(false).resolve().getElement();
        if (type instanceof DotNetTypeDeclaration declaration && (declaration.isStruct() || declaration.isEnum())) {
            return null;
        }
        // a type parameter - CS8337 unless constrained to struct, not looked at here
        if (type instanceof DotNetGenericParameter || type == null) {
            return null;
        }
        return newBuilder(getNameIdentifier(element), element.getName());
    }
}
