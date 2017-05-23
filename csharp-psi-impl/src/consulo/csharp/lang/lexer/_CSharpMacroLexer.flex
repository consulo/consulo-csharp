package consulo.csharp.lang.lexer;

import java.util.*;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import consulo.csharp.lang.psi.CSharpPreprocesorTokens;

%%

%public
%class _CSharpMacroLexer
%extends LexerBase
%unicode
%function advanceImpl
%type IElementType
%eof{  return;
%eof}

%state MACRO_ENTERED
%state MACRO_EXPRESSION

DIGIT=[0-9]
LETTER=[a-z]|[A-Z]
WHITE_SPACE=[ \n\r\t\f]+
SINGLE_LINE_COMMENT="/""/"[^\r\n]*
SINGLE_LINE_DOC_COMMENT="/""/""/"[^\r\n]*
MULTI_LINE_STYLE_COMMENT=("/*"[^"*"]{COMMENT_TAIL})|"/*"

COMMENT_TAIL=([^"*"]*("*"+[^"*""/"])?)*("*"+"/")?
CHARACTER_LITERAL="'"([^\\\'\r\n]|{ESCAPE_SEQUENCE})*("'"|\\)?
STRING_LITERAL=\"([^\\\"\r\n]|{ESCAPE_SEQUENCE})*(\"|\\)?
ESCAPE_SEQUENCE=\\[^\r\n]


IDENTIFIER=[:jletter:] [:jletterdigit:]*

WHITE_SPACE=[ \n\r\t\f]+


MACRO_WHITE_SPACE=[ \t\f]+
MACRO_NEW_LINE=\r\n|\n|\r

MACRO_START="#"
MACRO_DEFINE={MACRO_START}"define"
MACRO_UNDEF={MACRO_START}"undef"
MACRO_IF={MACRO_START}"if"
MACRO_ENDIF={MACRO_START}"endif"
MACRO_REGION={MACRO_START}"region"
MACRO_ENDREGION={MACRO_START}"endregion"
ILLEGAL_DIRECTIVE={MACRO_START}{IDENTIFIER}
MACRO_ELSE={MACRO_START}"else"
MACRO_ELIF={MACRO_START}"elif"
%%


<MACRO_ENTERED>
{
	{IDENTIFIER}         { return CSharpPreprocesorTokens.IDENTIFIER; }

	{WHITE_SPACE}        { return CSharpPreprocesorTokens.WHITE_SPACE; }

	.                    { return CSharpPreprocesorTokens.BAD_CHARACTER; }
}

<MACRO_EXPRESSION>
{
	"("                  { return CSharpPreprocesorTokens.LPAR; }

	")"                  { return CSharpPreprocesorTokens.RPAR; }

	"!"                  { return CSharpPreprocesorTokens.EXCL; }

	"&&"                 { return CSharpPreprocesorTokens.ANDAND; }

	"||"                 { return CSharpPreprocesorTokens.OROR; }

	{IDENTIFIER}         { return CSharpPreprocesorTokens.IDENTIFIER; }

	{WHITE_SPACE}        { return CSharpPreprocesorTokens.WHITE_SPACE; }

	.                    { return CSharpPreprocesorTokens.BAD_CHARACTER; }
}

<YYINITIAL>
{
	{MACRO_IF}           { yybegin(MACRO_EXPRESSION); return CSharpPreprocesorTokens.MACRO_IF_KEYWORD; }

	{MACRO_ELIF}         { yybegin(MACRO_EXPRESSION); return CSharpPreprocesorTokens.MACRO_ELIF_KEYWORD; }

	{MACRO_ELSE}         { yybegin(MACRO_ENTERED); return CSharpPreprocesorTokens.MACRO_ELSE_KEYWORD; }

	{MACRO_ENDIF}        { yybegin(MACRO_ENTERED); return CSharpPreprocesorTokens.MACRO_ENDIF_KEYWORD; }

	{MACRO_DEFINE}       { yybegin(MACRO_ENTERED); return CSharpPreprocesorTokens.MACRO_DEFINE_KEYWORD; }

	{MACRO_UNDEF}        { yybegin(MACRO_ENTERED); return CSharpPreprocesorTokens.MACRO_UNDEF_KEYWORD; }

	{MACRO_REGION}       { yybegin(MACRO_ENTERED); return CSharpPreprocesorTokens.MACRO_REGION_KEYWORD; }

	{MACRO_ENDREGION}    { yybegin(MACRO_ENTERED); return CSharpPreprocesorTokens.MACRO_ENDREGION_KEYWORD; }

	{ILLEGAL_DIRECTIVE}  { yybegin(MACRO_EXPRESSION); return CSharpPreprocesorTokens.ILLEGAL_KEYWORD; }

	{WHITE_SPACE}        {  return CSharpPreprocesorTokens.WHITE_SPACE; }

	.                    { return CSharpPreprocesorTokens.BAD_CHARACTER; }
}
