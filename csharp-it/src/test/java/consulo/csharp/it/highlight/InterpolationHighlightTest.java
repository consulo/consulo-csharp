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

package consulo.csharp.it.highlight;

import consulo.application.ReadAction;
import consulo.application.WriteAction;
import consulo.undoRedo.CommandProcessor;
import consulo.codeEditor.EditorFactory;
import consulo.codeEditor.HighlighterClient;
import consulo.codeEditor.HighlighterIterator;
import consulo.colorScheme.EditorColorsManager;
import consulo.csharp.impl.ide.highlight.CSharpEditorHighlighter;
import consulo.document.Document;
import consulo.language.editor.highlight.LexerEditorHighlighter;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.project.Project;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Typing an interpolated string keeps the editor colors right: after each typed char the tokens of the incrementally
 * updated highlighter are the ones of a highlighter of the whole text (issue #568).
 *
 * @author VISTALL
 */
@ExtendWith(HeadlessProjectExtension.class)
public class InterpolationHighlightTest {
    private static final String PREFIX = "class A\n{\n    void M(int x)\n    {\n        var s = ";
    private static final String SUFFIX = ";\n        var t = \"plain\";\n    }\n}\n";

    @ParameterizedTest
    @ValueSource(strings = {"$\"a {x} b\"", "$\"{x + 1} and {M(x)}\"", "$@\"path {x}\\file\"", "$\"{{escaped}} {x}\""})
    public void typing(String typed, HeadlessProjects projects) throws Exception {
        typing(typed, projects, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"$\"a {x} b\"", "$\"{x + 1} and {M(x)}\"", "$@\"path {x}\\file\"", "$\"{{escaped}} {x}\""})
    public void typingMainLexer(String typed, HeadlessProjects projects) throws Exception {
        typing(typed, projects, false);
    }

    private void typing(String typed, HeadlessProjects projects, boolean layered) throws Exception {
        LAYERED = layered;
        Project project = projects.open(projects.newDirectory());
        Document document = ReadAction.compute(() -> EditorFactory.getInstance().createDocument(PREFIX + SUFFIX));

        LexerEditorHighlighter incremental = create();
        incremental.setEditor(client(project, document));
        incremental.setText(document.getImmutableCharSequence());
        document.addDocumentListener(incremental);

        int offset = PREFIX.length();
        for (int i = 0; i < typed.length(); i++) {
            int at = offset + i;
            String c = String.valueOf(typed.charAt(i));
            CommandProcessor.getInstance().executeCommand(() -> WriteAction.run(() -> document.insertString(at, c)), "type", null);

            assertSame(incremental, document, "after typing " + typed.substring(0, i + 1));
        }

        // and backspace it char by char
        for (int i = typed.length() - 1; i >= 0; i--) {
            int at = offset + i;
            CommandProcessor.getInstance().executeCommand(() -> WriteAction.run(() -> document.deleteString(at, at + 1)), "delete", null);

            assertSame(incremental, document, "after deleting to " + typed.substring(0, i));
        }
    }

    /**
     * The incremental tokens are the ones of a new highlighter of the text - the message shows where they part
     */
    private static boolean LAYERED;

    private static LexerEditorHighlighter create() {
        return LAYERED
            ? new CSharpEditorHighlighter(null, EditorColorsManager.getInstance().getGlobalScheme())
            : new LexerEditorHighlighter(new consulo.csharp.impl.ide.highlight.CSharpSyntaxHighlighter(), EditorColorsManager.getInstance().getGlobalScheme());
    }

    private static void assertSame(LexerEditorHighlighter incremental, Document document, String step) {
        List<String> actual = tokens(incremental, document);
        List<String> expected = tokens(fresh(document), document);
        int i = 0;
        while (i < actual.size() && i < expected.size() && actual.get(i).equals(expected.get(i))) {
            i++;
        }
        int from = Math.max(0, i - 2);
        assertThat(actual.subList(from, Math.min(actual.size(), i + 4)))
            .as("%s: tokens from #%d", step, from)
            .isEqualTo(expected.subList(from, Math.min(expected.size(), i + 4)));
    }

    private static LexerEditorHighlighter fresh(Document document) {
        LexerEditorHighlighter fresh = create();
        fresh.setText(document.getImmutableCharSequence());
        return fresh;
    }

    private static List<String> tokens(consulo.codeEditor.EditorHighlighter highlighter, Document document) {
        List<String> result = new ArrayList<>();
        CharSequence text = document.getImmutableCharSequence();
        HighlighterIterator iterator = highlighter.createIterator(0);
        while (!iterator.atEnd()) {
            result.add(iterator.getTokenType() + " '" + text.subSequence(iterator.getStart(), iterator.getEnd()) + "'");
            iterator.advance();
        }
        return result;
    }

    private static HighlighterClient client(Project project, Document document) {
        return new HighlighterClient() {
            @Override
            public Project getProject() {
                return project;
            }

            @Override
            public void repaint(int start, int end) {
            }

            @Override
            public Document getDocument() {
                return document;
            }
        };
    }
}
