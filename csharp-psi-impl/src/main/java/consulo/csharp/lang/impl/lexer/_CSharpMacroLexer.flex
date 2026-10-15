package consulo.csharp.lang.impl.lexer;

import consulo.csharp.lang.impl.psi.CSharpPreprocessorTokens;
import consulo.language.ast.IElementType;
import consulo.language.lexer.LexerBase;

%%

%public
%class _CSharpMacroLexer
%extends LexerBase
%unicode
%function advanceImpl
%type IElementType

%state MACRO_ENTERED
%state MACRO_EXPRESSION

WHITE_SPACE=[ \n\r\t\f]+

IDENTIFIER=[:jletter:] [:jletterdigit:]*

SINGLE_LINE_COMMENT="/""/"[^\r\n]*

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
MACRO_PRAGMA={MACRO_START}"pragma"
MACRO_NULLABLE={MACRO_START}"nullable"
MACRO_WARNING={MACRO_START}"warning"
MACRO_ERROR={MACRO_START}"error"
%%


<MACRO_ENTERED>
{
	{SINGLE_LINE_COMMENT} { return CSharpPreprocessorTokens.LINE_COMMENT; }

	{IDENTIFIER}         { return CSharpPreprocessorTokens.IDENTIFIER; }

	{WHITE_SPACE}        { return CSharpPreprocessorTokens.WHITE_SPACE; }

	.                    { return CSharpPreprocessorTokens.BAD_CHARACTER; }
}

<MACRO_EXPRESSION>
{
	{SINGLE_LINE_COMMENT} { return CSharpPreprocessorTokens.LINE_COMMENT; }

	"("                  { return CSharpPreprocessorTokens.LPAR; }

	")"                  { return CSharpPreprocessorTokens.RPAR; }

	"!"                  { return CSharpPreprocessorTokens.EXCL; }

	"&&"                 { return CSharpPreprocessorTokens.ANDAND; }

	"||"                 { return CSharpPreprocessorTokens.OROR; }

	{IDENTIFIER}         { return CSharpPreprocessorTokens.IDENTIFIER; }

	{WHITE_SPACE}        { return CSharpPreprocessorTokens.WHITE_SPACE; }

	.                    { return CSharpPreprocessorTokens.BAD_CHARACTER; }
}

<YYINITIAL>
{
	{SINGLE_LINE_COMMENT} { return CSharpPreprocessorTokens.LINE_COMMENT; }

	{MACRO_IF}           { yybegin(MACRO_EXPRESSION); return CSharpPreprocessorTokens.MACRO_IF_KEYWORD; }

	{MACRO_ELIF}         { yybegin(MACRO_EXPRESSION); return CSharpPreprocessorTokens.MACRO_ELIF_KEYWORD; }

	{MACRO_ELSE}         { yybegin(MACRO_ENTERED); return CSharpPreprocessorTokens.MACRO_ELSE_KEYWORD; }

	{MACRO_ENDIF}        { yybegin(MACRO_ENTERED); return CSharpPreprocessorTokens.MACRO_ENDIF_KEYWORD; }

	{MACRO_DEFINE}       { yybegin(MACRO_ENTERED); return CSharpPreprocessorTokens.MACRO_DEFINE_KEYWORD; }

	{MACRO_UNDEF}        { yybegin(MACRO_ENTERED); return CSharpPreprocessorTokens.MACRO_UNDEF_KEYWORD; }

	{MACRO_REGION}       { yybegin(MACRO_ENTERED); return CSharpPreprocessorTokens.MACRO_REGION_KEYWORD; }

	{MACRO_ENDREGION}    { yybegin(MACRO_ENTERED); return CSharpPreprocessorTokens.MACRO_ENDREGION_KEYWORD; }

	{MACRO_PRAGMA}       { yybegin(MACRO_ENTERED); return CSharpPreprocessorTokens.PRAGMA_KEYWORD; }

	{MACRO_NULLABLE}     { yybegin(MACRO_ENTERED); return CSharpPreprocessorTokens.NULLABLE_KEYWORD; }

	{MACRO_WARNING}      { yybegin(MACRO_ENTERED); return CSharpPreprocessorTokens.WARNING_KEYWORD; }

	{MACRO_ERROR}        { yybegin(MACRO_ENTERED); return CSharpPreprocessorTokens.ERROR_KEYWORD; }

	{ILLEGAL_DIRECTIVE}  { yybegin(MACRO_EXPRESSION); return CSharpPreprocessorTokens.ILLEGAL_KEYWORD; }

	{WHITE_SPACE}        {  return CSharpPreprocessorTokens.WHITE_SPACE; }

	[^]                  { return CSharpPreprocessorTokens.BAD_CHARACTER; }
}
