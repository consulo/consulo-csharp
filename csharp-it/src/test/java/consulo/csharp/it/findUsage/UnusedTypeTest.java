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
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CSharpCompilerChecks;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.it.resolve.CSharpResolveTesting;
import consulo.csharp.it.resolve.MockSdk;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiReference;
import consulo.language.psi.search.ReferencesSearch;
import consulo.language.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CC0009 over files: a type used only by another file is used - the word index and a search find it; [Foo] is a use
 * of FooAttribute, for the search of Find Usages too
 *
 * @author VISTALL
 */
@ExtendWith(HeadlessProjectExtension.class)
public class UnusedTypeTest {
    private static final String TYPES = """
        using System;

        internal class UsedElsewhere { }

        internal class UnusedAnywhere { }

        internal class FooAttribute : Attribute { }

        internal class Constructed
        {
            public Constructed(int value) { }
        }
        """;

    private static final String USER = """
        public class User
        {
            private UsedElsewhere field;

            [Foo]
            public object Run() => new Constructed(1);

            public object Field() => field;
        }
        """;

    @Test
    public void otherFile(HeadlessProjects projects) throws Exception {
        Map<String, PsiFile> files = CSharpResolveTesting.openSources(projects, MockSdk.NET_10, Map.of("Types.cs", TYPES, "User.cs", USER), List.of());
        PsiFile types = files.get("Types.cs");

        ReadAction.run(() -> {
            CSharpHighlightContext context = new CSharpHighlightContext(types);
            List<String> unused = new ArrayList<>();
            for (CSharpTypeDeclaration type : PsiTreeUtil.findChildrenOfType(types, CSharpTypeDeclaration.class)) {
                for (CompilerCheck.HighlightInfoFactory result : CSharpCompilerChecks.CC0009.check(context.getLanguageVersion(), context, type)) {
                    unused.add(type.getName());
                }
            }
            assertThat(unused).containsExactly("UnusedAnywhere");
        });

        CSharpTypeDeclaration attribute = ReadAction.compute(() -> {
            for (CSharpTypeDeclaration type : PsiTreeUtil.findChildrenOfType(types, CSharpTypeDeclaration.class)) {
                if ("FooAttribute".equals(type.getName())) {
                    return type;
                }
            }
            throw new AssertionError();
        });
        List<String> usages = new ArrayList<>();
        for (PsiReference reference : ReferencesSearch.search(attribute).findAll()) {
            ReadAction.run(() -> usages.add(reference.getElement().getText() + "@" + reference.getElement().getContainingFile().getName()));
        }
        assertThat(usages).containsExactly("Foo@User.cs");
    }
}
