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
import consulo.csharp.lang.doc.impl.psi.CSharpDocTagImpl;

import java.util.Set;

/**
 * How a tag of a C# XML documentation comment is treated during text extraction.
 *
 * @author VISTALL
 * @since 2026-11-02
 */
public enum CSharpDocTagKind {
    /**
     * A separate piece of prose, e.g. {@code <summary>}, {@code <param>}, {@code <para>}
     */
    BLOCK,
    /**
     * Stands for a word or a code reference, e.g. {@code <see cref="..."/>}, {@code <c>...</c>}
     */
    UNKNOWN,
    /**
     * Code sample, not prose
     */
    CODE,
    /**
     * Formatting markup around prose, e.g. {@code <b>...</b>}
     */
    MARKUP;

    private static final Set<String> UNKNOWN_TAGS = Set.of("see", "seealso", "paramref", "typeparamref", "c", "inheritdoc", "include");

    private static final Set<String> CODE_TAGS = Set.of("code");

    private static final Set<String> MARKUP_TAGS = Set.of("b", "i", "u", "em", "strong", "a", "br", "tt", "sub", "sup", "span");

    @RequiredReadAction
    public static CSharpDocTagKind of(CSharpDocTagImpl tag) {
        String name = tag.getName();
        if (name == null || UNKNOWN_TAGS.contains(name)) {
            return UNKNOWN;
        }
        if (CODE_TAGS.contains(name)) {
            return CODE;
        }
        if (MARKUP_TAGS.contains(name)) {
            return MARKUP;
        }
        return BLOCK;
    }
}
