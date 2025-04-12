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
import consulo.application.util.function.Processor;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.search.ReferencesSearch;
import consulo.language.psi.search.ReferencesSearchQueryExecutor;
import consulo.project.util.query.QueryExecutorBase;
import jakarta.annotation.Nonnull;

import java.util.function.Predicate;

/**
 * @author VISTALL
 * @since 27.10.14
 */
@ExtensionImpl
public class AdditionalReferenceSearch extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> implements ReferencesSearchQueryExecutor {
    @Override
    public void processQuery(@Nonnull ReferencesSearch.SearchParameters queryParameters, @Nonnull Predicate<? super PsiReference> consumer) {
        PsiElement elementToSearch = queryParameters.getElementToSearch();

        PsiElement declaration = elementToSearch.getUserData(CSharpResolveUtil.EXTENSION_METHOD_WRAPPER);
        if (declaration == null) {
            declaration = elementToSearch.getUserData(CSharpResolveUtil.ACCESSOR_VALUE_VARIABLE_OWNER);
        }

        if (declaration == null) {
            return;
        }

        ReferencesSearch.search(declaration, queryParameters.getEffectiveSearchScope(), queryParameters.isIgnoreAccessScope()).forEach(consumer);
    }
}
