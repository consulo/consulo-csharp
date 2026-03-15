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

package consulo.csharp.impl.ide.codeInspection.unusedUsing;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.impl.ide.codeInspection.CSharpGeneralLocalInspection;
import consulo.csharp.lang.impl.ide.codeInspection.unusedUsing.UnusedUsingVisitor;
import consulo.csharp.lang.psi.CSharpUsingListChild;
import consulo.csharp.lang.psi.CSharpUsingNamespaceStatement;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.util.dataholder.Key;

import java.util.Map;

/**
 * @author VISTALL
 * @since 05.03.2016
 */
@ExtensionImpl
public class UnusedUsingInspection extends CSharpGeneralLocalInspection {
    public static final class DeleteStatement extends LocalQuickFixOnPsiElement {
        protected DeleteStatement(PsiElement element) {
            super(element);
        }

        @Override
        public LocalizeValue getText() {
            return LocalizeValue.localizeTODO("Delete statement");
        }

        @Override
        public void invoke(Project project, PsiFile psiFile, final PsiElement element, PsiElement element2) {
            new WriteCommandAction.Simple<Object>(project, psiFile) {
                @Override
                protected void run() throws Throwable {
                    element.delete();
                }
            }.execute();
        }
    }

    private static final Key<UnusedUsingVisitor> KEY = Key.create("UnusedUsingVisitor");

    @Override
    public PsiElementVisitor buildVisitor(ProblemsHolder holder, boolean isOnTheFly, LocalInspectionToolSession session, Object state) {
        UnusedUsingVisitor visitor = session.getUserData(KEY);
        if (visitor == null) {
            session.putUserData(KEY, visitor = new UnusedUsingVisitor());
        }
        return visitor;
    }

    @Override
    @RequiredReadAction
    public void inspectionFinished(LocalInspectionToolSession session, ProblemsHolder problemsHolder, Object state) {
        UnusedUsingVisitor visitor = session.getUserData(KEY);
        if (visitor == null) {
            return;
        }

        Map<CSharpUsingListChild, Boolean> usingContext = visitor.getUsingContext();
        for (Map.Entry<CSharpUsingListChild, Boolean> entry : usingContext.entrySet()) {
            if (entry.getValue()) {
                continue;
            }

            CSharpUsingListChild element = entry.getKey();
            if (element instanceof CSharpUsingNamespaceStatement usingNamespaceStatement && usingNamespaceStatement.isGlobal()) {
                continue;
            }

            problemsHolder.registerProblem(element, "Using statement is not used", ProblemHighlightType.LIKE_UNUSED_SYMBOL, new DeleteStatement(element));
        }
    }

    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("Unused using");
    }

    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }
}
