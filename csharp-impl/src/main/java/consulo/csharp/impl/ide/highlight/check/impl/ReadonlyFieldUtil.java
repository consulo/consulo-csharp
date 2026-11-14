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
import consulo.csharp.lang.impl.psi.source.CSharpAnonymousMethodExpression;
import consulo.csharp.lang.impl.psi.source.CSharpOutRefWrapExpressionImpl;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetConstructorDeclaration;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.psi.PsiUtilCore;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A readonly field passed by ref or out - CS0192, CS0199
 *
 * @author VISTALL
 * @since 2026-11-14
 */
class ReadonlyFieldUtil {
    /**
     * @return the readonly field of {@code ref field} or {@code out field} - not of {@code in field}
     */
    @Nullable
    @RequiredReadAction
    static DotNetFieldDeclaration getReadonlyField(CSharpOutRefWrapExpressionImpl wrap) {
        // in field - a readonly reference is fine
        if (PsiUtilCore.getElementType(wrap.getStartElement()) == CSharpTokens.IN_KEYWORD) {
            return null;
        }
        DotNetExpression inner = wrap.getInnerExpression();
        if (!(inner instanceof CSharpReferenceExpression reference) || !(reference.resolve() instanceof DotNetFieldDeclaration field)) {
            return null;
        }
        return field.hasModifier(CSharpModifier.READONLY) && !field.isConstant() ? field : null;
    }

    /**
     * @return true in a constructor of the type of the field - static for a static field, of this for an instance one
     */
    @RequiredReadAction
    static boolean isInConstructor(CSharpOutRefWrapExpressionImpl wrap, DotNetFieldDeclaration field) {
        CSharpSimpleLikeMethodAsElement owner = PsiTreeUtil.getParentOfType(wrap, CSharpSimpleLikeMethodAsElement.class);
        if (!(owner instanceof DotNetConstructorDeclaration constructor) || owner instanceof CSharpAnonymousMethodExpression) {
            return false;
        }
        if (constructor.hasModifier(CSharpModifier.STATIC) != field.hasModifier(CSharpModifier.STATIC)) {
            return false;
        }
        if (!(constructor.getParent() instanceof DotNetTypeDeclaration type) || !(field.getParent() instanceof DotNetTypeDeclaration fieldType)) {
            return false;
        }
        if (!Objects.equals(type.getVmQName(), fieldType.getVmQName())) {
            return false;
        }
        // other.r - the field of another instance
        DotNetExpression qualifier = ((CSharpReferenceExpression) wrap.getInnerExpression()).getQualifier();
        return qualifier == null || qualifier instanceof CSharpReferenceExpression reference && reference.kind() == CSharpReferenceExpression.ResolveToKind.THIS
            || field.hasModifier(CSharpModifier.STATIC);
    }
}
