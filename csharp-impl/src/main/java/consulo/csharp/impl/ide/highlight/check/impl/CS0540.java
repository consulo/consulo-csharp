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
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import org.jspecify.annotations.Nullable;

/**
 * {@code void I.M()} in a type without I - the containing type does not implement the interface
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0540 extends CompilerCheck<DotNetVirtualImplementOwner> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetVirtualImplementOwner element) {
        DotNetType typeForImplement = element.getTypeForImplement();
        if (typeForImplement == null || !(element.getParent() instanceof DotNetTypeDeclaration parent)) {
            return null;
        }
        if (!(typeForImplement.toTypeRef().resolve().getElement() instanceof DotNetTypeDeclaration iface) || !iface.isInterface()) {
            return null;
        }
        // the interface or an interface deriving from it in the base list - of the type or of a base type
        DotNetTypeDeclaration type = CSharpCompositeTypeDeclaration.selectCompositeOrSelfType(parent);
        if (type.isEquivalentTo(iface) || type.isInheritor(iface.getVmQName(), true)) {
            return null;
        }
        return newBuilder(typeForImplement, formatElement(element), formatElement(iface));
    }
}
