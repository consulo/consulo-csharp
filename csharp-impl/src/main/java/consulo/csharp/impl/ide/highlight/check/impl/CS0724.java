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
import consulo.csharp.lang.impl.psi.source.CSharpFinallyStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpThrowStatementImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import org.jspecify.annotations.Nullable;

/**
 * A throw statement with no arguments is not allowed in a finally clause that is nested inside the nearest enclosing
 * catch clause
 *
 * @author VISTALL
 */
public class CS0724 extends CompilerCheck<CSharpThrowStatementImpl> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpThrowStatementImpl element) {
        return CS0156.rethrowPlace(element) instanceof CSharpFinallyStatementImpl ? newBuilder(element.getFirstChild()) : null;
    }
}
