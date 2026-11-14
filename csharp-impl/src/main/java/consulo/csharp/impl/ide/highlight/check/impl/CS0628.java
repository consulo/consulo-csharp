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
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetXAccessor;
import org.jspecify.annotations.Nullable;

/**
 * {@code protected int P;} of a sealed class - a new protected member declared in a sealed type
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0628 extends CompilerCheck<DotNetModifierListOwner> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetModifierListOwner element) {
        // a sealed class - not a struct, the protected members of a struct are CS0666
        if (!(element.getParent() instanceof DotNetTypeDeclaration type) || !(element instanceof DotNetNamedElement) || element instanceof DotNetXAccessor
            || type.isStruct() || type.isInterface() || !type.hasModifier(CSharpModifier.SEALED)) {
            return null;
        }
        if (!element.hasModifier(CSharpModifier.PROTECTED) || element.hasModifier(CSharpModifier.OVERRIDE)) {
            return null;
        }
        return newBuilder(getNameIdentifier(element), formatElement(element));
    }
}
