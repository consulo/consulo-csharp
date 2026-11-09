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
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Partial declarations must be all classes, all structs, or all interfaces - a declaration of another kind than the
 * first; see {@link CS0260}
 *
 * @author VISTALL
 */
public class CS0261 extends CompilerCheck<CSharpTypeDeclaration> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpTypeDeclaration element) {
        List<CSharpTypeDeclaration> parts = CS0260.parts(element);
        if (parts == null || kind(parts.get(0)) == kind(element)) {
            return null;
        }
        return newBuilder(getNameIdentifier(element), formatElement(element));
    }

    @RequiredReadAction
    private static int kind(CSharpTypeDeclaration type) {
        return type.isInterface() ? 2 : type.isStruct() ? 1 : 0;
    }
}
