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
import consulo.csharp.lang.impl.psi.source.CSharpRangeExpressionImpl;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import org.jspecify.annotations.Nullable;

/**
 * An indexer access by System.Index or System.Range in an expression tree
 *
 * @author VISTALL
 */
public class CS8790 extends CompilerCheck<CSharpIndexAccessExpressionImpl> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpIndexAccessExpressionImpl element) {
        if (CSharpExpressionTreeUtil.findExpressionTree(element) == null) {
            return null;
        }
        CSharpCallArgument[] arguments = element.getCallArguments();
        DotNetExpression argument = arguments.length == 1 ? arguments[0].getArgumentExpression() : null;
        if (argument == null || !(argument.toTypeRef(true).resolve().getElement() instanceof DotNetTypeDeclaration type)
            || !CSharpRangeExpressionImpl.INDEX.equals(type.getVmQName()) && !CSharpRangeExpressionImpl.RANGE.equals(type.getVmQName())) {
            return null;
        }
        return newBuilder(element);
    }
}
