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

package consulo.csharp.it.controlFlow;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.ReadAction;
import consulo.csharp.it.resolve.CSharpResolveTesting;
import consulo.csharp.it.resolve.MockSdk;
import consulo.csharp.lang.impl.psi.controlFlow.CSharpControlFlow;
import consulo.csharp.lang.impl.psi.controlFlow.CSharpInstruction;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.controlFlow.Instruction;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The graphs of the flow owners of every {@code flow/<name>.cs}, in the order of the file, against {@code flow/<name>.txt}:
 * a header per owner, then an instruction per line as {@code num(successors) element}, unreachable ones marked with
 * {@code !}. A missing {@code .txt} is written to {@code src/test/resources/flow} and the test fails.
 *
 * @author VISTALL
 */
@ExtendWith(HeadlessProjectExtension.class)
public class ControlFlowTest {
    private static final String DIRECTORY = "flow";

    static Stream<String> sources() throws Exception {
        try (Stream<Path> list = Files.list(resource(DIRECTORY))) {
            return list.map(path -> path.getFileName().toString()).filter(name -> name.endsWith(".cs")).sorted().toList().stream();
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("sources")
    public void check(String fileName, HeadlessProjects projects) throws Exception {
        String source = Files.readString(resource(DIRECTORY + "/" + fileName), StandardCharsets.UTF_8);
        MockSdk sdk = MockSdk.NET_10;
        PsiFile file = CSharpResolveTesting.openSource(projects, sdk, fileName, source, sdk.getLinqLibraries());

        String actual = ReadAction.compute(() -> {
            assertThat(PsiTreeUtil.findChildrenOfType(file, PsiErrorElement.class)).as("parse errors").isEmpty();
            return dump(file);
        });

        String expectedName = fileName.substring(0, fileName.length() - ".cs".length()) + ".txt";
        URL expectedUrl = ControlFlowTest.class.getClassLoader().getResource(DIRECTORY + "/" + expectedName);
        if (expectedUrl == null) {
            Path path = Path.of("src/test/resources", DIRECTORY, expectedName);
            Files.writeString(path, actual, StandardCharsets.UTF_8);
            throw new AssertionError("written " + path.toAbsolutePath());
        }
        String expected = Files.readString(Path.of(expectedUrl.toURI()), StandardCharsets.UTF_8).replace("\r\n", "\n");
        assertThat(actual).isEqualTo(expected);
    }

    @RequiredReadAction
    private static String dump(PsiFile file) {
        StringBuilder builder = new StringBuilder();
        List<CSharpSimpleLikeMethodAsElement> owners = List.copyOf(PsiTreeUtil.findChildrenOfType(file, CSharpSimpleLikeMethodAsElement.class));
        for (CSharpSimpleLikeMethodAsElement owner : owners) {
            CSharpControlFlow flow = CSharpControlFlow.of(owner);
            if (flow == null) {
                continue;
            }
            builder.append("# ").append(header(owner)).append('\n');
            for (Instruction instruction : flow.getInstructions()) {
                builder.append(flow.isReachable(instruction) ? "  " : "! ").append(instruction).append('\n');
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    @RequiredReadAction
    private static String header(CSharpSimpleLikeMethodAsElement owner) {
        String text = CSharpInstruction.presentation(owner);
        int brace = text.indexOf('{');
        return brace > 0 ? text.substring(0, brace).trim() : text;
    }

    private static Path resource(String path) throws Exception {
        URL url = ControlFlowTest.class.getClassLoader().getResource(path);
        assertThat(url).as(path).isNotNull();
        return Path.of(url.toURI());
    }
}
