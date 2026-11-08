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

package consulo.csharp.lang.impl.psi.controlFlow;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.source.CSharpBlockStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpLabeledStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpSwitchLabelStatement;
import consulo.csharp.lang.impl.psi.source.CSharpSwitchStatementImpl;
import consulo.dotnet.psi.DotNetStatement;
import consulo.language.psi.PsiElement;

import java.util.ArrayList;
import java.util.List;

/**
 * A switch section - its labels and its statements. The parser puts the labels and the statements of all sections in
 * one block, and a labeled statement takes all the statements after it - also the next labels; both are flattened here,
 * so a labeled statement is in {@link #statements()} without its children, which follow it.
 *
 * @author VISTALL
 */
public record CSharpSwitchSection(List<CSharpSwitchLabelStatement> labels, List<DotNetStatement> statements) {
    public CSharpSwitchLabelStatement lastLabel() {
        return labels.get(labels.size() - 1);
    }

    @RequiredReadAction
    public static List<CSharpSwitchSection> of(CSharpSwitchStatementImpl statement) {
        List<CSharpSwitchSection> sections = new ArrayList<>();
        CSharpBlockStatementImpl block = null;
        for (PsiElement child = statement.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof CSharpBlockStatementImpl childBlock) {
                block = childBlock;
                break;
            }
        }
        if (block == null) {
            return sections;
        }

        List<DotNetStatement> flat = new ArrayList<>();
        flatten(block, flat);

        List<CSharpSwitchLabelStatement> labels = new ArrayList<>();
        List<DotNetStatement> statements = new ArrayList<>();
        for (DotNetStatement child : flat) {
            if (child instanceof CSharpSwitchLabelStatement label) {
                if (!statements.isEmpty()) {
                    sections.add(new CSharpSwitchSection(labels, statements));
                    labels = new ArrayList<>();
                    statements = new ArrayList<>();
                }
                labels.add(label);
            }
            // statements before the first label are an error
            else if (!labels.isEmpty()) {
                statements.add(child);
            }
        }
        if (!labels.isEmpty()) {
            sections.add(new CSharpSwitchSection(labels, statements));
        }
        return sections;
    }

    private static void flatten(PsiElement parent, List<DotNetStatement> result) {
        for (PsiElement child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof DotNetStatement statement) {
                result.add(statement);
                if (statement instanceof CSharpLabeledStatementImpl) {
                    flatten(statement, result);
                }
            }
        }
    }
}
