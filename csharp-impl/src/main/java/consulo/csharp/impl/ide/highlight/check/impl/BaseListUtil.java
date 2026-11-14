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
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeList;
import org.jspecify.annotations.Nullable;

/**
 * The types of a base list - CS0527, CS0528, CS1721
 *
 * @author VISTALL
 * @since 2026-11-14
 */
class BaseListUtil {
    /**
     * @return the type declaration whose base list the type is in, null for another type
     */
    @Nullable
    static DotNetTypeDeclaration getOwner(DotNetType type) {
        if (type.getParent() instanceof DotNetTypeList list && list.getParent() instanceof DotNetTypeDeclaration owner && owner.getExtendList() == list) {
            return owner;
        }
        return null;
    }

    @Nullable
    @RequiredReadAction
    static DotNetTypeDeclaration resolve(DotNetType type) {
        return type.toTypeRef().resolve().getElement() instanceof DotNetTypeDeclaration declaration ? declaration : null;
    }
}
