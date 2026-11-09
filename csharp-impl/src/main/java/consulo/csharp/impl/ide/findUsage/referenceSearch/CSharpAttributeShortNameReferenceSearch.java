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
package consulo.csharp.impl.ide.findUsage.referenceSearch;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.AccessRule;
import consulo.csharp.lang.impl.psi.resolve.additionalMembersImpl.StructOrGenericParameterConstructorProvider;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.search.ReferencesSearch;
import consulo.language.psi.search.ReferencesSearchQueryExecutor;
import consulo.project.util.query.QueryExecutorBase;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

/**
 * An attribute class FooAttribute is used as [Foo] - its name without the suffix is searched too, for the type and its
 * constructors (an attribute is a constructor call)
 *
 * @author VISTALL
 */
@ExtensionImpl
public class CSharpAttributeShortNameReferenceSearch extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> implements ReferencesSearchQueryExecutor {
    private static final String SUFFIX = "Attribute";

    @Override
    public void processQuery(ReferencesSearch.SearchParameters queryParameters, Predicate<? super PsiReference> consumer) {
        if (!(queryParameters.getElementToSearch() instanceof CSharpTypeDeclaration type)) {
            return;
        }
        String shortName = AccessRule.read(() -> shortName(type));
        if (shortName == null) {
            return;
        }
        queryParameters.getOptimizer().searchWord(shortName, queryParameters.getEffectiveSearchScope(), true, type);
        for (DotNetNamedElement member : AccessRule.read(type::getMembers)) {
            if (member instanceof CSharpConstructorDeclaration) {
                queryParameters.getOptimizer().searchWord(shortName, queryParameters.getEffectiveSearchScope(), true, member);
            }
        }
        // as CSharpConstructorPlusTypeReferenceSearch - the default constructor
        PsiElement constructor = AccessRule.read(() -> StructOrGenericParameterConstructorProvider.buildDefaultConstructor(type, type.getName()));
        queryParameters.getOptimizer().searchWord(shortName, queryParameters.getEffectiveSearchScope(), true, constructor);
    }

    /**
     * Foo of an attribute class FooAttribute - null for another type
     */
    public static @Nullable String shortName(CSharpTypeDeclaration type) {
        String name = type.getName();
        if (name == null || name.length() <= SUFFIX.length() || !name.endsWith(SUFFIX) || !type.isInheritor(DotNetTypes.System.Attribute, true)) {
            return null;
        }
        return name.substring(0, name.length() - SUFFIX.length());
    }
}
