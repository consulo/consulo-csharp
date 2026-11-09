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
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Partial declarations must not specify different base classes; see {@link CS0260}
 *
 * @author VISTALL
 */
public class CS0263 extends CompilerCheck<CSharpTypeDeclaration> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpTypeDeclaration element) {
        List<CSharpTypeDeclaration> parts = CS0260.parts(element);
        if (!CS0260.isFirst(element, parts) || element.isInterface()) {
            return null;
        }
        DotNetTypeRef baseClass = null;
        for (CSharpTypeDeclaration part : parts) {
            DotNetTypeRef partBaseClass = baseClass(part);
            if (partBaseClass == null) {
                continue;
            }
            if (baseClass == null) {
                baseClass = partBaseClass;
            }
            else if (!CSharpTypeUtil.isTypeEqual(baseClass, partBaseClass)) {
                return newBuilder(getNameIdentifier(element), formatElement(element));
            }
        }
        return null;
    }

    /**
     * The first type of the base list of the part, if a class
     */
    @RequiredReadAction
    private static @Nullable DotNetTypeRef baseClass(CSharpTypeDeclaration part) {
        DotNetTypeList extendList = part.getExtendList();
        DotNetType[] types = extendList == null ? DotNetType.EMPTY_ARRAY : extendList.getTypes();
        if (types.length == 0) {
            return null;
        }
        DotNetTypeRef typeRef = types[0].toTypeRef();
        return typeRef.resolve().getElement() instanceof DotNetTypeDeclaration declaration && !declaration.isInterface() ? typeRef : null;
    }
}
