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

package consulo.csharp.impl.versionControlSystem;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetMethodDeclaration;
import consulo.language.Language;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.versionControlSystem.codeVision.VcsCodeVisionCurlyBracketLanguageContext;

/**
 * @author VISTALL
 * @since 2026-03-25
 */
@ExtensionImpl
public class CSharpVcsCodeVisionContext extends VcsCodeVisionCurlyBracketLanguageContext {

    @Override
    public boolean isAccepted(PsiElement element) {
        return element instanceof CSharpSimpleLikeMethodAsElement && element instanceof DotNetMethodDeclaration
            || element instanceof CSharpTypeDeclaration;
    }

    @Override
    protected boolean isRBrace(PsiElement element) {
        return PsiUtilCore.getElementType(element) == CSharpTokens.RBRACE;
    }

    @Override
    public Language getLanguage() {
        return CSharpLanguage.INSTANCE;
    }
}