/*
 * Copyright 2013-2017 consulo.io
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

package consulo.csharp.impl.lang.doc.inspection;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.impl.ide.highlight.check.impl.CC0001;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.doc.CSharpDocUtil;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.language.Language;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.editor.rawHighlight.HighlightInfo;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * @author VISTALL
 * @since 24.07.2015
 */
@ExtensionImpl
public class CSharpDocReferenceInspection extends LocalInspectionTool {
    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("Documentation reference problems");
    }

    @Nonnull
    @Override
    public LocalizeValue getGroupDisplayName() {
        return LocalizeValue.localizeTODO("Documentation");
    }

    @Nullable
    @Override
    public Language getLanguage() {
        return CSharpLanguage.INSTANCE;
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    @Nonnull
    @Override
    public PsiElementVisitor buildVisitor(@Nonnull final ProblemsHolder holder, boolean isOnTheFly) {
        return new CSharpElementVisitor() {
            @Override
            @RequiredReadAction
            public void visitReferenceExpression(CSharpReferenceExpression expression) {
                PsiElement referenceElement = expression.getReferenceElement();
                if (referenceElement == null || expression.isSoft() || !CSharpDocUtil.isInsideDoc(expression)) {
                    return;
                }

                List<CompilerCheck.HighlightInfoFactory> factories = CC0001.checkReference(expression, Arrays.asList(referenceElement));
                if (factories.isEmpty()) {
                    return;
                }

                for (CompilerCheck.HighlightInfoFactory factory : factories) {
                    HighlightInfo highlightInfo = factory.create(true);
                    if (highlightInfo == null) {
                        continue;
                    }
                    holder.newProblem(highlightInfo.getDescription())
                        .range((PsiElement) expression)
                        .create();
                }
            }
        };
    }
}
