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
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.dotnet.psi.DotNetParameter;
import org.jspecify.annotations.Nullable;

/**
 * {@code params} in one part only - both parts must use a params parameter or neither
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0758 extends CompilerCheck<CSharpMethodDeclaration> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpMethodDeclaration element) {
        if (!PartialMethodUtil.isPartial(element)) {
            return null;
        }
        CSharpMethodDeclaration other = PartialMethodUtil.isImplementing(element) ? PartialMethodUtil.getOtherPart(element) : null;
        if (other == null) {
            return null;
        }
        DotNetParameter[] parameters = element.getParameters();
        DotNetParameter[] otherParameters = other.getParameters();
        for (int i = 0; i < parameters.length && i < otherParameters.length; i++) {
            if (parameters[i].hasModifier(CSharpModifier.PARAMS) != otherParameters[i].hasModifier(CSharpModifier.PARAMS)) {
                return newBuilder(getNameIdentifier(element));
            }
        }
        return null;
    }
}
