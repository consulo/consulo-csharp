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
import consulo.csharp.lang.impl.psi.controlFlow.CSharpFlowVariables;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetVariable;
import consulo.language.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Use of unassigned local variable - read where it is not definitely assigned (C# spec 9.4), in a lambda where it is
 * created, through a local function where it is referenced
 *
 * @author VISTALL
 */
public class CS0165 extends CompilerCheck<CSharpReferenceExpression> {
    @RequiredReadAction
    @Override
    public List<? extends HighlightInfoFactory> check(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpReferenceExpression element) {
        List<HighlightInfoFactory> results = new ArrayList<>();
        for (DotNetVariable variable : unassigned(element)) {
            if (!CSharpFlowVariables.isOut(variable)) {
                results.add(newBuilder(element, variable.getName()));
            }
        }
        return results;
    }

    /**
     * The variables read at the reference not assigned - reported by its owner or, for a read in a lambda, by an
     * enclosing one
     */
    @RequiredReadAction
    public static Set<DotNetVariable> unassigned(CSharpReferenceExpression element) {
        Set<DotNetVariable> variables = new LinkedHashSet<>();
        for (CSharpSimpleLikeMethodAsElement owner = PsiTreeUtil.getParentOfType(element, CSharpSimpleLikeMethodAsElement.class); owner != null;
             owner = PsiTreeUtil.getParentOfType(owner, CSharpSimpleLikeMethodAsElement.class)) {
            CSharpControlFlow flow = CSharpControlFlow.of(owner);
            if (flow != null) {
                variables.addAll(flow.getDefiniteAssignment().getUnassignedReads(element));
            }
        }
        return variables;
    }
}
