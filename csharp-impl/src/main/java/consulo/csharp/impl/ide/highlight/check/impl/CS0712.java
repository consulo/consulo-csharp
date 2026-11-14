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
import consulo.csharp.lang.psi.CSharpNewExpression;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * {@code new S()} - cannot create an instance of a static class
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0712 extends CompilerCheck<CSharpNewExpression> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpNewExpression element) {
        // new S[0] is of an array type - CS0719
        PsiElement resolved = element.toTypeRef(false).resolve().getElement();
        if (!(resolved instanceof DotNetTypeDeclaration typeDeclaration) || typeDeclaration.isInterface() || !typeDeclaration.hasModifier(DotNetModifier.STATIC)) {
            return null;
        }
        return newBuilder(element.getFirstChild(), formatElement(typeDeclaration));
    }
}
