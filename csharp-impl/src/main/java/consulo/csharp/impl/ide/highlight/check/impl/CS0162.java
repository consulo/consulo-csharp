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
import consulo.csharp.lang.impl.psi.source.CSharpLabeledStatementImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetStatement;
import org.jspecify.annotations.Nullable;

/**
 * Unreachable code detected - the first statement of an unreachable run
 *
 * @author VISTALL
 */
public class CS0162 extends CompilerCheck<DotNetStatement> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetStatement element) {
        CSharpControlFlow flow = CSharpControlFlow.ofElement(element);
        if (flow == null || !flow.getUnreachableStatements().contains(element)) {
            return null;
        }
        return newBuilder(element instanceof CSharpLabeledStatementImpl labeled ? labeled.getNameIdentifier() : element);
    }
}
