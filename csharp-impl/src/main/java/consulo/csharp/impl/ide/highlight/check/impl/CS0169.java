/*
 * Copyright 2013-2017 consulo.io
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


import consulo.language.psi.PsiElement;
import consulo.language.psi.search.ReferencesSearch;
import consulo.application.util.query.Query;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetAttributeUtil;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.psi.PsiReference;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 06-Nov-17
 */
public class CS0169 extends CompilerCheck<CSharpFieldDeclaration> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpFieldDeclaration element) {
        // with an initializer - assigned, CS0414
        if (!element.hasModifier(CSharpModifier.PRIVATE) || element.getInitializer() != null) {
            return null;
        }

        PsiElement parent = element.getParent();
        // [StructLayout] - its fields are its layout
        if (!(parent instanceof CSharpTypeDeclaration) || DotNetAttributeUtil.hasAttribute(parent, "System.Runtime.InteropServices.StructLayoutAttribute")) {
            return null;
        }

        PsiElement nameIdentifier = element.getNameIdentifier();
        if (nameIdentifier == null) {
            return null;
        }

        Query<PsiReference> search = ReferencesSearch.search(element, CSharpCompositeTypeDeclaration.createLocalScope((DotNetTypeDeclaration) parent));
        if (search.findFirst() == null) {
            return newBuilder(nameIdentifier, formatElement(element));
        }

        return null;
    }
}
