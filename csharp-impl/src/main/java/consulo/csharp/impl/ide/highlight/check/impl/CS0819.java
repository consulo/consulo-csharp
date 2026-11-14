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
import consulo.csharp.lang.impl.psi.source.CSharpLocalVariableDeclarationStatementImpl;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.psi.CSharpNativeType;
import consulo.csharp.lang.psi.CSharpSoftTokens;
import consulo.dotnet.psi.DotNetType;
import org.jspecify.annotations.Nullable;

/**
 * {@code var a = 1, b = 2;} - implicitly typed variables cannot have multiple declarators
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0819 extends CompilerCheck<CSharpLocalVariableDeclarationStatementImpl> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpLocalVariableDeclarationStatementImpl element) {
        CSharpLocalVariable[] variables = element.getVariables();
        if (variables.length < 2) {
            return null;
        }
        DotNetType type = variables[0].getType();
        if (!(type instanceof CSharpNativeType nativeType) || nativeType.getTypeElementType() != CSharpSoftTokens.VAR_KEYWORD) {
            return null;
        }
        return newBuilder(type);
    }
}
