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
package consulo.csharp.it.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.ReadAction;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.dotnet.psi.DotNetVariable;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Deconstruction {@code var (a, b) = x}, the variables of recursive patterns and C# 14 extension blocks resolve and are
 * typed.
 *
 * @author VISTALL
 * @since 2026-11-14
 */
@ExtendWith(HeadlessProjectExtension.class)
public class ModernSyntaxResolveTest {
    private static final String SOURCE = """
        using System.Collections.Generic;

        namespace Cs.Modern
        {
            public class Point
            {
                public int X;
                public int Y { get; set; }
                public void Deconstruct(out int x, out int y) { x = X; y = Y; }
            }

            public static class Ext
            {
                extension(string s)
                {
                    public int Len => s.Length;
                    public string Twice() => s + s;
                }

                extension<T>(List<T> l)
                {
                    public T FirstOr(T fallback) => l.Count > 0 ? l[0] : fallback;
                }
            }

            public static class Use
            {
                public static void Run(object o, int[] arr, (int, string) pair)
                {
                    var (a, b) = (1, "x");
                    var (px, py) = new Point();
                    (int c, var d) = (1, "y");
                    foreach (var (k, v) in new (string, long)[0]) { }

                    if (pair is (1, var second)) { }
                    if (o is Point(var dx, _) pt) { }
                    if (o is Point { Y: var vy }) { }
                    if (arr is [var first, .. var rest] whole) { }
                    switch (o)
                    {
                        case Point(var cx, _): break;
                    }

                    var n = "x".Len;
                    var t = "x".Twice();
                    var f = new List<long>().FirstOr(0);
                    var u = Ext.Twice("x");
                }
            }
        }
        """;

    @ParameterizedTest
    @EnumSource(MockSdk.class)
    public void resolves(MockSdk sdk, HeadlessProjects projects) throws Exception {
        PsiFile file = CSharpResolveTesting.openSource(projects, sdk, "Modern.cs", SOURCE, sdk.getLinqLibraries());

        ReadAction.run(() -> {
            List<String> errors = new ArrayList<>();
            for (PsiErrorElement error : PsiTreeUtil.findChildrenOfType(file, PsiErrorElement.class)) {
                errors.add(error.getErrorDescription() + "@" + error.getTextOffset());
            }
            assertThat(errors).as("parse errors").isEmpty();

            List<String> unresolved = new ArrayList<>();
            for (CSharpReferenceExpression reference : PsiTreeUtil.findChildrenOfType(file, CSharpReferenceExpression.class)) {
                if (reference.resolve() == null) {
                    unresolved.add(reference.getText() + "@" + reference.getTextOffset());
                }
            }
            assertThat(unresolved).as("unresolved references on %s", sdk).isEmpty();

            assertThat(variableType(file, "a")).isEqualTo("int");
            assertThat(variableType(file, "b")).isEqualTo("string");
            assertThat(variableType(file, "px")).isEqualTo("int");
            assertThat(variableType(file, "d")).isEqualTo("string");
            assertThat(variableType(file, "k")).isEqualTo("string");
            assertThat(variableType(file, "v")).isEqualTo("long");

            assertThat(variableType(file, "second")).isEqualTo("string");
            assertThat(variableType(file, "dx")).isEqualTo("int");
            assertThat(variableType(file, "pt")).isEqualTo("Cs.Modern.Point");
            assertThat(variableType(file, "vy")).isEqualTo("int");
            assertThat(variableType(file, "first")).isEqualTo("int");
            assertThat(variableType(file, "rest")).isEqualTo("int[]");
            assertThat(variableType(file, "whole")).isEqualTo("int[]");
            assertThat(variableType(file, "cx")).isEqualTo("int");

            assertThat(variableType(file, "n")).isEqualTo("int");
            assertThat(variableType(file, "t")).isEqualTo("string");
            assertThat(variableType(file, "f")).isEqualTo("long");
            assertThat(variableType(file, "u")).isEqualTo("string");
        });
    }

    @RequiredReadAction
    private static String variableType(PsiFile file, String name) {
        for (DotNetVariable variable : PsiTreeUtil.findChildrenOfType(file, DotNetVariable.class)) {
            if (name.equals(variable.getName())) {
                return CSharpTypeRefPresentationUtil.buildText(variable.toTypeRef(true), CSharpTypeRefPresentationUtil.QUALIFIED_NAME | CSharpTypeRefPresentationUtil.FORCE_TYPE_KEYWORD);
            }
        }
        throw new AssertionError("no variable " + name);
    }
}
