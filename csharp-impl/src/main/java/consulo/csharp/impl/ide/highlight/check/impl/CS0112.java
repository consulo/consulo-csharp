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
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import org.jspecify.annotations.Nullable;

/**
 * {@code static virtual void M()} of a class - a static member cannot be virtual, abstract or override
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0112 extends CompilerCheck<DotNetModifierListOwner> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetModifierListOwner element) {
        // static abstract and static virtual members of an interface are C# 11
        if (!(element.getParent() instanceof DotNetTypeDeclaration type) || type.isInterface() || !element.hasModifier(CSharpModifier.STATIC)) {
            return null;
        }
        DotNetModifierList modifierList = element.getModifierList();
        if (modifierList == null) {
            return null;
        }
        for (CSharpModifier modifier : new CSharpModifier[]{CSharpModifier.VIRTUAL, CSharpModifier.ABSTRACT, CSharpModifier.OVERRIDE}) {
            if (modifierList.getModifierElement(modifier) != null) {
                return newBuilder(getNameIdentifier(element), modifier.getPresentableText());
            }
        }
        return null;
    }
}
