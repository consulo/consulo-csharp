package consulo.csharp.lang.impl.lexer;

import consulo.csharp.lang.impl.psi.CSharpTemplateTokens;
import consulo.csharp.lang.impl.psi.CSharpTokensImpl;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.cfs.lang.CfsTokens;
import consulo.language.ast.IElementType;
import consulo.language.lexer.LexerBase;

%%

%{
  private int myStringInterpolationBalance;
  private int myInsideStringInterpolationParenthesesBalance;
  private java.util.ArrayDeque<Boolean> myStates = new java.util.ArrayDeque<>();
  private java.util.ArrayDeque<Integer> myStacks = new java.util.ArrayDeque<>();

  private final boolean myHighlight;

  public _CSharpLexer(boolean highlight) {
     myHighlight = highlight;
  }

  public boolean isInsideInterpolation() {
     return !myStates.isEmpty();
  }

  public void beginArgumentExpression() {
     myStates.addLast(Boolean.TRUE);
  }

  public void endArgumentExpression() {
     myStates.removeLast();
  }

  public void push(int value) {
     myStacks.add(yystate());

     yybegin(value);
  }

  public void clear() {
     myStringInterpolationBalance = 0;
     myStates.clear();
     myInsideStringInterpolationParenthesesBalance = 0;
     myStacks.clear();
  }


  public void back() {
     if(myStacks.isEmpty()) {
        yybegin(YYINITIAL);
     }
     else {
        int value = myStacks.removeLast();
        yybegin(value);
     }
  }
%}

%public
%class _CSharpLexer
%extends LexerBase
%unicode
%function advanceImpl
%type IElementType

%state PREPROCESSOR_DIRECTIVE
%state STRING_INTERPOLATION
%state STRING_INTERPOLATION_FORMAT_WAIT
%state STRING_INTERPOLATION_PARSE

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
DEC_SIGNIFICAND = "." {DIGITS} | {DIGITS} ("." {DIGIT_OR_UNDERSCORE}+)?
DEC_EXPONENT = [Ee] [+-]? {DIGIT_OR_UNDERSCORE}*
HEX_FP_LITERAL = {HEX_SIGNIFICAND} {HEX_EXPONENT}
HEX_SIGNIFICAND = 0 [Xx] ({HEX_DIGIT_OR_UNDERSCORE}+ "."? | {HEX_DIGIT_OR_UNDERSCORE}* "." {HEX_DIGIT_OR_UNDERSCORE}+)
HEX_EXPONENT = [Pp] [+-]? {DIGIT_OR_UNDERSCORE}*

MACRO_WHITE_SPACE=[ \t\f]+
MACRO_NEW_LINE=\r\n|\n|\r
MACRO_START={MACRO_NEW_LINE}?{MACRO_WHITE_SPACE}?"#"
%%

<PREPROCESSOR_DIRECTIVE>
{
	{MACRO_NEW_LINE}     {  yybegin(YYINITIAL); return CSharpTokens.WHITE_SPACE; }

	{MACRO_WHITE_SPACE}  {  return CSharpTemplateTokens.PREPROCESSOR_FRAGMENT; }

	[^]                    {  return CSharpTemplateTokens.PREPROCESSOR_FRAGMENT; }
}

<STRING_INTERPOLATION>
{
	"{{"
	{
		return CSharpTokensImpl.INTERPOLATION_STRING_LITERAL;
	}

	"{"
	{
		myStringInterpolationBalance ++;

		beginArgumentExpression();

		push(myHighlight ? YYINITIAL : STRING_INTERPOLATION_PARSE);

		return CSharpTokensImpl.INTERPOLATION_STRING_LITERAL;
	}

	"\\\""
	{
		return CSharpTokensImpl.INTERPOLATION_STRING_LITERAL;
	}

	"\""
	{
		back();

		return CSharpTokensImpl.INTERPOLATION_STRING_LITERAL;
	}

	[^]
	{
		return CSharpTokensImpl.INTERPOLATION_STRING_LITERAL;
	}
}

<STRING_INTERPOLATION_FORMAT_WAIT>
{
	"}"
	{
		yypushback(yylength());

		yybegin(YYINITIAL);
	}

	[^]   { return CfsTokens.FORMAT; }
}

