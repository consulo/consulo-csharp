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

package consulo.csharp.impl.ide.highlight.check.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.Editor;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.source.CSharpForeachStatementImpl;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.language.editor.inspection.LocalQuickFixAndIntentionActionOnPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.search.ReferencesSearch;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class CS0168 extends CompilerCheck<CSharpLocalVariable> {
    public static final class DeleteLocalVariable extends LocalQuickFixAndIntentionActionOnPsiElement {
        public DeleteLocalVariable(@Nonnull PsiElement element) {
            super(element);
        }

        @Nonnull
        @Override
        public LocalizeValue getText() {
            return LocalizeValue.localizeTODO("Delete variable");
        }

        @Override
        public void invoke(@Nonnull Project project,
                           @Nonnull PsiFile psiFile,
                           @Nullable Editor editor,
                           @Nonnull PsiElement psiElement,
                           @Nonnull PsiElement psiElement1) {
            psiElement.delete();
        }
    }

    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpLocalVariable element) {
        if (element.getInitializer() != null) {
            return null;
        }

        if (isUnused(element)) {
            CompilerCheckBuilder builder = newBuilder(element.getNameIdentifier(), formatElement(element));
            if (!(element.getParent() instanceof CSharpForeachStatementImpl)) {
                builder.withQuickFix(new DeleteLocalVariable(element));
            }
            return builder;
        }

        return null;
    }

    @RequiredReadAction
    static boolean isUnused(@Nonnull CSharpLocalVariable element) {
        PsiElement nameIdentifier = element.getNameIdentifier();
        if (nameIdentifier == null) {
            return false;
        }

        return ReferencesSearch.search(element).findFirst() == null;
    }
}
