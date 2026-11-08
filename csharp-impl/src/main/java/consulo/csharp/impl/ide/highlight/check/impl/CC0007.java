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
import consulo.csharp.lang.impl.psi.controlFlow.CSharpControlFlow;
import consulo.csharp.lang.impl.psi.controlFlow.CSharpUnusedValues;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.language.psi.PsiElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Unnecessary assignment of a value - assigned to a local or a parameter and never read after (Roslyn's IDE0059);
 * at the name of the declaration or at the reference assigned
 *
 * @author VISTALL
 */
public class CC0007 extends CompilerCheck<CSharpSimpleLikeMethodAsElement> {
    @RequiredReadAction
    @Override
    public List<? extends HighlightInfoFactory> check(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpSimpleLikeMethodAsElement element) {
        CSharpControlFlow flow = CSharpControlFlow.of(element);
        if (flow == null) {
            return List.of();
        }
        List<HighlightInfoFactory> results = new ArrayList<>();
        for (CSharpUnusedValues.Write write : flow.getUnusedValues()) {
            PsiElement range = write.element() == write.variable() ? getNameIdentifier(write.element()) : write.element();
            results.add(newBuilder(range, write.variable().getName()));
        }
        return results;
    }
}
