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
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import org.jspecify.annotations.Nullable;

/**
 * {@code implicit operator B(D d)} of D : B - user-defined conversions to or from a base type are not allowed
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0553 extends CompilerCheck<CSharpConversionMethodDeclaration> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpConversionMethodDeclaration element) {
        DotNetTypeRef other = OperatorCheckUtil.getOtherConversionType(element);
        DotNetTypeDeclaration type = (DotNetTypeDeclaration) element.getParent();
        if (other == null || !(other.resolve().getElement() instanceof DotNetTypeDeclaration otherType) || otherType.isInterface() || otherType.isEquivalentTo(type)) {
            return null;
        }
        // the containing type derives from the other one - object included
        if (!type.isInheritor(otherType.getVmQName(), true)) {
            return null;
        }
        return newBuilder(getNameIdentifier(element), formatElement(element));
    }
}
