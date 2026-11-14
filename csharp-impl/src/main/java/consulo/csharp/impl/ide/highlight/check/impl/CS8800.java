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
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.lang.psi.CSharpModifier;
import org.jspecify.annotations.Nullable;

/**
 * {@code virtual} in one part only - both parts must have identical virtual, override, sealed and new modifiers
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS8800 extends CompilerCheck<CSharpMethodDeclaration> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpMethodDeclaration element) {
        if (!PartialMethodUtil.isPartial(element)) {
            return null;
        }
        CSharpMethodDeclaration other = PartialMethodUtil.isImplementing(element) ? PartialMethodUtil.getOtherPart(element) : null;
        if (other == null) {
            return null;
        }
        for (CSharpModifier modifier : new CSharpModifier[]{CSharpModifier.VIRTUAL, CSharpModifier.OVERRIDE, CSharpModifier.SEALED, CSharpModifier.NEW}) {
            if (PartialMethodUtil.hasExplicitModifier(element, modifier) != PartialMethodUtil.hasExplicitModifier(other, modifier)) {
                return newBuilder(getNameIdentifier(element));
            }
        }
        return null;
    }
}
