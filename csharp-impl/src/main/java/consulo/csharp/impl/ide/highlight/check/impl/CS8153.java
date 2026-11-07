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
import consulo.csharp.lang.impl.psi.source.CSharpMethodCallExpressionImpl;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.MethodGroupConversion;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.jspecify.annotations.Nullable;

/**
 * A call of a method returning by reference in an expression tree
 *
 * @author VISTALL
 */
public class CS8153 extends CompilerCheck<CSharpMethodCallExpressionImpl> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpMethodCallExpressionImpl element) {
        // the tree first - cheaper than the resolve
        if (CSharpExpressionTreeUtil.findExpressionTree(element) == null || !(element.resolveToCallable() instanceof DotNetLikeMethodDeclaration method && MethodGroupConversion.isRefReturn(method))) {
            return null;
        }
        return newBuilder(element);
    }
}
