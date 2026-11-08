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
import consulo.csharp.lang.impl.psi.controlFlow.CSharpFlowVariables;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * Use of unassigned out parameter - read before it is assigned
 *
 * @author VISTALL
 */
public class CS0269 extends CompilerCheck<CSharpReferenceExpression> {
    @RequiredReadAction
    @Override
    public List<? extends HighlightInfoFactory> check(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpReferenceExpression element) {
        List<HighlightInfoFactory> results = new ArrayList<>();
        for (DotNetVariable variable : CS0165.unassigned(element)) {
            if (CSharpFlowVariables.isOut(variable)) {
                results.add(newBuilder(element, variable.getName()));
            }
        }
        return results;
    }
}
