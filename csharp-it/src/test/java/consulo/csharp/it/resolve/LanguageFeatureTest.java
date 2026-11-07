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
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CSharpCompilerChecks;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.source.CSharpConstantExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpOutRefWrapExpressionImpl;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetElement;
import consulo.dotnet.psi.DotNetAttributeUtil;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.editor.rawHighlight.HighlightInfoType;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.StringUtil;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
 * <li>{@code /*error:CS0000*&#47;} - the compiler check reporting an error there; no other check may report one</li>
 * <li>{@code /*modifier:readonly*&#47;} - a modifier of the declaration the name there resolves to</li>
 * <li>{@code /*attribute:System.ObsoleteAttribute*&#47;} - an attribute of the declaration the name there resolves to</li>
 * </ul>
 * A {@code !} before a modifier or an attribute expects the declaration to have none such.
 * <ul>
 * </ul>
 * A {@code /*unresolved*&#47;} comment before a name expects it not to resolve; a {@code // sdk:NET_10} comment limits the
 * file to the listed {@link MockSdk}s. A {@code language/<name>.dll} is a library of {@code <name>.cs}, built from
 * {@code src/test/library/<name>.cs} by {@code build.sh} there.
 *
 * @author VISTALL
 */
@ExtendWith(HeadlessProjectExtension.class)
public class LanguageFeatureTest {
    private static final String DIRECTORY = "language";

    private static final Pattern SDK_MARKER = Pattern.compile("// sdk:([A-Z0-9_,]+)");

    private static final Pattern MARKER = Pattern.compile("/\\*(type|value|error|modifier|attribute):(.+)\\*/");

    static Stream<Arguments> sources() throws Exception {
        List<String> files;
        try (Stream<Path> list = Files.list(CSharpResolveTesting.resource(DIRECTORY))) {
            files = list.map(path -> path.getFileName().toString()).filter(name -> name.endsWith(".cs")).sorted().toList();
        }

        List<Arguments> arguments = new ArrayList<>();
        for (String file : files) {
            String source = Files.readString(CSharpResolveTesting.resource(DIRECTORY + "/" + file), StandardCharsets.UTF_8);
            Matcher sdks = SDK_MARKER.matcher(source);
            for (MockSdk sdk : MockSdk.values()) {
                if (!sdks.find(0) || List.of(sdks.group(1).split(",")).contains(sdk.name())) {
                    arguments.add(Arguments.of(file, sdk));
                }
            }
        }
        return arguments.stream();
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("sources")
    public void check(String fileName, MockSdk sdk, HeadlessProjects projects) throws Exception {
        String source = Files.readString(CSharpResolveTesting.resource(DIRECTORY + "/" + fileName), StandardCharsets.UTF_8);

        List<Path> libraries = new ArrayList<>(sdk.getLinqLibraries());
        Path library = CSharpResolveTesting.resource(DIRECTORY).resolve(fileName.substring(0, fileName.length() - ".cs".length()) + ".dll");
        if (Files.exists(library)) {
            libraries.add(library);
        }

        PsiFile file = CSharpResolveTesting.openSource(projects, sdk, fileName, source, libraries);

        ReadAction.run(() -> {
            List<String> errors = new ArrayList<>();
            for (PsiErrorElement error : PsiTreeUtil.findChildrenOfType(file, PsiErrorElement.class)) {
                errors.add(error.getErrorDescription() + "@" + error.getTextOffset());
            }
            assertThat(errors).as("parse errors").isEmpty();

            // the names after /*unresolved*/ - and none else
            List<String> expectedUnresolved = new ArrayList<>();
            for (PsiComment comment : PsiTreeUtil.findChildrenOfType(file, PsiComment.class)) {
                PsiElement leaf = nextCode(comment);
                if (comment.getText().equals("/*unresolved*/") && leaf != null) {
                    expectedUnresolved.add(leaf.getText() + "@" + leaf.getTextOffset());
                }
            }
            List<String> unresolved = new ArrayList<>();
            for (CSharpReferenceExpression reference : PsiTreeUtil.findChildrenOfType(file, CSharpReferenceExpression.class)) {
                PsiElement name = reference.getReferenceElement();
                // out _ - a discard, no reference
                boolean discard = reference.isPlaceholderReference() && reference.getParent() instanceof CSharpOutRefWrapExpressionImpl;
                if (reference.resolve() == null && name != null && !discard) {
                    unresolved.add(name.getText() + "@" + name.getTextOffset());
                }
            }
            unresolved.sort(null);
            expectedUnresolved.sort(null);
            assertThat(unresolved).as("unresolved references").isEqualTo(expectedUnresolved);

            List<String> expected = new ArrayList<>();
            List<String> actual = new ArrayList<>();
            for (PsiComment comment : PsiTreeUtil.findChildrenOfType(file, PsiComment.class)) {
                Matcher matcher = MARKER.matcher(comment.getText());
                if (!matcher.matches()) {
                    continue;
                }

                PsiElement leaf = nextCode(comment);
                assertThat(leaf).as("code after %s", comment.getText()).isNotNull();

                String kind = matcher.group(1);
                String prefix = kind + ":" + leaf.getText() + "@" + leaf.getTextOffset() + " ";
                if ("error".equals(kind)) {
                    expected.add(prefix + matcher.group(2));
                    continue;
                }
                expected.add(prefix + matcher.group(2));
                actual.add(prefix + switch (kind) {
                    case "type" -> typeAt(leaf);
                    case "modifier", "attribute" -> declarationAt(leaf, kind, StringUtil.trimStart(matcher.group(2), "!"));
                    default -> valueAt(leaf);
                });
            }
            actual.addAll(compilerErrors(file));

            expected.sort(null);
            actual.sort(null);
            assertThat(actual).isEqualTo(expected);
        });
    }

    /**
     * The compiler checks reporting an error, as {@code error:<leaf>@<offset> <check>} at the start of their range
     */
    @RequiredReadAction
    private static List<String> compilerErrors(PsiFile file) {
        CSharpHighlightContext context = new CSharpHighlightContext(file);
        CSharpLanguageVersion languageVersion = context.getLanguageVersion();

        List<String> errors = new ArrayList<>();
        for (DotNetElement element : PsiTreeUtil.findChildrenOfType(file, DotNetElement.class)) {
            for (CSharpCompilerChecks check : CSharpCompilerChecks.VALUES) {
                if (languageVersion.ordinal() < check.getLanguageVersion().ordinal() || !check.getTargetClass().isAssignableFrom(element.getClass())) {
                    continue;
                }

                for (CompilerCheck.HighlightInfoFactory result : check.check(languageVersion, context, element)) {
                    if (result instanceof CompilerCheck.CompilerCheckBuilder builder && builder.getHighlightInfoType() == HighlightInfoType.ERROR) {
                        PsiElement leaf = file.findElementAt(builder.getTextRange().getStartOffset());
                        errors.add("error:" + (leaf == null ? "" : leaf.getText()) + "@" + builder.getTextRange().getStartOffset() + " " + check.name());
                    }
                }
            }
        }
        return errors;
    }

    /**
     * The code after a marker - past the other markers next to it
     */
    @Nullable
    private static PsiElement nextCode(PsiComment comment) {
        PsiElement leaf = PsiTreeUtil.nextVisibleLeaf(comment);
        while (leaf instanceof PsiComment) {
            leaf = PsiTreeUtil.nextVisibleLeaf(leaf);
        }
        return leaf;
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

    /**
     * The modifier or the attribute, if the declaration the name resolves to has it - else {@code !name}
     */
    @RequiredReadAction
    private static String declarationAt(PsiElement leaf, String kind, String name) {
        CSharpReferenceExpression reference = PsiTreeUtil.getParentOfType(leaf, CSharpReferenceExpression.class);
        if (reference == null || reference.getReferenceElement() != leaf) {
            return "<no reference>";
        }
        PsiElement resolved = reference.resolve();
        if (!(resolved instanceof DotNetModifierListOwner owner)) {
            return "<no declaration>";
        }
        boolean has = "modifier".equals(kind)
            ? owner.hasModifier(CSharpModifier.valueOf(name.toUpperCase(Locale.ROOT)))
            : DotNetAttributeUtil.hasAttribute(owner, name);
        return has ? name : "!" + name;
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
