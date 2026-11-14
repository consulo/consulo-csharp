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
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpSoftTokens;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import org.jspecify.annotations.Nullable;

/**
 * {@code override Equals(object)} without {@code override GetHashCode()}
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0659 extends CompilerCheck<CSharpTypeDeclaration> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpTypeDeclaration element) {
        // a record has the members synthesized
        if (element.isInterface() || element.getNode().findChildByType(CSharpSoftTokens.RECORD_KEYWORD) != null) {
            return null;
        }
        boolean equals = false;
        for (DotNetNamedElement member : element.getMembers()) {
            if (!(member instanceof CSharpMethodDeclaration method) || !method.hasModifier(CSharpModifier.OVERRIDE)) {
                continue;
            }
            DotNetTypeRef[] parameters = method.getParameterTypeRefs();
            if ("GetHashCode".equals(method.getName()) && parameters.length == 0) {
                return null;
            }
            if ("Equals".equals(method.getName()) && parameters.length == 1 && DotNetTypeRefUtil.isVmQNameEqual(parameters[0], DotNetTypes.System.Object)) {
                equals = true;
            }
        }
        return equals ? newBuilder(getNameIdentifier(element), formatElement(element)) : null;
    }
}
