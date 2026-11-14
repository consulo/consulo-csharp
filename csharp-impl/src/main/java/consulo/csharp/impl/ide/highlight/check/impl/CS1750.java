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
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.CSharpCastType;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import org.jspecify.annotations.Nullable;

/**
 * {@code int x = "s"} - the default value has no standard conversion to the parameter type
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS1750 extends CompilerCheck<DotNetParameter> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetParameter element) {
        DotNetExpression initializer = element.getInitializer();
        if (initializer == null) {
            return null;
        }
        DotNetTypeRef parameterTypeRef = element.toTypeRef(false);
        DotNetTypeRef valueTypeRef = initializer.toTypeRef(true);
        if (CSharpTypeUtil.isErrorTypeRef(parameterTypeRef) || CSharpTypeUtil.isErrorTypeRef(valueTypeRef)) {
            return null;
        }
        if (CSharpTypeUtil.isInheritable(parameterTypeRef, valueTypeRef, CSharpCastType.IMPLICIT, element.getResolveScope()).isSuccess()) {
            return null;
        }
        // byte b = 1 - a constant of the range, CS0031 out of it
        if (CS0031.isIntegralConstant(initializer, parameterTypeRef)) {
            return null;
        }
        return newBuilder(getNameIdentifier(element), formatTypeRef(valueTypeRef), formatTypeRef(parameterTypeRef));
    }
}
