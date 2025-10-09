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

package consulo.csharp.impl.ide.codeInspection.unnecessaryModifier;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.impl.ide.codeInsight.actions.RemoveModifierFix;
import consulo.csharp.impl.ide.codeInspection.CSharpGeneralLocalInspection;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.intention.IntentionWrapper;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 17.11.2015
 */
@ExtensionImpl
public class UnnecessaryModifierInspection extends CSharpGeneralLocalInspection {
    @Nonnull
    @Override
    public PsiElementVisitor buildVisitor(@Nonnull final ProblemsHolder holder, boolean isOnTheFly) {
        return new CSharpElementVisitor() {
            @Override
            @RequiredReadAction
            public void visitTypeDeclaration(CSharpTypeDeclaration declaration) {
                DotNetModifierList modifierList = declaration.getModifierList();
                if (modifierList == null) {
                    return;
                }

                PsiElement modifierElement = modifierList.getModifierElement(CSharpModifier.PARTIAL);
                if (modifierElement != null) {
                    CSharpCompositeTypeDeclaration compositeType = CSharpCompositeTypeDeclaration.findCompositeType(declaration);
                    if (compositeType == null) {
                        holder.registerProblem(modifierElement, "Unnecessary modifier", ProblemHighlightType.LIKE_UNUSED_SYMBOL, new IntentionWrapper(new RemoveModifierFix(CSharpModifier.PARTIAL,
                            declaration), declaration.getContainingFile()));
                    }
                }
            }
        };
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("Unnecessary modifier");
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }
}
