package consulo.csharp.lang.impl.lexer;

import consulo.csharp.lang.impl.psi.CSharpPreprocessorTokens;
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
	{SINGLE_LINE_COMMENT} { return CSharpPreprocessorTokens.LINE_COMMENT; }

	{MACRO_IF}           { return CSharpPreprocessorTokens.MACRO_IF_KEYWORD; }

	{MACRO_ELIF}         { return CSharpPreprocessorTokens.MACRO_ELIF_KEYWORD; }

	{MACRO_ELSE}         { return CSharpPreprocessorTokens.MACRO_ELSE_KEYWORD; }

	{MACRO_ENDIF}        { return CSharpPreprocessorTokens.MACRO_ENDIF_KEYWORD; }

	{MACRO_DEFINE}       { return CSharpPreprocessorTokens.MACRO_DEFINE_KEYWORD; }

	{MACRO_UNDEF}        { return CSharpPreprocessorTokens.MACRO_UNDEF_KEYWORD; }

	{MACRO_REGION}       { return CSharpPreprocessorTokens.MACRO_REGION_KEYWORD; }

	{MACRO_ENDREGION}    { return CSharpPreprocessorTokens.MACRO_ENDREGION_KEYWORD; }

	{MACRO_PRAGMA}       { return CSharpPreprocessorTokens.PRAGMA_KEYWORD; }

	{MACRO_NULLABLE}     { return CSharpPreprocessorTokens.NULLABLE_KEYWORD; }

	{MACRO_WARNING}      { return CSharpPreprocessorTokens.WARNING_KEYWORD; }

	{MACRO_ERROR}        { return CSharpPreprocessorTokens.ERROR_KEYWORD; }

	"("                  { return CSharpPreprocessorTokens.LPAR; }

	")"                  { return CSharpPreprocessorTokens.RPAR; }

	"!"                  { return CSharpPreprocessorTokens.EXCL; }

	"&&"                 { return CSharpPreprocessorTokens.ANDAND; }

	"||"                 { return CSharpPreprocessorTokens.OROR; }

	{IDENTIFIER}         { return CSharpPreprocessorTokens.IDENTIFIER; }

	{WHITE_SPACE}        {  return CSharpPreprocessorTokens.WHITE_SPACE; }

	[^]                  { return CSharpPreprocessorTokens.BAD_CHARACTER; }
}
