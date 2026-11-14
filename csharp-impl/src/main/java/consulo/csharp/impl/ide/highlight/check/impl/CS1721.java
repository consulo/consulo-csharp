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
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import org.jspecify.annotations.Nullable;

/**
 * {@code class C : A, B} - a class cannot have multiple base classes
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS1721 extends CompilerCheck<DotNetType> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetType element) {
        DotNetTypeDeclaration owner = BaseListUtil.getOwner(element);
        DotNetTypeDeclaration base = BaseListUtil.resolve(element);
        if (owner == null || base == null || base.isInterface() || owner.isStruct() || owner.isInterface() || owner.isEnum()) {
            return null;
        }
        // a class before it - the first class is the base class
        DotNetType[] types = owner.getExtendList().getTypes();
        for (DotNetType previous : types) {
            if (previous == element) {
                return null;
            }
            DotNetTypeDeclaration previousType = BaseListUtil.resolve(previous);
            if (previousType != null && !previousType.isInterface()) {
                return newBuilder(element, formatElement(owner), formatElement(previousType), formatElement(base));
            }
        }
        return null;
    }
}
