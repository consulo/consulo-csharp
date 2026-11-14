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
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import org.jspecify.annotations.Nullable;

/**
 * {@code operator <<(int a, C b)} - the first operand of a shift operator must be the containing type
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0564 extends CompilerCheck<CSharpMethodDeclaration> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpMethodDeclaration element) {
        IElementType operator = OperatorCheckUtil.getStaticOperator(element);
        DotNetTypeRef[] parameters = element.getParameterTypeRefs();
        if (!OperatorCheckUtil.SHIFTS.contains(operator) || parameters.length != 2) {
            return null;
        }
        boolean first = OperatorCheckUtil.isContainingType(parameters[0], (DotNetTypeDeclaration) element.getParent());
        // before C# 11 the second operand is int
        boolean second = languageVersion.isAtLeast(CSharpLanguageVersion._11_0) || DotNetTypeRefUtil.isVmQNameEqual(parameters[1], DotNetTypes.System.Int32);
        if (first && second) {
            return null;
        }
        return newBuilder(getNameIdentifier(element));
    }
}
