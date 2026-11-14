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
import consulo.csharp.lang.impl.psi.source.CSharpForeachStatementImpl;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpSearchUtil;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import org.jspecify.annotations.Nullable;

/**
 * {@code foreach (var x in i)} of an int - the type has no GetEnumerator
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS1579 extends CompilerCheck<CSharpForeachStatementImpl> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpForeachStatementImpl element) {
        DotNetExpression iterable = element.getIterableExpression();
        if (iterable == null) {
            return null;
        }
        DotNetTypeRef typeRef = iterable.toTypeRef(false);
        // a value type with no GetEnumerator - the other types are by their interfaces, an extension GetEnumerator is not looked at
        if (!(typeRef.resolve().getElement() instanceof DotNetTypeDeclaration type) || !(type.isStruct() || type.isEnum())) {
            return null;
        }
        if (CSharpSearchUtil.findMethodByName("GetEnumerator", null, typeRef, 0) != null) {
            return null;
        }
        return newBuilder(iterable, formatTypeRef(typeRef));
    }
}
