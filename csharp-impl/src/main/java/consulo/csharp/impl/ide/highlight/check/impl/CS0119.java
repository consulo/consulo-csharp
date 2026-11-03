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
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpReferenceExpressionEx;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import org.jspecify.annotations.Nullable;

/**
 * {@code error.Call.Trim()} with a method {@code Call} - a method as the qualifier of a member access: "'Call()' is a
 * method, which is not valid in the given context", not "'Call' is not resolved".
 *
 * @author VISTALL
 */
public class CS0119 extends CompilerCheck<CSharpReferenceExpression> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpReferenceExpression element) {
        DotNetLikeMethodDeclaration method = findMethodAsQualifier(element);
        PsiElement referenceElement = element.getReferenceElement();
        if (method == null || referenceElement == null) {
            return null;
        }
        return newBuilder(referenceElement, formatElement(method));
    }

    /**
     * @return the method named by an unresolved qualifier of a member access
     */
    @Nullable
    @RequiredReadAction
    public static DotNetLikeMethodDeclaration findMethodAsQualifier(CSharpReferenceExpression element) {
        if (!(element instanceof CSharpReferenceExpressionEx ex) || !(element.getParent() instanceof CSharpReferenceExpression parent)
            || parent.getQualifier() != element || element.resolve() != null) {
            return null;
        }

        // the member group the qualifier position does not take
        for (ResolveResult result : ex.multiResolveImpl(element.kind(), false)) {
            PsiElement resolved = result.getElement();
            if (resolved instanceof DotNetLikeMethodDeclaration method) {
                return method;
            }
            if (resolved instanceof CSharpElementGroup<?> group) {
                for (PsiElement groupElement : group.getElements()) {
                    if (groupElement instanceof DotNetLikeMethodDeclaration method) {
                        return method;
                    }
                }
            }
        }
        return null;
    }
}
