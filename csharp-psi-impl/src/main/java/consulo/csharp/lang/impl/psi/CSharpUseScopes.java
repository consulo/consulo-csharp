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

package consulo.csharp.lang.impl.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.psi.CSharpAccessModifier;
import consulo.csharp.lang.psi.CSharpEnumConstantDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.language.psi.scope.LocalSearchScope;
import org.jspecify.annotations.Nullable;

/**
 * The use scope of a member by its accessibility - where Find Usages, rename and the unused checks look for the
 * references to it
 *
 * @author VISTALL
 */
public final class CSharpUseScopes {
    private CSharpUseScopes() {
    }

    /**
     * A private member (or nested type) is used only in its type - all the parts of a partial one; null for another
     * member. Not a field: a private [SerializeField] of Unity is referenced from its scene files (the search of the
     * Unity plugin needs a global scope)
     */
    @RequiredReadAction
    public static @Nullable LocalSearchScope getPrivateMemberScope(DotNetModifierListOwner member) {
        if (!(member.getParent() instanceof CSharpTypeDeclaration type) || member instanceof DotNetFieldDeclaration || member instanceof CSharpEnumConstantDeclaration) {
            return null;
        }
        // an explicit implementation has no access modifier, it is called through the interface
        if (member instanceof DotNetVirtualImplementOwner implementOwner && implementOwner.getTypeForImplement() != null) {
            return null;
        }
        CSharpAccessModifier access = CSharpAccessModifier.findModifier(member);
        if (access == CSharpAccessModifier.PRIVATE || access == CSharpAccessModifier.NONE && !type.isInterface() && !type.isEnum()) {
            return CSharpCompositeTypeDeclaration.createLocalScope(type);
        }
        return null;
    }
}
