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
package consulo.csharp.lang.impl.psi.interop;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.impl.psi.ToNativeElementTransformer;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.msil.lang.psi.MsilEntry;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * Types of other .NET languages (F#, Visual Basic) as C# types: a language takes part by its {@code DotNetPsiSearcherExtension},
 * which finds its types for C#. C# and MSIL elements are not touched (MSIL has own transformer).
 *
 * @author VISTALL
 * @since 2026-10-25
 */
@ExtensionImpl
public class InteropToNativeElementTransformer implements ToNativeElementTransformer {
    @RequiredReadAction
    @Override
    public @Nullable PsiElement transform(PsiElement element) {
        if (!(element instanceof DotNetTypeDeclaration type) || element instanceof CSharpTypeDeclaration || element instanceof MsilEntry) {
            return null;
        }
        return InteropToCSharpUtil.wrap(type);
    }
}
