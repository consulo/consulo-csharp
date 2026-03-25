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

package consulo.csharp.impl.ide.codeInsight.codeVision;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.api.localize.CSharpLocalize;
import consulo.csharp.lang.CSharpLanguage;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.language.editor.codeVision.CodeVisionRelativeOrdering;
import consulo.language.editor.impl.codeVision.ReferencesCodeVisionProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * @author VISTALL
 * @since 2026-03-25
 */
@ExtensionImpl
public class CSharpReferencesCodeVisionProvider extends ReferencesCodeVisionProvider {
    public static final String ID = "csharp.references";

    @Override
    @RequiredReadAction
    public boolean acceptsFile(PsiFile file) {
        return file.getLanguage() == CSharpLanguage.INSTANCE;
    }

    @Override
    public boolean acceptsElement(PsiElement element) {
        return element instanceof DotNetModifierListOwner;
    }

    @Override
    public @Nullable String getHint(PsiElement element, PsiFile file) {
        CSharpTelescope.UsagesHint usagesHint = CSharpTelescope.usagesHint((DotNetModifierListOwner) element, file);
        if (usagesHint == null) return null;
        return usagesHint.hint().get();
    }

    @Override
    public List<CodeVisionRelativeOrdering> getRelativeOrderings() {
        return List.of(new CodeVisionRelativeOrdering.CodeVisionRelativeOrderingBefore(CSharpInheritorsCodeVisionProvider.ID));
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public LocalizeValue getName() {
        return CSharpLocalize.settingsInlayUsages();
    }
}
