package consulo.csharp.lang.doc.impl.lexer;

import consulo.csharp.lang.psi.CSharpTokens;
import consulo.language.ast.IElementType;
import consulo.language.lexer.LexerBase;

%%

%public
%class CSharpReferenceLexer
%extends LexerBase
%unicode
%function advanceImpl
%type IElementType
%eof{  return;
%eof}


DIGIT=[0-9]
WHITE_SPACE=[ \n\r\t\f]+
SINGLE_LINE_COMMENT="/""/"[^\r\n]*
SINGLE_LINE_DOC_COMMENT="/""/""/"[^\r\n]*
MULTI_LINE_STYLE_COMMENT=("/*"{COMMENT_TAIL})|"/*"

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

<YYINITIAL>
{
	"{"                       { return CSharpTokens.LBRACE; }

	"}"                       { return CSharpTokens.RBRACE; }

	"["                       { return CSharpTokens.LBRACKET; }

	"]"                       { return CSharpTokens.RBRACKET; }

	"("                       { return CSharpTokens.LPAR; }

	")"                       { return CSharpTokens.RPAR; }

	"*="                      { return CSharpTokens.MULEQ; }

	"/="                      { return CSharpTokens.DIVEQ; }

	"%="                      { return CSharpTokens.PERCEQ; }

	"+="                      { return CSharpTokens.PLUSEQ; }

	"-="                      { return CSharpTokens.MINUSEQ; }

	"&="                      { return CSharpTokens.ANDEQ; }

	"^="                      { return CSharpTokens.XOREQ; }

	"|="                      { return CSharpTokens.OREQ; }

	"<<="                     { return CSharpTokens.LTLTEQ; }

	">>="                     { return CSharpTokens.GTGTEQ; }

	"<="                      { return CSharpTokens.LTEQ; }

	">="                      { return CSharpTokens.GTEQ; }

	"<"                       { return CSharpTokens.LT; }

	">"                       { return CSharpTokens.GT; }

	"="                       { return CSharpTokens.EQ; }

	":"                       { return CSharpTokens.COLON; }

	"::"                      { return CSharpTokens.COLONCOLON; }

	";"                       { return CSharpTokens.SEMICOLON; }

	"*"                       { return CSharpTokens.MUL; }

	"=>"                      { return CSharpTokens.DARROW; }

	"->"                      { return CSharpTokens.ARROW; }

	"=="                      { return CSharpTokens.EQEQ; }

	"++"                      { return CSharpTokens.PLUSPLUS; }

	"+"                       { return CSharpTokens.PLUS; }

	"--"                      { return CSharpTokens.MINUSMINUS; }

	"-"                       { return CSharpTokens.MINUS; }

	"/"                       { return CSharpTokens.DIV; }

	"%"                       { return CSharpTokens.PERC; }

	"&&"                      { return CSharpTokens.ANDAND; }

	"&"                       { return CSharpTokens.AND; }

	"||"                      { return CSharpTokens.OROR; }

	"|"                       { return CSharpTokens.OR; }

	"~"                       { return CSharpTokens.TILDE; }

	"!="                      { return CSharpTokens.NTEQ; }

	"!"                       { return CSharpTokens.EXCL; }

	"^"                       { return CSharpTokens.XOR; }

	"."                       { return CSharpTokens.DOT; }

	","                       { return CSharpTokens.COMMA; }

	"?."                      { return CSharpTokens.NULLABE_CALL; }

	"?"                       { return CSharpTokens.QUEST; }

	"??"                      { return CSharpTokens.QUESTQUEST; }

	"false"                   { return CSharpTokens.FALSE_KEYWORD; }

	"true"                    { return CSharpTokens.TRUE_KEYWORD; }

	"null"                    { return CSharpTokens.NULL_LITERAL; }

	{UINTEGER_LITERAL}        { return CSharpTokens.UINTEGER_LITERAL; }

	{ULONG_LITERAL}           { return CSharpTokens.ULONG_LITERAL; }

	{INTEGER_LITERAL}         { return CSharpTokens.INTEGER_LITERAL; }

	{LONG_LITERAL}            { return CSharpTokens.LONG_LITERAL; }

	{DECIMAL_LITERAL}         { return CSharpTokens.DECIMAL_LITERAL; }

	{DOUBLE_LITERAL}          { return CSharpTokens.DOUBLE_LITERAL; }

	{FLOAT_LITERAL}           { return CSharpTokens.FLOAT_LITERAL; }

	{CHARACTER_LITERAL}       { return CSharpTokens.CHARACTER_LITERAL; }

	{STRING_LITERAL}          { return CSharpTokens.STRING_LITERAL; }

	{IDENTIFIER}              { return CSharpTokens.IDENTIFIER; }

	{WHITE_SPACE}             { return CSharpTokens.WHITE_SPACE; }

	.                         { return CSharpTokens.BAD_CHARACTER; }
}
