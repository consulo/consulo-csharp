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
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * Cannot override inherited member because it is not marked virtual, abstract, or override - see {@link CS0115}
 *
 * @author VISTALL
 */
public class CS0506 extends CompilerCheck<DotNetModifierListOwner> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetModifierListOwner element) {
        if (!element.hasModifier(CSharpModifier.OVERRIDE)) {
            return null;
        }
        PsiElement hidden = CS0108.hiddenMember(element);
        return hidden != null && !CS0108.isVirtual(hidden) ? newBuilder(getNameIdentifier(element), formatElement(element), formatElement(hidden)) : null;
    }
}
