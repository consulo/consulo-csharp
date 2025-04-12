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

package consulo.csharp.impl.ide.navigation;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.impl.psi.msil.CSharpTransform;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.search.searches.TypeInheritorsSearch;
import consulo.language.psi.PsiElement;
import consulo.language.psi.search.DefinitionsScopedSearch;
import consulo.language.psi.search.DefinitionsScopedSearchExecutor;
import jakarta.annotation.Nonnull;

import java.util.function.Predicate;

/**
 * @author VISTALL
 * @since 17-May-16
 */
@ExtensionImpl
public class CSharpTypeImplementationSearcher implements DefinitionsScopedSearchExecutor {
    @Override
    public boolean execute(@Nonnull DefinitionsScopedSearch.SearchParameters queryParameters, @Nonnull final Predicate<? super PsiElement> consumer) {
        final PsiElement element = queryParameters.getElement();
        if (element instanceof DotNetTypeDeclaration) {
            return TypeInheritorsSearch.search((DotNetTypeDeclaration) element, queryParameters.getScope(), queryParameters.isCheckDeep(), true, CSharpTransform.INSTANCE).forEach(consumer);
        }
        return true;
    }
}
