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
import consulo.csharp.lang.impl.psi.source.CSharpReturnStatementImpl;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.psi.PsiElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Field must be fully assigned before control is returned to the caller - by a struct constructor, up to C# 10 (C# 11
 * defaults them); the plugin's C# 10 stands for 10 and later, so only C# 9 and lower
 *
 * @author VISTALL
 */
public class CS0171 extends CompilerCheck<CSharpConstructorDeclaration> {
    @RequiredReadAction
    @Override
    public List<? extends HighlightInfoFactory> check(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpConstructorDeclaration element) {
        return report(this, languageVersion, element, false);
    }

    /**
     * The members of the struct not assigned when the constructor returns - its fields and events, or its auto-properties
     */
    @RequiredReadAction
    static List<HighlightInfoFactory> report(CompilerCheck<?> check, CSharpLanguageVersion languageVersion, CSharpConstructorDeclaration element, boolean properties) {
        if (!isChecked(languageVersion)) {
            return List.of();
        }
        if (!(element.getParent() instanceof DotNetTypeDeclaration type) || !type.isStruct() || element.hasModifier(CSharpModifier.STATIC)) {
            return List.of();
        }
        CSharpControlFlow flow = CSharpControlFlow.of(element);
        if (flow == null || flow.getConstructorMembers().isEmpty()) {
            return List.of();
        }
        List<HighlightInfoFactory> results = new ArrayList<>();
        for (CSharpDefiniteAssignment.Leave leave : flow.getDefiniteAssignment().getUnassignedLeaves()) {
            if (!flow.getConstructorMembers().contains(leave.parameter()) || leave.parameter() instanceof CSharpPropertyDeclaration != properties) {
                continue;
            }
            PsiElement range = leave.element() instanceof CSharpReturnStatementImpl statement ? statement.getFirstChild() : getNameIdentifier(element);
            results.add(check.newBuilder(range, formatElement(leave.parameter())));
        }
        return results;
    }

    /**
     * C# 11 defaults the members not assigned
     */
    static boolean isChecked(CSharpLanguageVersion languageVersion) {
        return !languageVersion.isAtLeast(CSharpLanguageVersion._10_0);
    }
}
