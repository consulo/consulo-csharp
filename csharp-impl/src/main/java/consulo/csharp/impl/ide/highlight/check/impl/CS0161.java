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
import consulo.csharp.lang.impl.psi.CSharpImplicitReturnModel;
import consulo.csharp.lang.impl.psi.controlFlow.CSharpControlFlow;
import consulo.csharp.lang.impl.psi.source.CSharpAnonymousMethodExpression;
import consulo.csharp.lang.impl.psi.source.CSharpTopLevelStatementsImpl;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetConstructorDeclaration;
import consulo.dotnet.psi.DotNetStatement;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

/**
 * Not all code paths return a value - the end point of the body of a method, an operator, an accessor, a local function
 * or the top-level statements returning a value is reachable
 *
 * @author VISTALL
 * @since 16-Nov-17
 */
public class CS0161 extends CompilerCheck<CSharpSimpleLikeMethodAsElement> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpSimpleLikeMethodAsElement element) {
        // lambdas and anonymous methods - CS1643
        if (element instanceof CSharpAnonymousMethodExpression || element instanceof DotNetConstructorDeclaration
            || !returnsValue(element, element.getReturnTypeRef()) || !isEndReachable(element)) {
            return null;
        }

        if (element instanceof CSharpTopLevelStatementsImpl) {
            DotNetStatement first = PsiTreeUtil.getChildOfType(element, DotNetStatement.class);
            return newBuilder(first != null ? first : element, "<top-level-statements-entry-point>");
        }
        return newBuilder(getNameIdentifier(element), formatElement(element));
    }

    /**
     * The end point of the body is reachable, and it is not an iterator
     */
    @RequiredReadAction
    public static boolean isEndReachable(CSharpSimpleLikeMethodAsElement element) {
        CSharpControlFlow flow = CSharpControlFlow.of(element);
        return flow != null && !flow.isIterator() && flow.isEndReachable();
    }

    /**
     * A return statement must have a value - not void, nor Task or ValueTask of an async one
     */
    @RequiredReadAction
    public static boolean returnsValue(CSharpSimpleLikeMethodAsElement element, DotNetTypeRef returnTypeRef) {
        if (element.hasModifier(CSharpModifier.ASYNC)) {
            returnTypeRef = CSharpImplicitReturnModel.Async.extractTypeRefImpl(returnTypeRef);
            if (returnTypeRef == null) {
                return false;
            }
        }
        return returnTypeRef != DotNetTypeRef.ERROR_TYPE && returnTypeRef != DotNetTypeRef.UNKNOWN_TYPE && returnTypeRef != DotNetTypeRef.AUTO_TYPE
            && !DotNetTypeRefUtil.isVmQNameEqual(returnTypeRef, DotNetTypes.System.Void);
    }
}
