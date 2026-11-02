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
import consulo.csharp.lang.impl.psi.source.CSharpConstantExpressionImpl;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every {@code language/*.cs} must parse without errors and resolve all its references. Its marker comments state what
 * the code right after them must be:
 * <ul>
 * <li>{@code /*type:T*&#47;} - the type of the expression starting there, or of the variable named there</li>
 * <li>{@code /*value:V*&#47;} - the value of the literal starting there</li>
 * </ul>
 *
 * @author VISTALL
 */
@ExtendWith(HeadlessProjectExtension.class)
public class LanguageFeatureTest {
    private static final String DIRECTORY = "language";

    private static final Pattern MARKER = Pattern.compile("/\\*(type|value):(.+)\\*/");

    static Stream<Arguments> sources() throws Exception {
        List<String> files;
        try (Stream<Path> list = Files.list(CSharpResolveTesting.resource(DIRECTORY))) {
            files = list.map(path -> path.getFileName().toString()).filter(name -> name.endsWith(".cs")).sorted().toList();
        }

        List<Arguments> arguments = new ArrayList<>();
        for (String file : files) {
            for (MockSdk sdk : MockSdk.values()) {
                arguments.add(Arguments.of(file, sdk));
            }
        }
        return arguments.stream();
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("sources")
    public void check(String fileName, MockSdk sdk, HeadlessProjects projects) throws Exception {
        String source = Files.readString(CSharpResolveTesting.resource(DIRECTORY + "/" + fileName), StandardCharsets.UTF_8);

        PsiFile file = CSharpResolveTesting.openSource(projects, sdk, fileName, source, sdk.getLinqLibraries());

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
            assertThat(unresolved).as("unresolved references").isEmpty();

            List<String> expected = new ArrayList<>();
            List<String> actual = new ArrayList<>();
            for (PsiComment comment : PsiTreeUtil.findChildrenOfType(file, PsiComment.class)) {
                Matcher matcher = MARKER.matcher(comment.getText());
                if (!matcher.matches()) {
                    continue;
                }

                PsiElement leaf = PsiTreeUtil.nextVisibleLeaf(comment);
                assertThat(leaf).as("code after %s", comment.getText()).isNotNull();

                String kind = matcher.group(1);
                String prefix = kind + ":" + leaf.getText() + "@" + leaf.getTextOffset() + " ";
                expected.add(prefix + matcher.group(2));
                actual.add(prefix + ("type".equals(kind) ? typeAt(leaf) : valueAt(leaf)));
            }
            assertThat(actual).isEqualTo(expected);
        });
    }

    @RequiredReadAction
    private static String typeAt(PsiElement leaf) {
        DotNetVariable variable = PsiTreeUtil.getParentOfType(leaf, DotNetVariable.class);
        if (variable != null && variable.getNameIdentifier() != null && PsiTreeUtil.isAncestor(variable.getNameIdentifier(), leaf, false)) {
            return presentation(variable.toTypeRef(true));
        }

        DotNetExpression outermost = null;
        for (PsiElement element = leaf; element != null && element.getTextRange().getStartOffset() == leaf.getTextRange().getStartOffset(); element = element.getParent()) {
            if (element instanceof DotNetExpression expression) {
                outermost = expression;
            }
        }
        return outermost == null ? "<no expression>" : presentation(outermost.toTypeRef(true));
    }

    @RequiredReadAction
    private static String valueAt(PsiElement leaf) {
        CSharpConstantExpressionImpl constant = PsiTreeUtil.getParentOfType(leaf, CSharpConstantExpressionImpl.class);
        return constant == null ? "<no literal>" : Objects.toString(constant.getValue());
    }

    @RequiredReadAction
    private static String presentation(DotNetTypeRef typeRef) {
        return CSharpTypeRefPresentationUtil.buildText(typeRef, CSharpTypeRefPresentationUtil.QUALIFIED_NAME | CSharpTypeRefPresentationUtil.FORCE_TYPE_KEYWORD);
    }
}
