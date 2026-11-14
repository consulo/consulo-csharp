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
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.lang.impl.psi.source.*;
import consulo.dotnet.psi.DotNetStatement;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * {@code if (b) int y = 1;} - an embedded statement cannot be a declaration or a labeled statement
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS1023 extends CompilerCheck<DotNetStatement> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetStatement element) {
        if (!(element instanceof CSharpLocalVariableDeclarationStatementImpl || element instanceof CSharpLabeledStatementImpl)) {
            return null;
        }
        // the statement of if, while, for... - not in a block
        PsiElement parent = element.getParent();
        boolean embedded = parent instanceof CSharpIfStatementImpl || parent instanceof CSharpWhileStatementImpl || parent instanceof CSharpDoWhileStatementImpl
            || parent instanceof CSharpForStatementImpl || parent instanceof CSharpForeachStatementImpl || parent instanceof CSharpUsingStatementImpl
            || parent instanceof CSharpLockStatementImpl || parent instanceof CSharpFixedStatementImpl;
        return embedded ? newBuilder(element) : null;
    }
}
