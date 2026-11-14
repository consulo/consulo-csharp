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
import consulo.csharp.lang.impl.psi.source.CSharpForeachStatementImpl;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpReferenceExpressionEx;
import consulo.dotnet.psi.DotNetExpression;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.language.psi.ResolveResult;
import org.jspecify.annotations.Nullable;

/**
 * {@code foreach (var x in Items)} of a method - foreach cannot operate on a method group
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0446 extends CompilerCheck<CSharpForeachStatementImpl> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpForeachStatementImpl element) {
        DotNetExpression iterable = element.getIterableExpression();
        if (!(iterable instanceof CSharpReferenceExpressionEx reference)) {
            return null;
        }
        // a method group has no valid result without a delegate type - the members of the name as nameof(Items)
        for (ResolveResult result : reference.multiResolveImpl(CSharpReferenceExpression.ResolveToKind.NAMEOF, true)) {
            if (result.getElement() instanceof DotNetLikeMethodDeclaration || result.getElement() instanceof CSharpElementGroup<?>) {
                return newBuilder(iterable);
            }
        }
        return null;
    }
}
