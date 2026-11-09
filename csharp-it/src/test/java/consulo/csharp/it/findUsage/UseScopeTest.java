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
package consulo.csharp.it.findUsage;

import consulo.application.ReadAction;
import consulo.content.scope.SearchScope;
import consulo.csharp.it.resolve.CSharpResolveTesting;
import consulo.csharp.it.resolve.MockSdk;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiReference;
import consulo.language.psi.scope.LocalSearchScope;
import consulo.language.psi.search.ReferencesSearch;
import consulo.language.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The use scope by accessibility: a private member is searched in its type - all its parts - and not beyond; a
 * public one, a field and an explicit implementation keep the module scope
 *
 * @author VISTALL
 */
@ExtendWith(HeadlessProjectExtension.class)
public class UseScopeTest {
    private static final String SOURCE = """
        interface I { void Explicit(); }

        partial class Parts : I
        {
            private void Private() { }
            void DefaultPrivate() { }
            public void Public() { }
            private int field;
            private class Nested { }
            void I.Explicit() { }

            void Run() { Private(); DefaultPrivate(); Public(); field = 1; new Nested(); }
        }

        partial class Parts
        {
            void Other() { Private(); }
        }

        class User
        {
            void Run(Parts parts) { parts.Public(); }
        }
        """;

    @Test
    public void scopes(HeadlessProjects projects) throws Exception {
        PsiFile file = CSharpResolveTesting.openSource(projects, MockSdk.NET_10, "Parts.cs", SOURCE, List.of());

        ReadAction.run(() -> {
            assertThat(scope(file, "Private")).isInstanceOf(LocalSearchScope.class);
            assertThat(scope(file, "DefaultPrivate")).isInstanceOf(LocalSearchScope.class);
            assertThat(scope(file, "Nested")).isInstanceOf(LocalSearchScope.class);
            assertThat(scope(file, "Public")).isNotInstanceOf(LocalSearchScope.class);
            assertThat(scope(file, "field")).isNotInstanceOf(LocalSearchScope.class);
            assertThat(scope(file, "Explicit")).isNotInstanceOf(LocalSearchScope.class);

            // both parts
            assertThat(((LocalSearchScope) scope(file, "Private")).getScope()).hasSize(2);
        });

        // in Run and in the other part
        assertThat(usages(file, "Private")).containsExactly("Private@" + offset(file, "Private(); Default"), "Private@" + offset(file, "Private(); }\n}"));
        // in Run and in User
        assertThat(usages(file, "Public")).hasSize(2);
    }

    private static SearchScope scope(PsiFile file, String name) {
        return declaration(file, name).getUseScope();
    }

    private static DotNetNamedElement declaration(PsiFile file, String name) {
        for (DotNetNamedElement element : PsiTreeUtil.findChildrenOfType(file, DotNetNamedElement.class)) {
            if (name.equals(element.getName()) && !(element.getParent() instanceof PsiFile)) {
                return element;
            }
        }
        throw new AssertionError(name);
    }

    private static List<String> usages(PsiFile file, String name) {
        DotNetNamedElement declaration = ReadAction.compute(() -> declaration(file, name));
        List<String> result = new ArrayList<>();
        for (PsiReference reference : ReferencesSearch.search(declaration).findAll()) {
            ReadAction.run(() -> result.add(reference.getElement().getText() + "@" + reference.getElement().getTextOffset()));
        }
        result.sort(null);
        return result;
    }

    private static int offset(PsiFile file, String text) {
        return ReadAction.compute(() -> file.getText().indexOf(text));
    }
}
