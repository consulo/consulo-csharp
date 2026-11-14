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
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import org.jspecify.annotations.Nullable;

/**
 * {@code static class S : I} - static classes cannot implement interfaces
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0714 extends CompilerCheck<DotNetTypeDeclaration> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetTypeDeclaration element) {
        DotNetTypeList extendList = element.getExtendList();
        if (extendList == null || element.isInterface() || !element.hasModifier(DotNetModifier.STATIC)) {
            return null;
        }
        for (DotNetType type : extendList.getTypes()) {
            if (DotNetTypeRefUtil.resolve(type) instanceof DotNetTypeDeclaration base && base.isInterface()) {
                return newBuilder(type, formatElement(element));
            }
        }
        return null;
    }
}
