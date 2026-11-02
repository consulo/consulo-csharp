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
package consulo.csharp.spellchecker.internal;

import consulo.annotation.access.RequiredReadAction;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.spellcheker.text.TextContent;
import consulo.language.spellcheker.text.util.EscapeUtils;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a {@link TextContent.TextDomain#LITERALS} text for a C# string literal:
 * <ul>
 *   <li>the {@code $}/{@code @} prefixes and the quote delimiters (including raw {@code """} delimiters) are excluded,</li>
 *   <li>{@code \n} and {@code \t} become whitespace, other backslash escapes are marked unknown (non-verbatim strings only),</li>
 *   <li>doubled quotes in verbatim strings and doubled braces in interpolated strings are collapsed,</li>
 *   <li>interpolation holes {@code {...}} are marked unknown.</li>
 * </ul>
 *
 * @author VISTALL
 * @since 2026-11-02
 */
public final class CSharpStringLiteralTextBuilder {
    private CSharpStringLiteralTextBuilder() {
    }

    @RequiredReadAction
    public static @Nullable TextContent build(PsiElement element) {
        String text = element.getText();
        int length = text.length();

        boolean verbatim = false;
        boolean interpolated = false;
        int pos = 0;
        while (pos < length && (text.charAt(pos) == '$' || text.charAt(pos) == '@')) {
            if (text.charAt(pos) == '$') {
                interpolated = true;
            }
            else {
                verbatim = true;
            }
            pos++;
        }

        int quoteCount = 0;
        while (pos + quoteCount < length && text.charAt(pos + quoteCount) == '"') {
            quoteCount++;
        }
        if (quoteCount == 0) {
            return null;
        }

        boolean raw = quoteCount >= 3;
        int delimiterLength = raw ? quoteCount : 1;
        int start = pos + delimiterLength;
        int end = length;
        int closing = 0;
        while (closing < delimiterLength && end > start && text.charAt(end - 1) == '"') {
            end--;
            closing++;
        }
        if (start >= end) {
            return null;
        }

        boolean backslashEscapes = !verbatim && !raw;
        List<TextContent.Exclusion> exclusions = new ArrayList<>();
        int i = start;
        while (i < end) {
            char c = text.charAt(i);
            if (backslashEscapes && c == '\\' && i + 1 < end) {
                char next = text.charAt(i + 1);
                if (next == 'n' || next == 't') {
                    // replaced with whitespace below
                    i += 2;
                    continue;
                }
                int escapeEnd = findEscapeEnd(text, i, end);
                exclusions.add(TextContent.Exclusion.markUnknown(new TextRange(i - start, escapeEnd - start)));
                i = escapeEnd;
                continue;
            }

            if (verbatim && !raw && c == '"' && i + 1 < end && text.charAt(i + 1) == '"') {
                exclusions.add(TextContent.Exclusion.exclude(new TextRange(i - start, i + 1 - start)));
                i += 2;
                continue;
            }

            if (interpolated) {
                if ((c == '{' || c == '}') && i + 1 < end && text.charAt(i + 1) == c) {
                    exclusions.add(TextContent.Exclusion.exclude(new TextRange(i - start, i + 1 - start)));
                    i += 2;
                    continue;
                }
                if (c == '{') {
                    int holeEnd = findHoleEnd(text, i, end);
                    exclusions.add(TextContent.Exclusion.markUnknown(new TextRange(i - start, holeEnd - start)));
                    i = holeEnd;
                    continue;
                }
            }
            i++;
        }

        TextContent content = TextContent.psiFragment(TextContent.TextDomain.LITERALS, element, new TextRange(start, end));
        if (!exclusions.isEmpty()) {
            content = content.excludeRanges(exclusions);
        }
        if (backslashEscapes) {
            content = EscapeUtils.replaceBackslashEscapedWhitespace(content);
        }
        content = content.trimWhitespace();
        return content.length() == 0 ? null : content;
    }

    private static int findEscapeEnd(String text, int backslash, int end) {
        char kind = text.charAt(backslash + 1);
        int maxDigits = switch (kind) {
            case 'x', 'u' -> 4;
            case 'U' -> 8;
            default -> 0;
        };
        int result = backslash + 2;
        int digits = 0;
        while (digits < maxDigits && result < end && Character.digit(text.charAt(result), 16) >= 0) {
            result++;
            digits++;
        }
        return result;
    }

    /**
     * @return the offset after the closing brace of the interpolation hole starting at {@code open}, or {@code end} if it's unclosed
     */
    private static int findHoleEnd(String text, int open, int end) {
        int depth = 0;
        int i = open;
        while (i < end) {
            char c = text.charAt(i);
            if (c == '"' || c == '\'') {
                i = skipQuoted(text, i, end);
                continue;
            }
            if (c == '{') {
                depth++;
            }
            else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return i + 1;
                }
            }
            i++;
        }
        return end;
    }

    private static int skipQuoted(String text, int quote, int end) {
        char quoteChar = text.charAt(quote);
        int i = quote + 1;
        while (i < end) {
            char c = text.charAt(i);
            if (c == '\\') {
                i += 2;
                continue;
            }
            if (c == quoteChar) {
                return i + 1;
            }
            i++;
        }
        return end;
    }
}
