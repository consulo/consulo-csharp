package consulo.csharp.lang.impl.lexer;

import consulo.csharp.lang.impl.psi.CSharpPreprocesorTokens;
import consulo.language.ast.IElementType;
import consulo.language.lexer.LexerBase;
%%

%public
%class CSharpPreprocessorHightlightLexer
%extends LexerBase
%unicode
%function advanceImpl
%type IElementType

WHITE_SPACE=[ \n\r\t\f]+

IDENTIFIER=[:jletter:] [:jletterdigit:]*

WHITE_SPACE=[ \n\r\t\f]+

MACRO_WHITE_SPACE=[ \t\f]+
MACRO_NEW_LINE=\r\n|\n|\r

SINGLE_LINE_COMMENT="/""/"[^\r\n]*

MACRO_START={MACRO_NEW_LINE}?{MACRO_WHITE_SPACE}?"#"
MACRO_DEFINE={MACRO_START}"define"
MACRO_UNDEF={MACRO_START}"undef"
MACRO_IF={MACRO_START}"if"
MACRO_ENDIF={MACRO_START}"endif"
MACRO_REGION={MACRO_START}"region"
MACRO_ENDREGION={MACRO_START}"endregion"
MACRO_ELSE={MACRO_START}"else"
MACRO_ELIF={MACRO_START}"elif"
MACRO_PRAGMA={MACRO_START}"pragma"
MACRO_WARNING={MACRO_START}"warning"
MACRO_NULLABLE={MACRO_START}"nullable"
MACRO_ERROR={MACRO_START}"error"
%%

<YYINITIAL>
{
	{SINGLE_LINE_COMMENT} { return CSharpPreprocesorTokens.LINE_COMMENT; }

	{MACRO_IF}           { return CSharpPreprocesorTokens.MACRO_IF_KEYWORD; }

	{MACRO_ELIF}         { return CSharpPreprocesorTokens.MACRO_ELIF_KEYWORD; }

	{MACRO_ELSE}         { return CSharpPreprocesorTokens.MACRO_ELSE_KEYWORD; }

	{MACRO_ENDIF}        { return CSharpPreprocesorTokens.MACRO_ENDIF_KEYWORD; }

	{MACRO_DEFINE}       { return CSharpPreprocesorTokens.MACRO_DEFINE_KEYWORD; }

	{MACRO_UNDEF}        { return CSharpPreprocesorTokens.MACRO_UNDEF_KEYWORD; }

	{MACRO_REGION}       { return CSharpPreprocesorTokens.MACRO_REGION_KEYWORD; }

	{MACRO_ENDREGION}    { return CSharpPreprocesorTokens.MACRO_ENDREGION_KEYWORD; }

	{MACRO_PRAGMA}       { return CSharpPreprocesorTokens.PRAGMA_KEYWORD; }

	{MACRO_NULLABLE}     { return CSharpPreprocesorTokens.NULLABLE_KEYWORD; }

	{MACRO_WARNING}      { return CSharpPreprocesorTokens.WARNING_KEYWORD; }

	{MACRO_ERROR}        { return CSharpPreprocesorTokens.ERROR_KEYWORD; }

	"("                  { return CSharpPreprocesorTokens.LPAR; }

	")"                  { return CSharpPreprocesorTokens.RPAR; }

	"!"                  { return CSharpPreprocesorTokens.EXCL; }

	"&&"                 { return CSharpPreprocesorTokens.ANDAND; }

	"||"                 { return CSharpPreprocesorTokens.OROR; }

	{IDENTIFIER}         { return CSharpPreprocesorTokens.IDENTIFIER; }

	{WHITE_SPACE}        {  return CSharpPreprocesorTokens.WHITE_SPACE; }

	[^]                  { return CSharpPreprocesorTokens.BAD_CHARACTER; }
}
