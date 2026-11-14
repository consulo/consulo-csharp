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
import consulo.dotnet.psi.DotNetModifierListOwner;
import org.jspecify.annotations.Nullable;

/**
 * {@code override long M()} of {@code virtual int M()} - the return type must match the overridden member
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0508 extends CompilerCheck<CSharpMethodDeclaration> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpMethodDeclaration element) {
        DotNetModifierListOwner overridden = OverrideCheckUtil.getOverridden(element);
        if (overridden == null || OverrideCheckUtil.isMatchingType(languageVersion, element, overridden)) {
            return null;
        }
        return newBuilder(getNameIdentifier(element), formatElement(element), formatTypeRef(OverrideCheckUtil.getTypeRef(overridden)), formatElement(overridden));
    }
}
