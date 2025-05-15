/*
 * Copyright 2013-2017 consulo.io
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

package consulo.csharp.lang.impl.psi.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.progress.ProgressManager;
import consulo.application.util.RecursionManager;
import consulo.application.util.function.Processor;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 29.09.14
 */
public class CSharpTypeResolveContext extends CSharpBaseResolveContext<CSharpTypeDeclaration> {
    @RequiredReadAction
    public CSharpTypeResolveContext(@Nonnull CSharpTypeDeclaration element,
                                    @Nonnull DotNetGenericExtractor genericExtractor,
                                    @Nullable Set<String> recursiveGuardSet) {
        super(element, genericExtractor, recursiveGuardSet);
    }

    @Override
    public void acceptChildren(CSharpElementVisitor visitor) {
        for (DotNetNamedElement element : myElement.getMembers()) {
            ProgressManager.checkCanceled();

            element.accept(visitor);
        }
    }

    @RequiredReadAction
    @Override
    public boolean processExtensionMethodGroups(@Nonnull Processor<CSharpMethodDeclaration> processor) {
        for (DotNetNamedElement element : myElement.getMembers()) {
            if (element instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) element).isExtension()) {
                if (!processor.process((CSharpMethodDeclaration) element)) {
                    return false;
                }
            }
        }
        return true;
    }

    @RequiredReadAction
    @Nonnull
    @Override
    protected List<DotNetTypeRef> getExtendTypeRefs() {
        DotNetTypeRef[] typeRefs = myElement.getExtendTypeRefs();
        List<DotNetTypeRef> extendTypeRefs = new ArrayList<>(typeRefs.length);

        for (DotNetTypeRef typeRef : typeRefs) {
            DotNetTypeRef ref = RecursionManager.doPreventingRecursion(this, false, () -> GenericUnwrapTool.exchangeTypeRef(typeRef, myExtractor));
            if (ref == null) {
                continue;
            }
            extendTypeRefs.add(ref);
        }
        return extendTypeRefs;
    }
}
