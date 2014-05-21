/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi;

import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 22.11.13.
 */
public interface CSharpTokens extends TokenType
{
	IElementType STRING_KEYWORD = new IElementType("STRING_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType FIXED_KEYWORD = new IElementType("FIXED_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType USING_KEYWORD = new IElementType("USING_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType VOID_KEYWORD = new IElementType("VOID_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType INT_KEYWORD = new IElementType("INT_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType BYTE_KEYWORD = new IElementType("BYTE_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType BOOL_KEYWORD = new IElementType("BOOL_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType USHORT_KEYWORD = new IElementType("USHORT_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType DYNAMIC_KEYWORD = new IElementType("DYNAMIC_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType DECIMAL_KEYWORD = new IElementType("DECIMAL_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType DOUBLE_KEYWORD = new IElementType("DOUBLE_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType LONG_KEYWORD = new IElementType("LONG_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType ULONG_KEYWORD = new IElementType("ULONG_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType FLOAT_KEYWORD = new IElementType("FLOAT_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType OBJECT_KEYWORD = new IElementType("OBJECT_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType SBYTE_KEYWORD = new IElementType("SBYTE_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType SHORT_KEYWORD = new IElementType("SHORT_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType CHAR_KEYWORD = new IElementType("CHAR_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType UINT_KEYWORD = new IElementType("UINT_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType STATIC_KEYWORD = new IElementType("STATIC_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType IMPLICIT_KEYWORD = new IElementType("IMPLICIT_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType EXPLICIT_KEYWORD = new IElementType("EXPLICIT_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType PUBLIC_KEYWORD = new IElementType("PUBLIC_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType NAMESPACE_KEYWORD = new IElementType("NAMESPACE_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType EVENT_KEYWORD = new IElementType("EVENT_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType DELEGATE_KEYWORD = new IElementType("DELEGATE_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType CONST_KEYWORD = new IElementType("CONST_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType GOTO_KEYWORD = new IElementType("GOTO_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType CLASS_KEYWORD = new IElementType("CLASS_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType INTERFACE_KEYWORD = new IElementType("INTERFACE_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType STRUCT_KEYWORD = new IElementType("STRUCT_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType ENUM_KEYWORD = new IElementType("ENUM_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType NEW_KEYWORD = new IElementType("NEW_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType TYPEOF_KEYWORD = new IElementType("TYPEOF_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType SIZEOF_KEYWORD = new IElementType("SIZEOF_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType DEFAULT_KEYWORD = new IElementType("DEFAULT_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType LOCK_KEYWORD = new IElementType("LOCK_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType BREAK_KEYWORD = new IElementType("BREAK_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType CONTINUE_KEYWORD = new IElementType("CONTINUE_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType FOR_KEYWORD = new IElementType("FOR_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType DO_KEYWORD = new IElementType("DO_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType WHILE_KEYWORD = new IElementType("WHILE_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType RETURN_KEYWORD = new IElementType("RETURN_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType FOREACH_KEYWORD = new IElementType("FOREACH_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType TRY_KEYWORD = new IElementType("TRY_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType CATCH_KEYWORD = new IElementType("CATCH_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType FINALLY_KEYWORD = new IElementType("FINALLY_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType SWITCH_KEYWORD = new IElementType("SWITCH_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType CASE_KEYWORD = new IElementType("CASE_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType THROW_KEYWORD = new IElementType("THROW_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType IN_KEYWORD = new IElementType("IN_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType OUT_KEYWORD = new IElementType("OUT_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType INTERNAL_KEYWORD = new IElementType("INTERNAL_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType ABSTRACT_KEYWORD = new IElementType("ABSTRACT_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType PRIVATE_KEYWORD = new IElementType("PRIVATE_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType SEALED_KEYWORD = new IElementType("SEALED_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType CHECKED_KEYWORD = new IElementType("CHECKED_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType UNCHECKED_KEYWORD = new IElementType("UNCHECKED_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType UNSAFE_KEYWORD = new IElementType("UNSAFE_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType OVERRIDE_KEYWORD = new IElementType("OVERRIDE_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType REF_KEYWORD = new IElementType("REF_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType EXTERN_KEYWORD = new IElementType("EXTERN_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType VIRTUAL_KEYWORD = new IElementType("VIRTUAL_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType PROTECTED_KEYWORD = new IElementType("PROTECTED_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType VOLATILE_KEYWORD = new IElementType("VOLATILE_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType PARAMS_KEYWORD = new IElementType("PARAMS_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType READONLY_KEYWORD = new IElementType("READONLY_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType OPERATOR_KEYWORD = new IElementType("OPERATOR_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType IS_KEYWORD = new IElementType("IS_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType AS_KEYWORD = new IElementType("AS_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType BASE_KEYWORD = new IElementType("BASE_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType THIS_KEYWORD = new IElementType("THIS_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType IF_KEYWORD = new IElementType("IF_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType ELSE_KEYWORD = new IElementType("ELSE_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType LBRACE = new IElementType("LBRACE", CSharpLanguage.INSTANCE);

	IElementType RBRACE = new IElementType("RBRACE", CSharpLanguage.INSTANCE);

	IElementType LPAR = new IElementType("LPAR", CSharpLanguage.INSTANCE);

	IElementType RPAR = new IElementType("RPAR", CSharpLanguage.INSTANCE);

	IElementType LT = new IElementType("LT", CSharpLanguage.INSTANCE);

	IElementType GT = new IElementType("GT", CSharpLanguage.INSTANCE);

	IElementType EQ = new IElementType("EQ", CSharpLanguage.INSTANCE);

	IElementType MULEQ = new IElementType("MULEQ", CSharpLanguage.INSTANCE);

	IElementType DIVEQ = new IElementType("DIVEQ", CSharpLanguage.INSTANCE);

	IElementType PERCEQ = new IElementType("PERCEQ", CSharpLanguage.INSTANCE);

	IElementType PLUSEQ = new IElementType("PLUSEQ", CSharpLanguage.INSTANCE);

	IElementType MINUSEQ = new IElementType("MINUSEQ", CSharpLanguage.INSTANCE);

	IElementType ANDEQ = new IElementType("ANDEQ", CSharpLanguage.INSTANCE);

	IElementType OREQ = new IElementType("OREQ", CSharpLanguage.INSTANCE);

	IElementType XOREQ = new IElementType("XOREQ", CSharpLanguage.INSTANCE);

	IElementType LTLTEQ = new IElementType("LTLTEQ", CSharpLanguage.INSTANCE);

	IElementType GTGTEQ = new IElementType("GTGTEQ", CSharpLanguage.INSTANCE);

	IElementType COLON = new IElementType("COLON", CSharpLanguage.INSTANCE);

	IElementType COLONCOLON = new IElementType("COLONCOLON", CSharpLanguage.INSTANCE);

	IElementType LBRACKET = new IElementType("LBRACKET", CSharpLanguage.INSTANCE);

	IElementType RBRACKET = new IElementType("RBRACKET", CSharpLanguage.INSTANCE);

	IElementType COMMA = new IElementType("COMMA", CSharpLanguage.INSTANCE);

	IElementType SEMICOLON = new IElementType("SEMICOLON", CSharpLanguage.INSTANCE);

	IElementType DOT = new IElementType("DOT", CSharpLanguage.INSTANCE);

	IElementType MUL = new IElementType("MUL", CSharpLanguage.INSTANCE);

	IElementType EQEQ = new IElementType("EQEQ", CSharpLanguage.INSTANCE);

	IElementType PLUS = new IElementType("PLUS", CSharpLanguage.INSTANCE);

	IElementType PLUSPLUS = new IElementType("PLUSPLUS", CSharpLanguage.INSTANCE);

	IElementType MINUS = new IElementType("MINUS", CSharpLanguage.INSTANCE);

	IElementType DARROW = new IElementType("DARROW", CSharpLanguage.INSTANCE);

	IElementType MINUSMINUS = new IElementType("MINUSMINUS", CSharpLanguage.INSTANCE);

	IElementType NTEQ = new IElementType("NTEQ", CSharpLanguage.INSTANCE);

	IElementType AND = new IElementType("AND", CSharpLanguage.INSTANCE);

	IElementType ANDAND = new IElementType("ANDAND", CSharpLanguage.INSTANCE);

	IElementType GTEQ = new IElementType("GTEQ", CSharpLanguage.INSTANCE);

	IElementType GTGT = new IElementType("GTGT", CSharpLanguage.INSTANCE);

	IElementType LTLT = new IElementType("LTLT", CSharpLanguage.INSTANCE);

	IElementType LTEQ = new IElementType("LTEQ", CSharpLanguage.INSTANCE);

	IElementType XOR = new IElementType("XOR", CSharpLanguage.INSTANCE);

	IElementType DIV = new IElementType("DIV", CSharpLanguage.INSTANCE);

	IElementType EXCL = new IElementType("EXCL", CSharpLanguage.INSTANCE);

	IElementType OR = new IElementType("OR", CSharpLanguage.INSTANCE);

	IElementType OROR = new IElementType("OROR", CSharpLanguage.INSTANCE);

	IElementType TILDE = new IElementType("TILDE", CSharpLanguage.INSTANCE);

	IElementType PERC = new IElementType("PERC", CSharpLanguage.INSTANCE);

	IElementType QUEST = new IElementType("QUEST", CSharpLanguage.INSTANCE);

	IElementType NULL_COALESCING = new IElementType("NULL_COALESCING", CSharpLanguage.INSTANCE);

	IElementType LINE_COMMENT = new IElementType("LINE_COMMENT", CSharpLanguage.INSTANCE);

	IElementType LINE_DOC_COMMENT = new IElementType("LINE_DOC_COMMENT", CSharpLanguage.INSTANCE);

	IElementType BLOCK_COMMENT = new IElementType("BLOCK_COMMENT", CSharpLanguage.INSTANCE);

	IElementType INTEGER_LITERAL = new IElementType("INTEGER_LITERAL", CSharpLanguage.INSTANCE);

	IElementType LONG_LITERAL = new IElementType("LONG_LITERAL", CSharpLanguage.INSTANCE);

	IElementType NON_ACTIVE_SYMBOL = new IElementType("NON_ACTIVE_SYMBOL", CSharpLanguage.INSTANCE);

	IElementType FLOAT_LITERAL = new IElementType("FLOAT_LITERAL", CSharpLanguage.INSTANCE);

	IElementType DOUBLE_LITERAL = new IElementType("DOUBLE_LITERAL", CSharpLanguage.INSTANCE);

	IElementType UINTEGER_LITERAL = new IElementType("UINTEGER_LITERAL", CSharpLanguage.INSTANCE);

	IElementType ULONG_LITERAL = new IElementType("ULONG_LITERAL", CSharpLanguage.INSTANCE);

	IElementType DECIMAL_LITERAL = new IElementType("DECIMAL_LITERAL", CSharpLanguage.INSTANCE);

	IElementType CHARACTER_LITERAL = new IElementType("CHARACTER_LITERAL", CSharpLanguage.INSTANCE);

	IElementType BOOL_LITERAL = new IElementType("BOOL_LITERAL", CSharpLanguage.INSTANCE);

	IElementType NULL_LITERAL = new IElementType("NULL_LITERAL", CSharpLanguage.INSTANCE);

	IElementType STRING_LITERAL = new IElementType("STRING_LITERAL", CSharpLanguage.INSTANCE);

	IElementType VERBATIM_STRING_LITERAL = new IElementType("VERBATIM_STRING_LITERAL", CSharpLanguage.INSTANCE);

	IElementType IDENTIFIER = new IElementType("IDENTIFIER", CSharpLanguage.INSTANCE);
}
