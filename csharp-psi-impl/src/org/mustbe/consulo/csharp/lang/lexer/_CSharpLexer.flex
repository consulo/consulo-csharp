package org.mustbe.consulo.csharp.lang.lexer;

import java.util.*;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokensImpl;
import org.mustbe.consulo.csharp.lang.psi.CSharpTemplateTokens;

%%

%{
  private boolean myEnteredNewLine = true;

  private IElementType returnElementType(IElementType type)
  {
    myEnteredNewLine = false;
    return type;
  }
%}

%public
%class _CSharpLexer
%extends LexerBase
%unicode
%function advanceImpl
%type IElementType
%eof{  return;
%eof}

%state PREPROCESSOR_DIRECTIVE

DIGIT=[0-9]
SINGLE_LINE_COMMENT="/""/"[^\r\n]*
SINGLE_LINE_DOC_COMMENT="/""/""/"[^\r\n]*
MULTI_LINE_STYLE_COMMENT=("/*"{COMMENT_TAIL})|"/*"

WHITE_SPACE_NO_NEW_LINE=[ \t\f]+
NEW_LINE=\r\n|\n|\r

COMMENT_TAIL=([^"*"]*("*"+[^"*""/"])?)*("*"+"/")?
CHARACTER_LITERAL="'"([^\\\'\r\n]|{ESCAPE_SEQUENCE})*("'"|\\)?
STRING_LITERAL=\"([^\\\"\r\n]|{ESCAPE_SEQUENCE})*(\"|\\)?
ESCAPE_SEQUENCE=\\[^\r\n]

SINGLE_VERBATIM_CHAR=[^\"]
QUOTE_ESC_SEQ=\"\"
VERBATIM_STRING_CHAR={SINGLE_VERBATIM_CHAR}|{QUOTE_ESC_SEQ}
VERBATIM_STRING_LITERAL=@\"{VERBATIM_STRING_CHAR}*\"
INTERPOLATION_STRING_LITERAL=\$\"{VERBATIM_STRING_CHAR}*\"

IDENTIFIER=@?[:jletter:] [:jletterdigit:]*

DIGIT = [0-9]
DIGIT_OR_UNDERSCORE = [_0-9]
DIGITS = {DIGIT} | {DIGIT} {DIGIT_OR_UNDERSCORE}*
HEX_DIGIT_OR_UNDERSCORE = [_0-9A-Fa-f]

INTEGER_LITERAL = {DIGITS} | {HEX_INTEGER_LITERAL} | {BIN_INTEGER_LITERAL}
LONG_LITERAL = {INTEGER_LITERAL} [Ll]
UINTEGER_LITERAL = {INTEGER_LITERAL} [Uu]
ULONG_LITERAL = {INTEGER_LITERAL} (([Uu][Ll]) | ([Ll][Uu]))
HEX_INTEGER_LITERAL = 0 [Xx] {HEX_DIGIT_OR_UNDERSCORE}*
BIN_INTEGER_LITERAL = 0 [Bb] {DIGIT_OR_UNDERSCORE}*

FLOAT_LITERAL = ({DEC_FP_LITERAL} | {HEX_FP_LITERAL}) [Ff] | {DIGITS} [Ff]
DOUBLE_LITERAL = ({DEC_FP_LITERAL} | {HEX_FP_LITERAL}) [Dd]? | {DIGITS} [Dd]
DECIMAL_LITERAL = ({DEC_FP_LITERAL} | {HEX_FP_LITERAL}) [Mm] | {DIGITS} [Mm]
DEC_FP_LITERAL = {DIGITS} {DEC_EXPONENT} | {DEC_SIGNIFICAND} {DEC_EXPONENT}?
DEC_SIGNIFICAND = "." {DIGITS} | {DIGITS} "." {DIGIT_OR_UNDERSCORE}*
DEC_EXPONENT = [Ee] [+-]? {DIGIT_OR_UNDERSCORE}*
HEX_FP_LITERAL = {HEX_SIGNIFICAND} {HEX_EXPONENT}
HEX_SIGNIFICAND = 0 [Xx] ({HEX_DIGIT_OR_UNDERSCORE}+ "."? | {HEX_DIGIT_OR_UNDERSCORE}* "." {HEX_DIGIT_OR_UNDERSCORE}+)
HEX_EXPONENT = [Pp] [+-]? {DIGIT_OR_UNDERSCORE}*
%%

<PREPROCESSOR_DIRECTIVE>
{
	{NEW_LINE}
	{
		myEnteredNewLine = true;
		yybegin(YYINITIAL);
		return CSharpTemplateTokens.PREPROCESSOR_DIRECTIVE;
	}

	{WHITE_SPACE_NO_NEW_LINE}  {  return CSharpTemplateTokens.PREPROCESSOR_DIRECTIVE; }

	.                          { return CSharpTemplateTokens.PREPROCESSOR_DIRECTIVE; }
}

<YYINITIAL>
{
	"#"
	{
		if(myEnteredNewLine)
		{
			yypushback(yylength());
			yybegin(PREPROCESSOR_DIRECTIVE);
		}
		else
		{
			return CSharpTokens.BAD_CHARACTER;
		}
	}

	{VERBATIM_STRING_LITERAL} { return returnElementType(CSharpTokens.VERBATIM_STRING_LITERAL); }

	{INTERPOLATION_STRING_LITERAL} { return CSharpTokensImpl.INTERPOLATION_STRING_LITERAL; }

	"__arglist"               { return returnElementType(CSharpTokens.__ARGLIST_KEYWORD); }

	"__makeref"               { return returnElementType(CSharpTokens.__MAKEREF_KEYWORD); }

	"__reftype"               { return returnElementType(CSharpTokens.__REFTYPE_KEYWORD); }

	"__refvalue"              { return returnElementType(CSharpTokens.__REFVALUE_KEYWORD); }

	"using"                   { return returnElementType(CSharpTokens.USING_KEYWORD); }

// native types
	"string"                  { return returnElementType(CSharpTokens.STRING_KEYWORD); }

	"void"                    { return returnElementType(CSharpTokens.VOID_KEYWORD); }

	"int"                     { return returnElementType(CSharpTokens.INT_KEYWORD); }

	"bool"                    { return returnElementType(CSharpTokens.BOOL_KEYWORD); }

	"byte"                    { return returnElementType(CSharpTokens.BYTE_KEYWORD); }

	"char"                    { return returnElementType(CSharpTokens.CHAR_KEYWORD); }

	"decimal"                 { return returnElementType(CSharpTokens.DECIMAL_KEYWORD); }

	"double"                  { return returnElementType(CSharpTokens.DOUBLE_KEYWORD); }

	"float"                   { return returnElementType(CSharpTokens.FLOAT_KEYWORD); }

	"long"                    { return returnElementType(CSharpTokens.LONG_KEYWORD); }

	"object"                  { return returnElementType(CSharpTokens.OBJECT_KEYWORD); }

	"sbyte"                   { return returnElementType(CSharpTokens.SBYTE_KEYWORD); }

	"short"                   { return returnElementType(CSharpTokens.SHORT_KEYWORD); }

	"uint"                    { return returnElementType(CSharpTokens.UINT_KEYWORD); }

	"ulong"                   { return returnElementType(CSharpTokens.ULONG_KEYWORD); }

	"ushort"                  { return returnElementType(CSharpTokens.USHORT_KEYWORD); }

	"dynamic"                 { return returnElementType(CSharpTokens.DYNAMIC_KEYWORD); }

	"explicit"                { return returnElementType(CSharpTokens.EXPLICIT_KEYWORD); }

	"implicit"                { return returnElementType(CSharpTokens.IMPLICIT_KEYWORD); }

// modifier tokens
	"static"                  { return returnElementType(CSharpTokens.STATIC_KEYWORD); }

	"public"                  { return returnElementType(CSharpTokens.PUBLIC_KEYWORD); }

	"in"                      { return returnElementType(CSharpTokens.IN_KEYWORD); }

	"out"                     { return returnElementType(CSharpTokens.OUT_KEYWORD); }

	"abstract"                { return returnElementType(CSharpTokens.ABSTRACT_KEYWORD); }

	"extern"                  { return returnElementType(CSharpTokens.EXTERN_KEYWORD); }

	"internal"                { return returnElementType(CSharpTokens.INTERNAL_KEYWORD); }

	"override"                { return returnElementType(CSharpTokens.OVERRIDE_KEYWORD); }

	"params"                  { return returnElementType(CSharpTokens.PARAMS_KEYWORD); }

	"private"                 { return returnElementType(CSharpTokens.PRIVATE_KEYWORD); }

	"protected"               { return returnElementType(CSharpTokens.PROTECTED_KEYWORD); }

	"ref"                     { return returnElementType(CSharpTokens.REF_KEYWORD); }

	"readonly"                { return returnElementType(CSharpTokens.READONLY_KEYWORD); }

	"operator"                { return returnElementType(CSharpTokens.OPERATOR_KEYWORD); }

	"sealed"                  { return returnElementType(CSharpTokens.SEALED_KEYWORD); }

	"unsafe"                  { return returnElementType(CSharpTokens.UNSAFE_KEYWORD); }

	"checked"                 { return returnElementType(CSharpTokens.CHECKED_KEYWORD); }

	"unchecked"               { return returnElementType(CSharpTokens.UNCHECKED_KEYWORD); }

	"virtual"                 { return returnElementType(CSharpTokens.VIRTUAL_KEYWORD); }

	"volatile"                { return returnElementType(CSharpTokens.VOLATILE_KEYWORD); }

// declaration tokens
	"namespace"               { return returnElementType(CSharpTokens.NAMESPACE_KEYWORD); }

	"class"                   { return returnElementType(CSharpTokens.CLASS_KEYWORD); }

	"interface"               { return returnElementType(CSharpTokens.INTERFACE_KEYWORD); }

	"struct"                  { return returnElementType(CSharpTokens.STRUCT_KEYWORD); }

	"enum"                    { return returnElementType(CSharpTokens.ENUM_KEYWORD); }

	"event"                   { return returnElementType(CSharpTokens.EVENT_KEYWORD); }

	"delegate"                { return returnElementType(CSharpTokens.DELEGATE_KEYWORD); }

	"const"                   { return returnElementType(CSharpTokens.CONST_KEYWORD); }

// expression tokens
	"new"                     { return returnElementType(CSharpTokens.NEW_KEYWORD); }

	"stackalloc"              { return returnElementType(CSharpTokens.STACKALLOC_KEYWORD); }

	"typeof"                  { return returnElementType(CSharpTokens.TYPEOF_KEYWORD); }

	"sizeof"                  { return returnElementType(CSharpTokens.SIZEOF_KEYWORD); }

	"fixed"                   { return returnElementType(CSharpTokens.FIXED_KEYWORD); }

	"is"                      { return returnElementType(CSharpTokens.IS_KEYWORD); }

	"switch"                  { return returnElementType(CSharpTokens.SWITCH_KEYWORD); }

	"case"                    { return returnElementType(CSharpTokens.CASE_KEYWORD); }

	"default"                 { return returnElementType(CSharpTokens.DEFAULT_KEYWORD); }

	"as"                      { return returnElementType(CSharpTokens.AS_KEYWORD); }

	"lock"                    { return returnElementType(CSharpTokens.LOCK_KEYWORD); }

	"return"                  { return returnElementType(CSharpTokens.RETURN_KEYWORD); }

	"do"                      { return returnElementType(CSharpTokens.DO_KEYWORD); }

	"while"                   { return returnElementType(CSharpTokens.WHILE_KEYWORD); }

	"try"                     { return returnElementType(CSharpTokens.TRY_KEYWORD); }

	"catch"                   { return returnElementType(CSharpTokens.CATCH_KEYWORD); }

	"finally"                 { return returnElementType(CSharpTokens.FINALLY_KEYWORD); }

	"throw"                   { return returnElementType(CSharpTokens.THROW_KEYWORD); }

	"goto"                    { return returnElementType(CSharpTokens.GOTO_KEYWORD); }

	"foreach"                 { return returnElementType(CSharpTokens.FOREACH_KEYWORD); }

	"for"                     { return returnElementType(CSharpTokens.FOR_KEYWORD); }

	"break"                   { return returnElementType(CSharpTokens.BREAK_KEYWORD); }

	"continue"                { return returnElementType(CSharpTokens.CONTINUE_KEYWORD); }

	"base"                    { return returnElementType(CSharpTokens.BASE_KEYWORD); }

	"this"                    { return returnElementType(CSharpTokens.THIS_KEYWORD); }

	"if"                      { return returnElementType(CSharpTokens.IF_KEYWORD); }

	"else"                    { return returnElementType(CSharpTokens.ELSE_KEYWORD); }

//
	"{"                       { return returnElementType(CSharpTokens.LBRACE); }

	"}"                       { return returnElementType(CSharpTokens.RBRACE); }

	"["                       { return returnElementType(CSharpTokens.LBRACKET); }

	"]"                       { return returnElementType(CSharpTokens.RBRACKET); }

	"("                       { return returnElementType(CSharpTokens.LPAR); }

	")"                       { return returnElementType(CSharpTokens.RPAR); }

	"*="                      { return returnElementType(CSharpTokens.MULEQ); }

	"/="                      { return returnElementType(CSharpTokens.DIVEQ); }

	"%="                      { return returnElementType(CSharpTokens.PERCEQ); }

	"+="                      { return returnElementType(CSharpTokens.PLUSEQ); }

	"-="                      { return returnElementType(CSharpTokens.MINUSEQ); }

	"&="                      { return returnElementType(CSharpTokens.ANDEQ); }

	"^="                      { return returnElementType(CSharpTokens.XOREQ); }

	"|="                      { return returnElementType(CSharpTokens.OREQ); }

	"<<="                     { return returnElementType(CSharpTokens.LTLTEQ); }

	">>="                     { return returnElementType(CSharpTokens.GTGTEQ); }

	"<="                      { return returnElementType(CSharpTokens.LTEQ); }

	">="                      { return returnElementType(CSharpTokens.GTEQ); }

	"<"                       { return returnElementType(CSharpTokens.LT); }

	">"                       { return returnElementType(CSharpTokens.GT); }

	"="                       { return returnElementType(CSharpTokens.EQ); }

	":"                       { return returnElementType(CSharpTokens.COLON); }

	"::"                      { return returnElementType(CSharpTokens.COLONCOLON); }

	";"                       { return returnElementType(CSharpTokens.SEMICOLON); }

	"*"                       { return returnElementType(CSharpTokens.MUL); }

	"=>"                      { return returnElementType(CSharpTokens.DARROW); }

	"->"                      { return returnElementType(CSharpTokens.ARROW); }

	"=="                      { return returnElementType(CSharpTokens.EQEQ); }

	"++"                      { return returnElementType(CSharpTokens.PLUSPLUS); }

	"+"                       { return returnElementType(CSharpTokens.PLUS); }

	"--"                      { return returnElementType(CSharpTokens.MINUSMINUS); }

	"-"                       { return returnElementType(CSharpTokens.MINUS); }

	"/"                       { return returnElementType(CSharpTokens.DIV); }

	"%"                       { return returnElementType(CSharpTokens.PERC); }

	"&&"                      { return returnElementType(CSharpTokens.ANDAND); }

	"&"                       { return returnElementType(CSharpTokens.AND); }

	"||"                      { return returnElementType(CSharpTokens.OROR); }

	"|"                       { return returnElementType(CSharpTokens.OR); }

	"~"                       { return returnElementType(CSharpTokens.TILDE); }

	"!="                      { return returnElementType(CSharpTokens.NTEQ); }

	"!"                       { return returnElementType(CSharpTokens.EXCL); }

	"^"                       { return returnElementType(CSharpTokens.XOR); }

	"."                       { return returnElementType(CSharpTokens.DOT); }

	","                       { return returnElementType(CSharpTokens.COMMA); }

	"?."                      { return returnElementType(CSharpTokens.NULLABE_CALL); }

	"?"                       { return returnElementType(CSharpTokens.QUEST); }

	"??"                      { return returnElementType(CSharpTokens.QUESTQUEST); }

	"false"                   { return returnElementType(CSharpTokens.FALSE_KEYWORD); }

	"true"                    { return returnElementType(CSharpTokens.TRUE_KEYWORD); }

	"null"                    { return returnElementType(CSharpTokens.NULL_LITERAL); }

	{SINGLE_LINE_DOC_COMMENT} { return CSharpTokensImpl.LINE_DOC_COMMENT; }

	{SINGLE_LINE_COMMENT}     { return returnElementType(CSharpTokens.LINE_COMMENT); }

	{MULTI_LINE_STYLE_COMMENT} { return returnElementType(CSharpTokens.BLOCK_COMMENT); }

	{UINTEGER_LITERAL}        { return returnElementType(CSharpTokens.UINTEGER_LITERAL); }

	{ULONG_LITERAL}           { return returnElementType(CSharpTokens.ULONG_LITERAL); }

	{INTEGER_LITERAL}         { return returnElementType(CSharpTokens.INTEGER_LITERAL); }

	{LONG_LITERAL}            { return returnElementType(CSharpTokens.LONG_LITERAL); }

	{DECIMAL_LITERAL}         { return returnElementType(CSharpTokens.DECIMAL_LITERAL); }

	{DOUBLE_LITERAL}          { return returnElementType(CSharpTokens.DOUBLE_LITERAL); }

	{FLOAT_LITERAL}           { return returnElementType(CSharpTokens.FLOAT_LITERAL); }

	{CHARACTER_LITERAL}       { return returnElementType(CSharpTokens.CHARACTER_LITERAL); }

	{STRING_LITERAL}          { return returnElementType(CSharpTokens.STRING_LITERAL); }

	{IDENTIFIER}              { return returnElementType(CSharpTokens.IDENTIFIER); }

	{WHITE_SPACE_NO_NEW_LINE} { return CSharpTokens.WHITE_SPACE; }

	{NEW_LINE}                { myEnteredNewLine = true; return CSharpTokens.WHITE_SPACE; }

	.                         { return returnElementType(CSharpTokens.BAD_CHARACTER); }
}
