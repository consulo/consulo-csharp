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
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code class C : I { }} - a member of the interface is not implemented - at the base type, once per member
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0535 extends CompilerCheck<CSharpTypeDeclaration> {
    @RequiredReadAction
    @Override
    public List<? extends HighlightInfoFactory> check(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpTypeDeclaration element) {
        List<HighlightInfoFactory> results = new ArrayList<>();
        for (InterfaceImplementationUtil.Problem problem : InterfaceImplementationUtil.ofCode(element, "CS0535")) {
            results.add(newBuilder(problem.baseType(), formatElement(element), formatElement(problem.member())).withQuickFix(new CS0534.ImplementMembersQuickFix()));
        }
        return results;
    }
}
