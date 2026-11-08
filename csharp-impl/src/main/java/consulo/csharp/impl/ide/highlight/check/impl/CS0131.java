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
import consulo.csharp.lang.impl.psi.source.CSharpAssignmentExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpConstantExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpMethodCallExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpParenthesesExpressionImpl;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.MethodGroupConversion;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import org.jspecify.annotations.Nullable;

/**
 * The left-hand side of an assignment must be a variable, property or indexer - a literal, a constant, a call of a
 * method not returning by reference
 *
 * @author VISTALL
 */
public class CS0131 extends CompilerCheck<CSharpAssignmentExpressionImpl> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpAssignmentExpressionImpl element) {
        DotNetExpression left = element.getLeftExpression();
        DotNetExpression target = left;
        while (target instanceof CSharpParenthesesExpressionImpl parentheses) {
            target = parentheses.getInnerExpression();
        }
        boolean notVariable = target instanceof CSharpConstantExpressionImpl
            || target instanceof CSharpReferenceExpression reference && reference.resolve() instanceof DotNetVariable variable && variable.isConstant()
            || target instanceof CSharpMethodCallExpressionImpl call && call.resolveToCallable() instanceof DotNetLikeMethodDeclaration method
            && !MethodGroupConversion.isRefReturn(method);
        return notVariable ? newBuilder(left) : null;
    }
}
