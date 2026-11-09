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
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetModifierList;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Partial declarations have conflicting accessibility modifiers - two parts of different ones; one without any is of
 * the others; see {@link CS0260}
 *
 * @author VISTALL
 */
public class CS0262 extends CompilerCheck<CSharpTypeDeclaration> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpTypeDeclaration element) {
        List<CSharpTypeDeclaration> parts = CS0260.parts(element);
        if (!CS0260.isFirst(element, parts)) {
            return null;
        }
        Set<CSharpAccessModifier> modifiers = EnumSet.noneOf(CSharpAccessModifier.class);
        for (CSharpTypeDeclaration part : parts) {
            CSharpAccessModifier modifier = explicitModifier(part);
            if (modifier != CSharpAccessModifier.NONE) {
                modifiers.add(modifier);
            }
        }
        return modifiers.size() > 1 ? newBuilder(getNameIdentifier(element), formatElement(element)) : null;
    }

    /**
     * The access modifier written - not the default of a part without one
     */
    @RequiredReadAction
    private static CSharpAccessModifier explicitModifier(CSharpTypeDeclaration part) {
        DotNetModifierList modifierList = part.getModifierList();
        if (modifierList == null) {
            return CSharpAccessModifier.NONE;
        }
        loop:
        for (CSharpAccessModifier value : CSharpAccessModifier.VALUES) {
            if (value == CSharpAccessModifier.NONE) {
                continue;
            }
            for (CSharpModifier modifier : value.getModifiers()) {
                if (!modifierList.hasModifierInTree(modifier)) {
                    continue loop;
                }
            }
            return value;
        }
        return CSharpAccessModifier.NONE;
    }
}
