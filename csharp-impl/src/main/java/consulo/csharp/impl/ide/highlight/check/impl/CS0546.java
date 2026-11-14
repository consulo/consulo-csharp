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
import consulo.csharp.lang.psi.CSharpXAccessorOwner;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetXAccessor;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * {@code override int P { set; }} of a property without an overridable set accessor
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0546 extends CompilerCheck<DotNetXAccessor> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetXAccessor element) {
        if (element.getAccessorKind() != DotNetXAccessor.Kind.SET || !(element.getParent() instanceof DotNetModifierListOwner owner)) {
            return null;
        }
        DotNetModifierListOwner overridden = OverrideCheckUtil.getOverridden(owner);
        if (!(overridden instanceof CSharpXAccessorOwner accessorOwner)) {
            return null;
        }
        for (DotNetXAccessor accessor : accessorOwner.getAccessors()) {
            if (accessor.getAccessorKind() == DotNetXAccessor.Kind.SET && !accessor.hasModifier(CSharpModifier.PRIVATE)) {
                return null;
            }
        }
        PsiElement keyword = getNameIdentifier(element);
        return newBuilder(keyword == null ? element : keyword, formatElement(element), formatElement(overridden));
    }
}
