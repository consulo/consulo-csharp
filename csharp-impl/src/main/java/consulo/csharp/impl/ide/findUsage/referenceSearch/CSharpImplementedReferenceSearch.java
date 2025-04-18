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

package consulo.csharp.impl.ide.findUsage.referenceSearch;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationManager;
import consulo.csharp.lang.impl.psi.source.resolve.overrideSystem.OverrideUtil;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.search.ReferencesSearch;
import consulo.language.psi.search.ReferencesSearchQueryExecutor;
import consulo.project.util.query.QueryExecutorBase;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 17-May-16
 */
@ExtensionImpl
public class CSharpImplementedReferenceSearch extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> implements ReferencesSearchQueryExecutor {
    @Override
    public void processQuery(@Nonnull ReferencesSearch.SearchParameters queryParameters, @Nonnull Predicate<? super PsiReference> consumer) {
        final PsiElement elementToSearch = queryParameters.getElementToSearch();
        if (elementToSearch instanceof CSharpMethodDeclaration) {
            Collection<DotNetVirtualImplementOwner> targets = ApplicationManager.getApplication().runReadAction((Supplier<Collection<DotNetVirtualImplementOwner>>) () -> {
                if (((CSharpMethodDeclaration) elementToSearch).hasModifier(DotNetModifier.ABSTRACT)) {
                    return OverrideUtil.collectOverridenMembers((DotNetVirtualImplementOwner) elementToSearch);
                }
                return Collections.emptyList();
            });

            for (DotNetVirtualImplementOwner target : targets) {
                if (!ReferencesSearch.search(target, queryParameters.getEffectiveSearchScope()).forEach(consumer)) {
                    return;
                }
            }
        }
    }
}
