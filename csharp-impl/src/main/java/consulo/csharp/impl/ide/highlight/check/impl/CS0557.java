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
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.util.collection.ArrayUtil;
import consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import org.jspecify.annotations.Nullable;

/**
 * a second {@code explicit operator int(C c)} - duplicate user-defined conversion
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0557 extends CompilerCheck<CSharpConversionMethodDeclaration> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpConversionMethodDeclaration element) {
        if (!(element.getParent() instanceof DotNetTypeDeclaration parent)) {
            return null;
        }
        DotNetTypeRef[] parameters = element.getParameterTypeRefs();
        if (parameters.length != 1) {
            return null;
        }
        // the conversions with the same types - implicit or explicit - the first one is fine
        DotNetTypeDeclaration type = CSharpCompositeTypeDeclaration.selectCompositeOrSelfType(parent);
        for (DotNetNamedElement member : type.getMembers()) {
            if (member == element || member.isEquivalentTo(element)) {
                return null;
            }
            if (member instanceof CSharpConversionMethodDeclaration other && other.getParameterTypeRefs().length == 1
                && CSharpTypeUtil.isTypeEqual(other.getReturnTypeRef(), element.getReturnTypeRef())
                && CSharpTypeUtil.isTypeEqual(other.getParameterTypeRefs()[0], parameters[0])) {
                return newBuilder(getNameIdentifier(element), formatElement(parent));
            }
        }
        return null;
    }
}
