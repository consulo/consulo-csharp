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

package consulo.csharp.lang.impl.psi.search;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationManager;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.ProgressIndicatorProvider;
import consulo.content.scope.SearchScope;
import consulo.csharp.lang.impl.psi.CSharpRecursiveElementVisitor;
import consulo.csharp.lang.impl.psi.stub.index.TypeIndex;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.search.searches.AllTypesSearch;
import consulo.dotnet.psi.search.searches.AllTypesSearchExecutor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.scope.LocalSearchScope;
import jakarta.annotation.Nonnull;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author max
 * <p/>
 * Copied from Java plugin by Jetbrains (com.intellij.psi.search.searches.ClassInheritorsSearch)
 */
@ExtensionImpl
public class CSharpAllTypesSearchExecutor implements AllTypesSearchExecutor {
    @Override
    public boolean execute(@Nonnull final AllTypesSearch.SearchParameters queryParameters, @Nonnull final Predicate<? super DotNetTypeDeclaration> consumer) {
        SearchScope scope = queryParameters.getScope();

        if (scope instanceof GlobalSearchScope) {
            return processAllClassesInGlobalScope((GlobalSearchScope) scope, consumer, queryParameters);
        }

        PsiElement[] scopeRoots = ((LocalSearchScope) scope).getScope();
        for (final PsiElement scopeRoot : scopeRoots) {
            if (!processScopeRootForAllClasses(scopeRoot, consumer)) {
                return false;
            }
        }
        return true;
    }

    private static boolean processAllClassesInGlobalScope(final GlobalSearchScope scope,
                                                          final Predicate<? super DotNetTypeDeclaration> processor,
                                                          final AllTypesSearch.SearchParameters parameters) {
        final Collection<String> names = ApplicationManager.getApplication().runReadAction((Supplier<Collection<String>>) () -> TypeIndex.getInstance().getAllKeys(parameters.getProject()));

        final ProgressIndicator indicator = ProgressIndicatorProvider.getGlobalProgressIndicator();
        if (indicator != null) {
            indicator.checkCanceled();
        }

        List<String> sorted = new ArrayList<String>(names.size());
        int i = 0;
        for (String name : names) {
            if (parameters.nameMatches(name)) {
                sorted.add(name);
            }
            if (indicator != null && i % 512 == 0) {
                indicator.checkCanceled();
            }

            i++;
        }

        if (indicator != null) {
            indicator.checkCanceled();
        }

        Collections.sort(sorted, new Comparator<String>() {
            @Override
            public int compare(final String o1, final String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });

        for (final String name : sorted) {
            ProgressIndicatorProvider.checkCanceled();
            final Collection<CSharpTypeDeclaration> classes = ApplicationManager.getApplication().runReadAction((Supplier<Collection<CSharpTypeDeclaration>>) () -> TypeIndex.getInstance().get(name, parameters.getProject(), scope));
            for (DotNetTypeDeclaration psiClass : classes) {
                ProgressIndicatorProvider.checkCanceled();
                if (!processor.test(psiClass)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean processScopeRootForAllClasses(PsiElement scopeRoot, final Predicate<? super DotNetTypeDeclaration> processor) {
        if (scopeRoot == null) {
            return true;
        }
        final boolean[] stopped = new boolean[]{false};

        scopeRoot.accept(new CSharpRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if (!stopped[0]) {
                    super.visitElement(element);
                }
            }

            @Override
            public void visitTypeDeclaration(CSharpTypeDeclaration aClass) {
                stopped[0] = !processor.test(aClass);
                super.visitTypeDeclaration(aClass);
            }
        });

        return !stopped[0];
    }
}
