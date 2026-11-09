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

package consulo.csharp.lang.impl.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.progress.ProgressManager;
import consulo.application.util.CachedValueProvider;
import consulo.csharp.lang.doc.psi.CSharpDocRoot;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.language.psi.*;
import consulo.language.psi.util.LanguageCachedValueUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * The declarations the references of a file resolve to - what is used in the file; a reference of a doc comment
 * (cref) is not a use, a method group (nameof) is a use of all its methods. Computed by one walk over the file, whose
 * resolves the highlighting of the file has mostly cached already; a declaration is in the set as its original element
 * (not a generic or extension wrapper)
 *
 * @author VISTALL
 */
public final class CSharpFileUsages {
    private CSharpFileUsages() {
    }

    @RequiredReadAction
    public static boolean isUsed(PsiElement declaration) {
        PsiFile file = declaration.getContainingFile();
        return file != null && of(file).contains(declaration);
    }

    @RequiredReadAction
    public static Set<PsiElement> of(PsiFile file) {
        return LanguageCachedValueUtil.getCachedValue(file, () -> CachedValueProvider.Result.create(compute(file), PsiModificationTracker.MODIFICATION_COUNT));
    }

    @RequiredReadAction
    private static Set<PsiElement> compute(PsiFile file) {
        Set<PsiElement> used = new HashSet<>();
        file.accept(new PsiRecursiveElementWalkingVisitor() {
            @Override
            @RequiredReadAction
            public void visitElement(PsiElement element) {
                if (element instanceof CSharpDocRoot) {
                    return;
                }
                if (element instanceof PsiPolyVariantReference reference) {
                    ProgressManager.checkCanceled();
                    ResolveResult[] results = reference.multiResolve(false);
                    if (results.length == 0) {
                        results = reference.multiResolve(true);
                    }
                    for (ResolveResult result : results) {
                        PsiElement target = result.getElement();
                        // a method group - nameof(M)
                        if (target instanceof CSharpElementGroup<?> group) {
                            for (PsiElement groupElement : group.getElements()) {
                                used.add(original(groupElement));
                            }
                        }
                        else if (target != null) {
                            used.add(original(target));
                        }
                    }
                }
                super.visitElement(element);
            }
        });
        return used;
    }

    @RequiredReadAction
    private static PsiElement original(PsiElement target) {
        CSharpMethodDeclaration extension = target.getUserData(CSharpResolveUtil.EXTENSION_METHOD_WRAPPER);
        PsiElement element = extension != null ? extension : target;
        PsiElement original = element.getOriginalElement();
        return original != null ? original : element;
    }
}
