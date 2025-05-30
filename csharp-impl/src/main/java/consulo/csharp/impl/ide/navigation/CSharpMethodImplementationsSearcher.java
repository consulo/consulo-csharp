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
import consulo.application.ReadAction;
import consulo.csharp.lang.impl.psi.source.resolve.overrideSystem.OverrideUtil;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.language.psi.PsiElement;
import consulo.language.psi.search.DefinitionsScopedSearch;
import consulo.language.psi.search.DefinitionsScopedSearchExecutor;
import consulo.util.collection.ContainerUtil;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * @author VISTALL
 * @since 17-May-16
 */
@ExtensionImpl
public class CSharpMethodImplementationsSearcher implements DefinitionsScopedSearchExecutor {
    @Override
    public boolean execute(@Nonnull DefinitionsScopedSearch.SearchParameters queryParameters, @Nonnull Predicate<? super PsiElement> consumer) {
        PsiElement element = queryParameters.getElement();
        if (element instanceof DotNetVirtualImplementOwner) {
            Collection<DotNetVirtualImplementOwner> members = ReadAction.compute(() -> OverrideUtil.collectOverridenMembers((DotNetVirtualImplementOwner) element));
            return ContainerUtil.process(members, consumer);
        }
        return true;
    }
}