<STRING_INTERPOLATION_PARSE>
{
	"$\""
	{
		push(STRING_INTERPOLATION);
		return CSharpTokensImpl.INTERPOLATION_STRING_LITERAL;
	}

	"}"
	{
		endArgumentExpression();

		if(myStringInterpolationBalance != 0)
		{
			myStringInterpolationBalance --;
			back();
			return CSharpTokensImpl.INTERPOLATION_STRING_LITERAL;
		}
		else
		{
			return CSharpTokensImpl.INTERPOLATION_STRING_LITERAL;
		}
	}

	"("
	{
		if(isInsideInterpolation())
		{
			myInsideStringInterpolationParenthesesBalance ++;
		}
		return CSharpTokensImpl.INTERPOLATION_STRING_LITERAL;
	}

	")"
	{
		if(isInsideInterpolation())
		{
			myInsideStringInterpolationParenthesesBalance --;
		}
		return CSharpTokensImpl.INTERPOLATION_STRING_LITERAL;
	}

	[^]  { return CSharpTokensImpl.INTERPOLATION_STRING_LITERAL; }
}

<YYINITIAL>
{
	{MACRO_START}
	{
		yypushback(yylength());
		yybegin(PREPROCESSOR_DIRECTIVE);
	}

	"$\""
	{
		push(STRING_INTERPOLATION);
		return CSharpTokensImpl.INTERPOLATION_STRING_LITERAL;
	}

	"$@\""
	{
		push(STRING_INTERPOLATION);
		return CSharpTokensImpl.INTERPOLATION_STRING_LITERAL;
	}

	{VERBATIM_STRING_LITERAL}
	{
		return CSharpTokens.VERBATIM_STRING_LITERAL;
	}

	"__arglist"               { return CSharpTokens.__ARGLIST_KEYWORD; }

	"__makeref"               { return CSharpTokens.__MAKEREF_KEYWORD; }

	"__reftype"               { return CSharpTokens.__REFTYPE_KEYWORD; }

	"__refvalue"              { return CSharpTokens.__REFVALUE_KEYWORD; }

	"using"                   { return CSharpTokens.USING_KEYWORD; }

// native types
	"string"                  { return CSharpTokens.STRING_KEYWORD; }

	"void"                    { return CSharpTokens.VOID_KEYWORD; }

	"int"                     { return CSharpTokens.INT_KEYWORD; }

	"bool"                    { return CSharpTokens.BOOL_KEYWORD; }

	"byte"                    { return CSharpTokens.BYTE_KEYWORD; }

	"char"                    { return CSharpTokens.CHAR_KEYWORD; }

	"decimal"                 { return CSharpTokens.DECIMAL_KEYWORD; }

	"double"                  { return CSharpTokens.DOUBLE_KEYWORD; }

	"float"                   { return CSharpTokens.FLOAT_KEYWORD; }

	"long"                    { return CSharpTokens.LONG_KEYWORD; }

	"object"                  { return CSharpTokens.OBJECT_KEYWORD; }

	"sbyte"                   { return CSharpTokens.SBYTE_KEYWORD; }

	"short"                   { return CSharpTokens.SHORT_KEYWORD; }

	"uint"                    { return CSharpTokens.UINT_KEYWORD; }

	"ulong"                   { return CSharpTokens.ULONG_KEYWORD; }

	"ushort"                  { return CSharpTokens.USHORT_KEYWORD; }

	"dynamic"                 { return CSharpTokens.DYNAMIC_KEYWORD; }

	"explicit"                { return CSharpTokens.EXPLICIT_KEYWORD; }

	"implicit"                { return CSharpTokens.IMPLICIT_KEYWORD; }

// modifier tokens
	"static"                  { return CSharpTokens.STATIC_KEYWORD; }

	"public"                  { return CSharpTokens.PUBLIC_KEYWORD; }

	"in"                      { return CSharpTokens.IN_KEYWORD; }

	"out"                     { return CSharpTokens.OUT_KEYWORD; }

	"abstract"                { return CSharpTokens.ABSTRACT_KEYWORD; }

	"extern"                  { return CSharpTokens.EXTERN_KEYWORD; }

	"internal"                { return CSharpTokens.INTERNAL_KEYWORD; }

	"override"                { return CSharpTokens.OVERRIDE_KEYWORD; }

	"params"                  { return CSharpTokens.PARAMS_KEYWORD; }

	"private"                 { return CSharpTokens.PRIVATE_KEYWORD; }

	"protected"               { return CSharpTokens.PROTECTED_KEYWORD; }

	"ref"                     { return CSharpTokens.REF_KEYWORD; }

	"readonly"                { return CSharpTokens.READONLY_KEYWORD; }

	"operator"                { return CSharpTokens.OPERATOR_KEYWORD; }

	"sealed"                  { return CSharpTokens.SEALED_KEYWORD; }

	"unsafe"                  { return CSharpTokens.UNSAFE_KEYWORD; }

	"checked"                 { return CSharpTokens.CHECKED_KEYWORD; }

	"unchecked"               { return CSharpTokens.UNCHECKED_KEYWORD; }

	"virtual"                 { return CSharpTokens.VIRTUAL_KEYWORD; }

	"volatile"                { return CSharpTokens.VOLATILE_KEYWORD; }

// declaration tokens
	"namespace"               { return CSharpTokens.NAMESPACE_KEYWORD; }

	"class"                   { return CSharpTokens.CLASS_KEYWORD; }

	"interface"               { return CSharpTokens.INTERFACE_KEYWORD; }

	"struct"                  { return CSharpTokens.STRUCT_KEYWORD; }

	"enum"                    { return CSharpTokens.ENUM_KEYWORD; }

	"event"                   { return CSharpTokens.EVENT_KEYWORD; }

	"delegate"                { return CSharpTokens.DELEGATE_KEYWORD; }

	"const"                   { return CSharpTokens.CONST_KEYWORD; }

// expression tokens
	"new"                     { return CSharpTokens.NEW_KEYWORD; }

	"stackalloc"              { return CSharpTokens.STACKALLOC_KEYWORD; }

	"typeof"                  { return CSharpTokens.TYPEOF_KEYWORD; }

	"sizeof"                  { return CSharpTokens.SIZEOF_KEYWORD; }

	"fixed"                   { return CSharpTokens.FIXED_KEYWORD; }

	"is"                      { return CSharpTokens.IS_KEYWORD; }

	"switch"                  { return CSharpTokens.SWITCH_KEYWORD; }

	"case"                    { return CSharpTokens.CASE_KEYWORD; }

	"default"                 { return CSharpTokens.DEFAULT_KEYWORD; }

	"as"                      { return CSharpTokens.AS_KEYWORD; }

	"lock"                    { return CSharpTokens.LOCK_KEYWORD; }

	"return"                  { return CSharpTokens.RETURN_KEYWORD; }

	"do"                      { return CSharpTokens.DO_KEYWORD; }

	"while"                   { return CSharpTokens.WHILE_KEYWORD; }

	"try"                     { return CSharpTokens.TRY_KEYWORD; }

	"catch"                   { return CSharpTokens.CATCH_KEYWORD; }

	"finally"                 { return CSharpTokens.FINALLY_KEYWORD; }

	"throw"                   { return CSharpTokens.THROW_KEYWORD; }

	"goto"                    { return CSharpTokens.GOTO_KEYWORD; }

	"foreach"                 { return CSharpTokens.FOREACH_KEYWORD; }

	"for"                     { return CSharpTokens.FOR_KEYWORD; }

	"break"                   { return CSharpTokens.BREAK_KEYWORD; }

	"continue"                { return CSharpTokens.CONTINUE_KEYWORD; }

	"base"                    { return CSharpTokens.BASE_KEYWORD; }

	"this"                    { return CSharpTokens.THIS_KEYWORD; }

	"if"                      { return CSharpTokens.IF_KEYWORD; }

	"else"                    { return CSharpTokens.ELSE_KEYWORD; }

//
	"{"                       { return CSharpTokens.LBRACE; }

	"}"
	{
		if(myStringInterpolationBalance != 0)
		{
			endArgumentExpression();

			myStringInterpolationBalance --;
			back();
			return CSharpTokensImpl.INTERPOLATION_STRING_LITERAL;
		}
		else
		{
			return CSharpTokens.RBRACE;
		}
	}

	"["                       { return CSharpTokens.LBRACKET; }

	"]"                       { return CSharpTokens.RBRACKET; }

	"("
	{
		if(isInsideInterpolation())
		{
			myInsideStringInterpolationParenthesesBalance ++;
		}
		return CSharpTokens.LPAR;
	}

	")"
	{
		if(isInsideInterpolation())
		{
			myInsideStringInterpolationParenthesesBalance --;
		}
		return CSharpTokens.RPAR;
	}

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

	":"
	{
		if(isInsideInterpolation() && myInsideStringInterpolationParenthesesBalance == 0)
		{
			yybegin(STRING_INTERPOLATION_FORMAT_WAIT);
			return CfsTokens.COLON;
		}
		return CSharpTokens.COLON;
	}

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

	{SINGLE_LINE_DOC_COMMENT} { return CSharpTokensImpl.LINE_DOC_COMMENT;}

	{SINGLE_LINE_COMMENT}     { return CSharpTokens.LINE_COMMENT; }

	{MULTI_LINE_STYLE_COMMENT} { return CSharpTokens.BLOCK_COMMENT; }

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

	[^]                       { return CSharpTokens.BAD_CHARACTER; }
}
