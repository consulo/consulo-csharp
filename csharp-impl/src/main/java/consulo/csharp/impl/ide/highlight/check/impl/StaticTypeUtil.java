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
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * The static classes of the CS0710 - CS0720 checks
 *
 * @author VISTALL
 * @since 2026-11-14
 */
class StaticTypeUtil {
    /**
     * @return the static class a type refers to, null for another type
     */
    @Nullable
    @RequiredReadAction
    static DotNetTypeDeclaration resolveStatic(@Nullable DotNetType type) {
        if (type == null) {
            return null;
        }
        PsiElement resolved = DotNetTypeRefUtil.resolve(type);
        return isStatic(resolved) ? (DotNetTypeDeclaration) resolved : null;
    }

    /**
     * @return true for a member of a static class
     */
    @RequiredReadAction
    static boolean isInStaticClass(PsiElement member) {
        return isStatic(member.getParent());
    }

    @RequiredReadAction
    private static boolean isStatic(@Nullable PsiElement element) {
        return element instanceof DotNetTypeDeclaration typeDeclaration && !typeDeclaration.isInterface() && typeDeclaration.hasModifier(DotNetModifier.STATIC);
    }
}
