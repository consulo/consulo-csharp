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
import consulo.csharp.lang.impl.psi.controlFlow.CSharpDefiniteAssignment;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.module.extension.CSharpLanguageVersion;

import java.util.ArrayList;
import java.util.List;

/**
 * Use of possibly unassigned field - read in a struct constructor before it is assigned; up to C# 10, see {@link CS0171}
 *
 * @author VISTALL
 */
public class CS9015 extends CompilerCheck<CSharpReferenceExpression> {
    @RequiredReadAction
    @Override
    public List<? extends HighlightInfoFactory> check(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpReferenceExpression element) {
        if (!CS0171.isChecked(languageVersion)) {
            return List.of();
        }
        CSharpControlFlow flow = CSharpControlFlow.ofElement(element);
        if (flow == null || flow.getConstructorMembers().isEmpty()) {
            return List.of();
        }
        List<HighlightInfoFactory> results = new ArrayList<>();
        for (CSharpDefiniteAssignment.Read read : flow.getDefiniteAssignment().getUnassignedMemberReads()) {
            if (read.element() == element && read.variable() instanceof CSharpPropertyDeclaration == false) {
                results.add(newBuilder(element, read.variable().getName(), CS0171.AUTO_DEFAULT_VERSION));
            }
        }
        return results;
    }
}
