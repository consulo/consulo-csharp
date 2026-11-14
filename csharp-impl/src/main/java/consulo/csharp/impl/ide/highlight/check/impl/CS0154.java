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
import consulo.csharp.lang.impl.psi.source.CSharpAssignmentExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpNameOfExpressionImpl;
import consulo.csharp.lang.psi.CSharpFieldOrPropertySet;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetXAccessor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

/**
 * {@code int w = W;} of a property without a getter - it cannot be used in this context
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0154 extends CompilerCheck<CSharpReferenceExpression> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpReferenceExpression element) {
        if (!(element.resolve() instanceof CSharpPropertyDeclaration property)) {
            return null;
        }
        DotNetXAccessor[] accessors = property.getAccessors();
        if (accessors.length == 0) {
            return null;
        }
        for (DotNetXAccessor accessor : accessors) {
            if (accessor.getAccessorKind() == DotNetXAccessor.Kind.GET) {
                return null;
            }
        }
        // W = 1 and new C { W = 1 } write it, nameof(W) does not read it
        PsiElement parent = element.getParent();
        if (parent instanceof CSharpAssignmentExpressionImpl assignment && assignment.getLeftExpression() == element
            && assignment.getOperatorElement().getOperatorElementType() == CSharpTokens.EQ) {
            return null;
        }
        if (parent instanceof CSharpFieldOrPropertySet || PsiTreeUtil.getParentOfType(element, CSharpNameOfExpressionImpl.class) != null) {
            return null;
        }
        // the receiver of a member access - this.W is the reference W
        if (parent instanceof CSharpReferenceExpression qualified && qualified.getQualifier() == element) {
            return newBuilder(element, formatElement(property));
        }
        PsiElement name = element.getReferenceElement();
        return newBuilder(name == null ? element : name, formatElement(property));
    }
}
