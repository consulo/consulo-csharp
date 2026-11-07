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
import consulo.csharp.lang.impl.psi.CSharpExpressionTreeUtil;
import consulo.csharp.lang.impl.psi.source.CSharpIndexAccessExpressionImpl;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import org.jspecify.annotations.Nullable;

/**
 * A null propagating operator - {@code ?.} and {@code ?[]} - in an expression tree
 *
 * @author VISTALL
 */
public class CS8072 extends CompilerCheck<DotNetExpression> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetExpression element) {
        if (!(element instanceof CSharpReferenceExpression reference && reference.getMemberAccessType() == CSharpReferenceExpression.AccessType.NULLABLE_CALL
                || element instanceof CSharpIndexAccessExpressionImpl indexAccess && indexAccess.isNullable()) || CSharpExpressionTreeUtil.findExpressionTree(element) == null) {
            return null;
        }
        return newBuilder(element);
    }
}
