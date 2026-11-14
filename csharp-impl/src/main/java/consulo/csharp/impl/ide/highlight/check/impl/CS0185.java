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
import consulo.csharp.lang.impl.psi.source.CSharpLockStatementImpl;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

/**
 * {@code lock (i)} of an int - the lock statement requires a reference type
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0185 extends CompilerCheck<CSharpLockStatementImpl> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpLockStatementImpl element) {
        DotNetExpression expression = PsiTreeUtil.getChildOfType(element, DotNetExpression.class);
        if (expression == null) {
            return null;
        }
        DotNetTypeRef typeRef = expression.toTypeRef(true);
        if (!(typeRef.resolve().getElement() instanceof DotNetTypeDeclaration type) || !(type.isStruct() || type.isEnum())) {
            return null;
        }
        return newBuilder(expression, formatTypeRef(typeRef));
    }
}
