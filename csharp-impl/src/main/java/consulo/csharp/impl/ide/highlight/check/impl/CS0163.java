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
import consulo.csharp.lang.impl.psi.source.CSharpSwitchLabelStatement;
import consulo.csharp.lang.impl.psi.source.CSharpSwitchStatementImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Control cannot fall through from one case label to another - the end of a switch section, not the last one, is
 * reachable; reported at its last label
 *
 * @author VISTALL
 */
public class CS0163 extends CompilerCheck<CSharpSwitchStatementImpl> {
    @RequiredReadAction
    @Override
    public List<? extends HighlightInfoFactory> check(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpSwitchStatementImpl element) {
        return fallingThrough(element, false).stream().map(label -> newBuilder(label, labelText(label))).toList();
    }

    /**
     * The last labels of the sections whose end is reachable - the last section, or the others
     */
    @RequiredReadAction
    public static Set<CSharpSwitchLabelStatement> fallingThrough(CSharpSwitchStatementImpl statement, boolean last) {
        Set<CSharpSwitchLabelStatement> labels = new LinkedHashSet<>();
        CSharpControlFlow flow = CSharpControlFlow.ofElement(statement);
        if (flow == null) {
            return labels;
        }
        for (CSharpControlFlow.SectionEnd end : flow.getSectionEnds()) {
            if (end.statement() == statement && end.last() == last && flow.isReachable(end.instruction())) {
                labels.add(end.label());
            }
        }
        return labels;
    }

    @RequiredReadAction
    public static String labelText(CSharpSwitchLabelStatement label) {
        return label.getText().replaceAll("\\s+", " ");
    }
}
