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
import consulo.csharp.lang.psi.CSharpAccessModifier;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * Cannot change access modifiers when overriding inherited member - an override of another access than the member it
 * overrides; protected for protected internal of another assembly; see {@link CS0115}
 *
 * @author VISTALL
 */
public class CS0507 extends CompilerCheck<DotNetModifierListOwner> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetModifierListOwner element) {
        if (!element.hasModifier(CSharpModifier.OVERRIDE) || !(CS0108.hiddenMember(element) instanceof DotNetModifierListOwner hidden) || !CS0108.isVirtual(hidden)) {
            return null;
        }
        CSharpAccessModifier access = CSharpAccessModifier.findModifierOrDefault(element);
        CSharpAccessModifier hiddenAccess = CSharpAccessModifier.findModifierOrDefault(hidden);
        if (access == hiddenAccess || !((PsiElement) hidden).isPhysical() && hiddenAccess == CSharpAccessModifier.PROTECTED_INTERNAL && access == CSharpAccessModifier.PROTECTED) {
            return null;
        }
        return newBuilder(getNameIdentifier(element), formatElement(element), hiddenAccess.getPresentableText(), formatElement(hidden));
    }
}
