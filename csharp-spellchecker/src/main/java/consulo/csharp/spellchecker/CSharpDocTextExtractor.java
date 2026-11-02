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
package consulo.csharp.spellchecker;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.doc.impl.CSharpDocLanguage;
import consulo.csharp.lang.doc.impl.psi.CSharpDocRootImpl;
import consulo.csharp.lang.doc.impl.psi.CSharpDocTagImpl;
import consulo.csharp.lang.doc.impl.psi.CSharpDocText;
import consulo.csharp.lang.doc.impl.psi.CSharpDocTokenType;
import consulo.csharp.spellchecker.internal.CSharpDocTagKind;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.spellcheker.text.TextContent;
import consulo.language.spellcheker.text.TextContentBuilder;
import consulo.language.spellcheker.text.TextExtractor;
import consulo.language.spellcheker.text.util.Text;
import consulo.util.collection.ContainerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Extracts {@link TextContent.TextDomain#DOCUMENTATION} prose from C# XML documentation comments ({@code ///}).
 * <p>
 * Every block tag ({@code <summary>}, {@code <param>}, {@code <returns>}, {@code <para>} etc.) and the untagged text
 * produce separate texts. Tag syntax and attribute values are excluded, reference-like inline tags
 * ({@code <see>}, {@code <paramref>}, {@code <c>} ...) are marked unknown, {@code <code>} blocks are excluded,
 * formatting tags ({@code <b>}, {@code <i>} ...) are treated as markup, XML entities are marked unknown.
 *
 * @author VISTALL
 * @since 2026-11-02
 */
@ExtensionImpl
public class CSharpDocTextExtractor extends TextExtractor {
    private static final Pattern ENTITY = Pattern.compile("&#?[A-Za-z0-9]+;");

    private static final TokenSet ALWAYS_EXCLUDED = TokenSet.orSet(
        TokenSet.create(CSharpDocTokenType.DOC_LINE_START),
        CSharpDocTokenType.COMMENTS
    );

    @Override
    @RequiredReadAction
    protected List<TextContent> buildTextContents(PsiElement element, Set<TextContent.TextDomain> allowedDomains) {
        if (!(element instanceof CSharpDocRootImpl) || !allowedDomains.contains(TextContent.TextDomain.DOCUMENTATION)) {
            return List.of();
        }

        List<PsiElement> units = new ArrayList<>();
        units.add(element);
        collectBlockTags(element, units);

        List<TextContent> result = new ArrayList<>();
        for (PsiElement unit : units) {
            TextContent content = createBuilder(unit)
                .build(unit, TextContent.TextDomain.DOCUMENTATION, new TextRange(0, unit.getTextLength()));
            if (content == null) {
                continue;
            }
            List<TextRange> entities = Text.allOccurrences(ENTITY, content);
            if (!entities.isEmpty()) {
                content = content.excludeRanges(ContainerUtil.map(entities, TextContent.Exclusion::markUnknown)).trimWhitespace();
            }
            if (content.length() > 0) {
                result.add(content);
            }
        }
        return result;
    }

    @RequiredReadAction
    private static void collectBlockTags(PsiElement parent, List<PsiElement> result) {
        for (PsiElement child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof CSharpDocTagImpl tag) {
                CSharpDocTagKind kind = CSharpDocTagKind.of(tag);
                if (kind == CSharpDocTagKind.BLOCK) {
                    result.add(tag);
                    collectBlockTags(tag, result);
                }
                else if (kind == CSharpDocTagKind.MARKUP) {
                    collectBlockTags(tag, result);
                }
            }
        }
    }

    private static TextContentBuilder createBuilder(PsiElement unit) {
        return TextContentBuilder.FromPsi
            .excluding(e -> ALWAYS_EXCLUDED.contains(PsiUtilCore.getElementType(e)))
            // nested blocks produce their own texts
            .excluding(e -> e != unit && e instanceof CSharpDocTagImpl tag && isExcludedTag(tag))
            .withUnknown(e -> e instanceof CSharpDocTagImpl tag && isUnknownTag(tag))
            .excluding(e -> e.getParent() == unit && isTagSyntax(e))
            .withMarkup(CSharpDocTextExtractor::isTagSyntax)
            .withUnknown(e -> e instanceof PsiErrorElement && e.getTextLength() > 0)
            .removingIndents(" \t/")
            .removingLineSuffixes(" \t");
    }

    @RequiredReadAction
    private static boolean isExcludedTag(CSharpDocTagImpl tag) {
        CSharpDocTagKind kind = CSharpDocTagKind.of(tag);
        return kind == CSharpDocTagKind.BLOCK || kind == CSharpDocTagKind.CODE;
    }

    @RequiredReadAction
    private static boolean isUnknownTag(CSharpDocTagImpl tag) {
        return CSharpDocTagKind.of(tag) == CSharpDocTagKind.UNKNOWN;
    }

    /**
     * Tag name, attributes and angle brackets - everything inside a tag except its text, nested tags and plain whitespace.
     */
    private static boolean isTagSyntax(PsiElement element) {
        if (!(element.getParent() instanceof CSharpDocTagImpl)) {
            return false;
        }
        if (element instanceof CSharpDocText || element instanceof CSharpDocTagImpl) {
            return false;
        }
        IElementType type = PsiUtilCore.getElementType(element);
        return type != CSharpDocTokenType.XML_REAL_WHITE_SPACE && type != CSharpDocTokenType.XML_WHITE_SPACE;
    }

    @Override
    public Language getLanguage() {
        return CSharpDocLanguage.INSTANCE;
    }
}
