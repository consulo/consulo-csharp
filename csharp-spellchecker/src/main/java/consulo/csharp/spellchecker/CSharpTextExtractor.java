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
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.CSharpTokensImpl;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.spellchecker.internal.CSharpStringLiteralTextBuilder;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.spellcheker.text.TextContent;
import consulo.language.spellcheker.text.TextContentBuilder;
import consulo.language.spellcheker.text.TextExtractor;
import consulo.language.spellcheker.text.util.PsiUtils;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Extracts natural language text from C# code:
 * <ul>
 *   <li>{@code //} and {@code /* *}{@code /} comments as {@link TextContent.TextDomain#COMMENTS}, adjacent line comments are joined,</li>
 *   <li>string literals (regular, verbatim, interpolated) as {@link TextContent.TextDomain#LITERALS}.</li>
 * </ul>
 * Inactive preprocessor code is never extracted. Documentation comments are handled by {@link CSharpDocTextExtractor}.
 *
 * @author VISTALL
 * @since 2026-11-02
 */
@ExtensionImpl
public class CSharpTextExtractor extends TextExtractor {
    private static final TextContentBuilder LINE_COMMENT_BUILDER =
        TextContentBuilder.FromPsi.removingIndents(" \t/").removingLineSuffixes(" \t");

    private static final TextContentBuilder BLOCK_COMMENT_BUILDER =
        TextContentBuilder.FromPsi.removingIndents(" \t/*").removingLineSuffixes(" \t*/");

    private static final TokenSet STRING_LITERALS =
        TokenSet.create(CSharpTokens.STRING_LITERAL, CSharpTokens.VERBATIM_STRING_LITERAL, CSharpTokensImpl.INTERPOLATION_STRING_LITERAL);

    @Override
    @RequiredReadAction
    protected List<TextContent> buildTextContents(PsiElement element, Set<TextContent.TextDomain> allowedDomains) {
        IElementType type = PsiUtilCore.getElementType(element);
        if (type == null) {
            return List.of();
        }

        if (type == CSharpTokens.LINE_COMMENT) {
            if (!(element instanceof PsiComment) || !allowedDomains.contains(TextContent.TextDomain.COMMENTS)) {
                return List.of();
            }
            List<PsiElement> roots = PsiUtils.getNotSoDistantSimilarSiblings(
                element,
                it -> it instanceof PsiComment && PsiUtilCore.getElementType(it) == CSharpTokens.LINE_COMMENT
            );
            List<TextContent> fragments = new ArrayList<>();
            for (PsiElement root : roots) {
                TextContent fragment = LINE_COMMENT_BUILDER.build(root, TextContent.TextDomain.COMMENTS);
                if (fragment != null && fragment.length() > 0) {
                    fragments.add(fragment);
                }
            }
            return asList(TextContent.joinWithWhitespace('\n', fragments));
        }

        if (type == CSharpTokens.BLOCK_COMMENT) {
            if (!(element instanceof PsiComment) || !allowedDomains.contains(TextContent.TextDomain.COMMENTS)) {
                return List.of();
            }
            return asList(BLOCK_COMMENT_BUILDER.build(element, TextContent.TextDomain.COMMENTS));
        }

        if (STRING_LITERALS.contains(type)) {
            if (!allowedDomains.contains(TextContent.TextDomain.LITERALS)) {
                return List.of();
            }
            return asList(CSharpStringLiteralTextBuilder.build(element));
        }
        return List.of();
    }

    private static List<TextContent> asList(@Nullable TextContent content) {
        return content == null || content.length() == 0 ? List.of() : List.of(content);
    }

    @Override
    public Language getLanguage() {
        return CSharpLanguage.INSTANCE;
    }
}
