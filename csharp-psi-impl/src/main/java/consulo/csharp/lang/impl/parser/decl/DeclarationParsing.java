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
import consulo.csharp.lang.impl.parser.SharedParsingHelpers;
import consulo.csharp.lang.impl.parser.UsingStatementParsing;
import consulo.csharp.lang.impl.parser.exp.ExpressionParsing;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.psi.CSharpSoftTokens;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.parser.PsiBuilder;
import consulo.util.lang.Pair;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class DeclarationParsing extends SharedParsingHelpers {
    // { (
    private static final TokenSet NAME_STOPPERS = TokenSet.create(LBRACE, LPAR, THIS_KEYWORD, OPERATOR_KEYWORD);

    public static void parseAll(@Nonnull CSharpBuilderWrapper builder, boolean root, boolean isEnum) {
        if (isEnum) {
            IElementType prevToken;
            while (!builder.eof()) {
                prevToken = builder.getTokenType();

                parseEnumConstant(builder, ModifierSet.EMPTY);

                if (builder.getTokenType() == COMMA) {
                    if (prevToken == COMMA) {
                        builder.error("Name expected");
                    }
                    builder.advanceLexer();
                }
                else if (builder.getTokenType() == RBRACE) {
                    break;
                }
                else {
                    PsiBuilder.Marker errorMarker = builder.mark();
                    builder.advanceLexer();
                    errorMarker.error("Expected comma");
                }
            }
        }
        else {
            while (!builder.eof()) {
                if (!root && builder.getTokenType() == RBRACE) {
                    return;
                }

                builder.skipNonInterestItems();

                parse(builder, root);
            }
        }
    }

    private static void parse(@Nonnull CSharpBuilderWrapper builder, boolean root) {
        PsiBuilder.Marker marker = builder.mark();

        Pair<PsiBuilder.Marker, ModifierSet> modifierListPair = parseWithSoftElements(b -> parseModifierListWithAttributes(b, STUB_SUPPORT), builder, PARTIAL_KEYWORD, ASYNC_KEYWORD);

        PsiBuilder.Marker modifierListMarker = modifierListPair.getFirst();
        ModifierSet modifierSet = modifierListPair.getSecond();

        IElementType tokenType = builder.getTokenType();
        if (tokenType == null) {
            if (modifierListPair.getSecond().isEmpty()) {
                marker.drop();
            }
            else {
                if (root) {
                    marker.done(DUMMY_DECLARATION);
                }
                else {
                    marker.error("Expected identifier");
                }
            }
            return;
        }
        if (tokenType == NAMESPACE_KEYWORD) {
            if (modifierListMarker != null) {
                modifierListMarker.drop();
            }

            NamespaceDeclarationParsing.parse(builder, marker);
        }
        else if (CSharpTokenSets.TYPE_DECLARATION_START.contains(tokenType)) {
            TypeDeclarationParsing.parse(builder, marker);
        }
        else if (tokenType == EVENT_KEYWORD) {
            builder.advanceLexer();

            EventParsing.parse(builder, marker, modifierSet);
        }
        else if (tokenType == DELEGATE_KEYWORD) {
            builder.advanceLexer();

            MethodParsing.parseMethodStartAtType(builder, marker, modifierSet);
        }
        else if (tokenType == USING_KEYWORD) {
            if (modifierListMarker != null) {
                modifierListMarker.drop();
            }

            UsingStatementParsing.parseUsing(builder, marker, CSharpBuilderWrapper::advanceLexer);
        }
        else if (tokenType == CONST_KEYWORD) {
            builder.advanceLexer();

            FieldOrPropertyParsing.parseFieldOrLocalVariableAtTypeWithDone(builder, marker, FIELD_DECLARATION, STUB_SUPPORT, true, modifierSet);
        }
        else {
            // global using ...
            if (builder.isSoftKeyword(CSharpSoftTokens.GLOBAL_KEYWORD) && builder.lookAhead(1) == USING_KEYWORD) {
                if (modifierListMarker != null) {
                    modifierListMarker.drop();
                }

                UsingStatementParsing.parseUsing(builder, marker, b ->
                {
                    b.enableSoftKeyword(CSharpSoftTokens.GLOBAL_KEYWORD);
                    // make sure its parsed to global
                    b.advanceLexer();
                    b.disableSoftKeyword(CSharpSoftTokens.GLOBAL_KEYWORD);

                    // eat using
                    b.advanceLexer();
                });
            }
            else {
                // MODIFIER_LIST IDENTIFIER LPAR -> CONSTRUCTOR
                if (tokenType == CSharpTokens.IDENTIFIER && builder.lookAhead(1) == LPAR) {
                    MethodParsing.parseMethodStartAfterType(builder, marker, null, MethodParsing.Target.CONSTRUCTOR, modifierSet);
                }
                else if (tokenType == TILDE) {
                    builder.advanceLexer();

                    MethodParsing.parseMethodStartAfterType(builder, marker, null, MethodParsing.Target.DECONSTRUCTOR, modifierSet);
                }
                else {
                    TypeInfo typeInfo = parseType(builder, STUB_SUPPORT);
                    if (typeInfo == null) {
                        if (!modifierSet.isEmpty()) {
                            if (root) {
                                marker.done(DUMMY_DECLARATION);
                                return;
                            }
                            builder.error("Type expected");
                            marker.done(FIELD_DECLARATION);
                            return;
                        }
                        else {
                            modifierListMarker.drop();
                        }

                        marker.drop();
                        advanceUnexpectedToken(builder);
                    }
                    else if (builder.getTokenType() == OPERATOR_KEYWORD) {
                        MethodParsing.parseMethodStartAfterType(builder, marker, typeInfo, MethodParsing.Target.METHOD, modifierSet);
                    }
                    else {
                        TypeInfo implementType = parseImplementType(builder);
                        if (implementType == null) {
                            builder.error("Name is expected");

                            // if we dont have name but we have lbracket - parse as index method
                            parseAfterName(builder, marker, builder.getTokenType() == LBRACKET ? THIS_KEYWORD : null, modifierSet);
                            return;
                        }

                        IElementType prevToken = null;
                        if (builder.getTokenType() == DOT) {
                            if (builder.lookAhead(1) == CSharpTokens.OPERATOR_KEYWORD) {
                                builder.advanceLexer();
                                
                                MethodParsing.parseMethodStartAfterType(builder, marker, typeInfo, MethodParsing.Target.METHOD, modifierSet);
                                return;
                            } else {
                                builder.advanceLexer();

                                prevToken = builder.getTokenType();

                                doneThisOrIdentifier(builder);
                            }
                        }
                        else {
                            if (implementType.marker != null) {
                                implementType.marker.rollbackTo();
                            }

                            prevToken = builder.getTokenType();

                            doneThisOrIdentifier(builder);
                        }

                        parseAfterName(builder, marker, prevToken, modifierSet);
                    }
                }
            }
        }
    }

    public static void doneThisOrIdentifier(CSharpBuilderWrapper builder) {
        if (builder.getTokenType() == THIS_KEYWORD) {
            builder.advanceLexer();
        }
        else {
            expectOrReportIdentifier(builder, STUB_SUPPORT);
        }
    }

    private static boolean parseEnumConstant(CSharpBuilderWrapper builder, ModifierSet set) {
        if (builder.getTokenType() == RBRACE) {
            return true;
        }
        PsiBuilder.Marker mark = builder.mark();

        boolean nameExpected = false;
        if (builder.getTokenType() == LBRACKET) {
            PsiBuilder.Marker modMark = builder.mark();
            parseAttributeList(builder, set, STUB_SUPPORT);
            modMark.done(CSharpStubElements.MODIFIER_LIST);

            nameExpected = true;
        }

        if (builder.getTokenType() == CSharpTokens.IDENTIFIER) {
            if (!nameExpected) {
                emptyElement(builder, CSharpStubElements.MODIFIER_LIST);
            }

            doneIdentifier(builder, STUB_SUPPORT);

            if (builder.getTokenType() == EQ) {
                builder.advanceLexer();

                if (ExpressionParsing.parse(builder, set) == null) {
                    builder.error("Expression expected");
                }
            }
        }
        else {
            if (builder.getTokenType() == COMMA || builder.getTokenType() == RBRACE) {
                if (nameExpected) {
                    PsiBuilder.Marker identifierMarker = builder.mark();
                    builder.error("Name expected");
                    identifierMarker.done(CSharpStubElements.IDENTIFIER);
                }

                done(mark, ENUM_CONSTANT_DECLARATION);
                return false;
            }
        }

        done(mark, ENUM_CONSTANT_DECLARATION);
        return true;
    }

    private static void parseAfterName(CSharpBuilderWrapper builder, PsiBuilder.Marker marker, @Nullable IElementType prevToken, ModifierSet set) {
        if (prevToken == THIS_KEYWORD) {
            FieldOrPropertyParsing.parseArrayAfterThis(builder, marker, set);
        }
        else if (builder.getTokenType() == LPAR || builder.getTokenType() == LT) // MODIFIER_LIST TYPE IDENTIFIER LPAR -> METHOD
        {
            MethodParsing.parseMethodStartAfterName(builder, marker, MethodParsing.Target.METHOD, set);
        }
        else {
            FieldOrPropertyParsing.parseFieldOrPropertyAfterName(builder, marker, set);
        }
    }

    @Nullable
    public static TypeInfo parseImplementType(CSharpBuilderWrapper builder) {
        IElementType tokenType = builder.getTokenType();
        if (tokenType == THIS_KEYWORD) {
            return new TypeInfo();
        }
        return parseType(builder, STUB_SUPPORT | TYPE_PARAMS_TO_GENERIC_PARAMS, NAME_STOPPERS);
    }
}
