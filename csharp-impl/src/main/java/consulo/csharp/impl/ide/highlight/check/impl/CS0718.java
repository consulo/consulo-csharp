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
import consulo.csharp.lang.impl.psi.CSharpElements;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.language.ast.IElementType;
import org.jspecify.annotations.Nullable;

/**
 * {@code List<S>} - static types cannot be used as type arguments
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0718 extends CompilerCheck<DotNetType> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetType element) {
        if (!(element.getParent() instanceof DotNetTypeList typeList)) {
            return null;
        }
        IElementType listType = typeList.getNode().getElementType();
        if (listType != CSharpElements.TYPE_ARGUMENTS && listType != CSharpStubElements.TYPE_ARGUMENTS) {
            return null;
        }
        DotNetTypeDeclaration staticType = StaticTypeUtil.resolveStatic(element);
        if (staticType == null) {
            return null;
        }
        return newBuilder(element, formatElement(staticType));
    }
}
