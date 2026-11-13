/*
 * Copyright 2013-2017 consulo.io
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

package consulo.csharp.lang.impl.parser.decl;

import consulo.csharp.lang.impl.parser.CSharpBuilderWrapper;
import consulo.csharp.lang.impl.parser.ModifierSet;
import consulo.csharp.lang.impl.parser.exp.ExpressionParsing;
import consulo.csharp.lang.psi.CSharpSoftTokens;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.parser.PsiBuilder;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class FieldOrPropertyParsing extends MemberWithBodyParsing {
    // before C# 9 init is a name, not an accessor
    private static final TokenSet PROPERTY_ACCESSOR_START_BEFORE_9 = TokenSet.andNot(PROPERTY_ACCESSOR_START, TokenSet.create(CSharpSoftTokens.INIT_KEYWORD));

    private static TokenSet propertyAccessorStart(CSharpBuilderWrapper builder) {
        return builder.getVersion().isAtLeast(CSharpLanguageVersion._9_0) ? PROPERTY_ACCESSOR_START : PROPERTY_ACCESSOR_START_BEFORE_9;
    }
    public static void parseFieldOrLocalVariableAtTypeWithDone(CSharpBuilderWrapper builder, PsiBuilder.Marker marker, IElementType to, int typeFlags, boolean semicolonEat, ModifierSet set) {
        TypeInfo typeInfo = parseType(builder, typeFlags);
        if (typeInfo == null) {
            builder.error("Type expected");

            if (semicolonEat) {
                expect(builder, SEMICOLON, "';' expected");
            }
            marker.done(to);
        }
        else {
            parseFieldOrLocalVariableAtNameWithDone(builder, marker, to, typeFlags, semicolonEat, set);
        }
    }

    public static boolean parseFieldOrLocalVariableAtNameWithDone(CSharpBuilderWrapper builder, PsiBuilder.Marker marker, IElementType to, int typeFlags, boolean semicolonEat, ModifierSet set) {
        if (expectOrReportIdentifier(builder, typeFlags)) {
            parseFieldAfterName(builder, marker, to, typeFlags, semicolonEat, set);
            return true;
        }
        else {
            if (semicolonEat) {
                expect(builder, SEMICOLON, "';' expected");
            }

            done(marker, to);
            return false;
        }
    }

    private static PsiBuilder.Marker parseFieldAfterName(CSharpBuilderWrapper builder, PsiBuilder.Marker marker, IElementType to, int typeFlags, boolean semicolonEat, ModifierSet set) {
        boolean autoProperty = builder.getTokenType() == DARROW;

        if (builder.getTokenType() == EQ || autoProperty) {
            builder.advanceLexer();
            if (ExpressionParsing.parseVariableInitializer(builder, set, 0) == null) {
                builder.error("Expression expected");
            }
        }

        if (autoProperty) {
            // if we found darrow - its property
            to = PROPERTY_DECLARATION;
        }

        if (!autoProperty && builder.getTokenType() == COMMA) {
            builder.advanceLexer();

            marker.done(to);

            PsiBuilder.Marker newMarker = builder.mark();

            parseFieldOrLocalVariableAtNameWithDone(builder, newMarker, to, typeFlags, semicolonEat, set);

            return marker;
        }
        else {
            if (semicolonEat) {
                expect(builder, SEMICOLON, "';' expected");
            }

            done(marker, to);

            return marker;
        }
    }

    public static void parseArrayAfterThis(CSharpBuilderWrapper builder, PsiBuilder.Marker marker, ModifierSet set) {
        if (builder.getTokenType() == LBRACKET) {
            MethodParsing.parseParameterList(builder, STUB_SUPPORT, RBRACKET, set);
        }
        else {
            builder.error("'[' expected");
        }

        boolean autoProperty = builder.getTokenType() == DARROW;

        if (autoProperty) {
            MethodParsing.parseExpressionCodeBlock(builder);
        }
        else {
            parseAccessors(builder, XACCESSOR, propertyAccessorStart(builder));
        }

        done(marker, INDEX_METHOD_DECLARATION);
    }

    public static void parseFieldOrPropertyAfterName(CSharpBuilderWrapper builder, PsiBuilder.Marker marker, ModifierSet set) {
        if (builder.getTokenType() == LBRACE) {
            parseAccessors(builder, XACCESSOR, propertyAccessorStart(builder));

            if (builder.getTokenType() == EQ) {
                builder.advanceLexer();
                if (ExpressionParsing.parseVariableInitializer(builder, set, 0) == null) {
                    builder.error("Expression expected");
                }
                expect(builder, SEMICOLON, "';' expected");
            }

            done(marker, PROPERTY_DECLARATION);
        }
        else if (builder.getTokenType() == LBRACKET) {
            PsiBuilder.Marker initMark = builder.mark();
            builder.advanceLexer();

            if (ExpressionParsing.parse(builder, set, 0) == null) {
                builder.error("Expression expected");
            }

            expect(builder, RBRACKET, "']' expected");
            
            initMark.done(FIXED_SIZE_BUFFER_INITIALIZER);

            parseFieldAfterName(builder, marker, FIELD_DECLARATION, STUB_SUPPORT, true, set);
        }
        else {
            parseFieldAfterName(builder, marker, FIELD_DECLARATION, STUB_SUPPORT, true, set);
        }
    }
}
