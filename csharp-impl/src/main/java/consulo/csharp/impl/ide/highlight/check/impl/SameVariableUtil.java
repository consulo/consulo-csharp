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
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetVariable;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * The same variable on both sides - CS1717 {@code x = x}, CS1718 {@code x == x}; locals, parameters and fields of the same receiver
 * only, a property has a getter and a setter
 *
 * @author VISTALL
 * @since 2026-11-14
 */
class SameVariableUtil {
    @RequiredReadAction
    static boolean isSameVariable(@Nullable DotNetExpression left, @Nullable DotNetExpression right) {
        if (!(left instanceof CSharpReferenceExpression a) || !(right instanceof CSharpReferenceExpression b)) {
            return false;
        }
        PsiElement resolved = a.resolve();
        if (!(resolved instanceof CSharpLocalVariable || resolved instanceof DotNetParameter || resolved instanceof DotNetFieldDeclaration)
            || !resolved.isEquivalentTo(b.resolve()) || ((DotNetVariable) resolved).isConstant()) {
            return false;
        }
        return isSameReceiver(a.getQualifier(), b.getQualifier());
    }

    @RequiredReadAction
    private static boolean isSameReceiver(@Nullable DotNetExpression a, @Nullable DotNetExpression b) {
        if (a == null || b == null) {
            return a == b;
        }
        if (a instanceof CSharpReferenceExpression ra && b instanceof CSharpReferenceExpression rb
            && ra.kind() == CSharpReferenceExpression.ResolveToKind.THIS && rb.kind() == CSharpReferenceExpression.ResolveToKind.THIS) {
            return true;
        }
        return isSameVariable(a, b);
    }
}
