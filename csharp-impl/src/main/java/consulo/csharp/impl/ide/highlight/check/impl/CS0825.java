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
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpUserType;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetParameter;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * {@code var f = 1;} of a field - var may only appear within a local variable declaration
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0825 extends CompilerCheck<CSharpUserType> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpUserType element) {
        // a type named var is a type
        if (!"var".equals(element.getText()) || element.getReferenceExpression().resolve() != null) {
            return null;
        }
        PsiElement parent = element.getParent();
        if (!(parent instanceof DotNetFieldDeclaration || parent instanceof CSharpPropertyDeclaration || parent instanceof DotNetLikeMethodDeclaration || parent instanceof DotNetParameter)) {
            return null;
        }
        return newBuilder(element);
    }
}
