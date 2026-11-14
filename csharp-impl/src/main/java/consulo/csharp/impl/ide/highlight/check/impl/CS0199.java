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
import consulo.csharp.lang.impl.psi.source.CSharpOutRefWrapExpressionImpl;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import org.jspecify.annotations.Nullable;

/**
 * {@code Ref(ref staticReadonlyField)} - a static readonly field cannot be used as a ref or out value except in a static constructor
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0199 extends CompilerCheck<CSharpOutRefWrapExpressionImpl> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpOutRefWrapExpressionImpl element) {
        DotNetFieldDeclaration field = ReadonlyFieldUtil.getReadonlyField(element);
        if (field == null || !field.hasModifier(CSharpModifier.STATIC) || ReadonlyFieldUtil.isInConstructor(element, field)) {
            return null;
        }
        return newBuilder(element.getInnerExpression());
    }
}
