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
 * {@code struct S : A} - a type in an interface list which is not an interface
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0527 extends CompilerCheck<DotNetType> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetType element) {
        DotNetTypeDeclaration owner = BaseListUtil.getOwner(element);
        DotNetTypeDeclaration base = BaseListUtil.resolve(element);
        // the base list of a struct or an interface is of interfaces only; of a class, a class after the first is CS1721 or CS1722
        if (owner == null || base == null || base.isInterface() || !(owner.isStruct() || owner.isInterface())) {
            return null;
        }
        return newBuilder(element, formatElement(base));
    }
}
