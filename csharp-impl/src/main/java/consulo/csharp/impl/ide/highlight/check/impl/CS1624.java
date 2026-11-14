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
import consulo.csharp.lang.impl.psi.source.CSharpAnonymousMethodExpression;
import consulo.csharp.lang.impl.psi.source.CSharpLocalMethodDeclarationImpl;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import org.jspecify.annotations.Nullable;

/**
 * {@code int M() { yield return 1; }} - the body of an iterator must return an iterator interface type
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS1624 extends CompilerCheck<CSharpSimpleLikeMethodAsElement> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpSimpleLikeMethodAsElement element) {
        // the methods and the local functions - an accessor is a part of its property
        if (!(element instanceof DotNetLikeMethodDeclaration || element instanceof CSharpLocalMethodDeclarationImpl) || element instanceof CSharpAnonymousMethodExpression) {
            return null;
        }
        if (!CS0127.isIterator(element)) {
            return null;
        }
        DotNetTypeRef returnTypeRef = element.getReturnTypeRef();
        String vmQName = IteratorCheckUtil.getVmQName(returnTypeRef);
        if (vmQName != null && (IteratorCheckUtil.ITERATOR_INTERFACES.contains(vmQName) || IteratorCheckUtil.ASYNC_ITERATOR_INTERFACES.contains(vmQName))) {
            return null;
        }
        return newBuilder(getNameIdentifier(element), formatElement(element), formatTypeRef(returnTypeRef));
    }
}
