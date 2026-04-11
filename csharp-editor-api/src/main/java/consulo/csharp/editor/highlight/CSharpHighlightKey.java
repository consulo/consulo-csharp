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

package consulo.csharp.editor.highlight;

import consulo.codeEditor.DefaultLanguageHighlighterColors;
import consulo.codeEditor.EditorColors;
import consulo.colorScheme.TextAttributesKey;

/**
 * @author VISTALL
 * @since 30.11.13.
 */
public interface CSharpHighlightKey
{
	TextAttributesKey STRING = TextAttributesKey.of("CSHARP_STRING", DefaultLanguageHighlighterColors.STRING);

	TextAttributesKey CLASS_NAME = TextAttributesKey.of("CSHARP_CLASS_NAME", DefaultLanguageHighlighterColors.CLASS_NAME);
	TextAttributesKey ENUM_NAME = TextAttributesKey.of("CSHARP_ENUM_NAME", CLASS_NAME);
	TextAttributesKey INTERFACE_NAME = TextAttributesKey.of("CSHARP_INTERFACE_NAME", CLASS_NAME);
	TextAttributesKey ATTRIBUTE_NAME = TextAttributesKey.of("CSHARP_ATTRIBUTE_NAME", DefaultLanguageHighlighterColors.METADATA);
	TextAttributesKey STRUCT_NAME = TextAttributesKey.of("CSHARP_STRUCT_NAME", CLASS_NAME);

	TextAttributesKey GENERIC_PARAMETER_NAME = TextAttributesKey.of("CSHARP_GENERIC_PARAMETER_NAME", DefaultLanguageHighlighterColors.TYPE_ALIAS_NAME);

	TextAttributesKey METHOD_NAME = TextAttributesKey.of("CSHARP_METHOD_NAME", DefaultLanguageHighlighterColors.INSTANCE_METHOD);
	TextAttributesKey CONSTRUCTOR_NAME = TextAttributesKey.of("CSHARP_CONSTRUCTOR_NAME", METHOD_NAME);
	TextAttributesKey DELEGATE_METHOD_NAME = TextAttributesKey.of("CSHARP_DELEGATE_METHOD_NAME", DefaultLanguageHighlighterColors.IDENTIFIER);
	TextAttributesKey STATIC_FIELD = TextAttributesKey.of("CSHARP_STATIC_FIELD", DefaultLanguageHighlighterColors.STATIC_FIELD);
	TextAttributesKey CONSTANT = TextAttributesKey.of("CSHARP_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT);
	TextAttributesKey STATIC_EVENT = TextAttributesKey.of("CSHARP_STATIC_EVENT", STATIC_FIELD);
	TextAttributesKey STATIC_PROPERTY = TextAttributesKey.of("CSHARP_STATIC_PROPERTY", STATIC_FIELD);
	TextAttributesKey INSTANCE_FIELD = TextAttributesKey.of("CSHARP_INSTANCE_FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
	TextAttributesKey INSTANCE_EVENT = TextAttributesKey.of("CSHARP_INSTANCE_EVENT", INSTANCE_FIELD);
	TextAttributesKey INSTANCE_PROPERTY = TextAttributesKey.of("CSHARP_INSTANCE_PROPERTY", INSTANCE_FIELD);
	TextAttributesKey STATIC_METHOD_CALL = TextAttributesKey.of("CSHARP_STATIC_METHOD_CALL", DefaultLanguageHighlighterColors.STATIC_METHOD);
	TextAttributesKey EXTENSION_METHOD_CALL = TextAttributesKey.of("CSHARP_EXTENSION_METHOD_CALL", DefaultLanguageHighlighterColors.LABEL);
	TextAttributesKey METHOD_REF = TextAttributesKey.of("CSHARP_METHOD_REF", DefaultLanguageHighlighterColors.IDENTIFIER);
	TextAttributesKey IMPLICIT_OR_EXPLICIT_CAST = TextAttributesKey.of("CSHARP_IMPLICIT_OR_EXPLICIT_CAST", EditorColors.INJECTED_LANGUAGE_FRAGMENT);
	TextAttributesKey INSTANCE_METHOD_CALL = TextAttributesKey.of("CSHARP_INSTANCE_METHOD_CALL", DefaultLanguageHighlighterColors.IDENTIFIER);
	TextAttributesKey PARAMETER = TextAttributesKey.of("CSHARP_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER);
	TextAttributesKey KEYWORD = TextAttributesKey.of("CSHARP_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
	TextAttributesKey MACRO_KEYWORD = TextAttributesKey.of("CSHARP_MACRO_KEYWORD", DefaultLanguageHighlighterColors.MACRO_KEYWORD);
	TextAttributesKey MACRO_VARIABLE = TextAttributesKey.of("CSHARP_MACRO_VARIABLE", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
	TextAttributesKey LOCAL_VARIABLE = TextAttributesKey.of("CSHARP_LOCAL_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
	TextAttributesKey NUMBER = TextAttributesKey.of("CSHARP_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
	TextAttributesKey SOFT_KEYWORD = TextAttributesKey.of("CSHARP_SOFT_KEYWORD", KEYWORD);
	TextAttributesKey BLOCK_COMMENT = TextAttributesKey.of("CSHARP_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
	TextAttributesKey LINE_COMMENT = TextAttributesKey.of("CSHARP_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
	TextAttributesKey DISABLED_BLOCK = TextAttributesKey.of("CSHARP_DISABLED_BLOCK");

	TextAttributesKey PARENTHESES = TextAttributesKey.of("CSHARP_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);
	TextAttributesKey BRACES = TextAttributesKey.of("CSHARP_BRACES", DefaultLanguageHighlighterColors.BRACES);
	TextAttributesKey BRACKETS = TextAttributesKey.of("CSHARP_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
	TextAttributesKey DOT = TextAttributesKey.of("CSHARP_DOT", DefaultLanguageHighlighterColors.DOT);
	TextAttributesKey COMMA = TextAttributesKey.of("CSHARP_COMMA", DefaultLanguageHighlighterColors.COMMA);
	TextAttributesKey SEMICOLON = TextAttributesKey.of("CSHARP_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON);
	TextAttributesKey COLON = TextAttributesKey.of("CSHARP_COLON", SEMICOLON);
	TextAttributesKey ARROW = TextAttributesKey.of("CSHARP_ARROW", DOT);
	TextAttributesKey OPERATION_SIGN = TextAttributesKey.of("CSHARP_OPERATION_SIGN", DefaultLanguageHighlighterColors.OPERATION_SIGN);
}
