package org.mustbe.consulo.csharp.lang.lexer;

import org.mustbe.consulo.csharp.lang.psi.CSharpPreprocessorTokens;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;

%%

%{
  private boolean myEnteredNewLine = true;
%}

%class _CSharpPreprocessorLexer
%extends LexerBase
%unicode
%function advanceImpl
%type IElementType
%eof{  return;
%eof}

%state EXPRESSION_DIRECTIVE_VALUE
%state SIMPLE_DIRECTIVE_VALUE
%state NO_DIRECTIVE_VALUE
%state REGION_DIRECTIVE_VALUE
%state DIRECTIVE

SINGLE_LINE_COMMENT="/""/"[^\r\n]*
MULTI_LINE_STYLE_COMMENT=("/*"[^"*"]{COMMENT_TAIL})|"/*"
COMMENT_TAIL=([^"*"]*("*"+[^"*""/"])?)*("*"+"/")?

WHITE_SPACE_NO_NEW_LINE=[ \t\f]+
NEW_LINE=\r\n|\n|\r

IDENTIFIER=[:jletter:] [:jletterdigit:]*
%%

<EXPRESSION_DIRECTIVE_VALUE>
{
	"("                  { return CSharpPreprocessorTokens.LPAR; }

	")"                  { return CSharpPreprocessorTokens.RPAR; }

	"!"                  { return CSharpPreprocessorTokens.EXCL; }

	"&&"                 { return CSharpPreprocessorTokens.ANDAND; }

	"||"                 { return CSharpPreprocessorTokens.OROR; }

	{IDENTIFIER}         { return CSharpPreprocessorTokens.IDENTIFIER; }

	{NEW_LINE}
	{
		myEnteredNewLine = true;
		yybegin(YYINITIAL);
		return CSharpPreprocessorTokens.WHITE_SPACE;
	}

	{WHITE_SPACE_NO_NEW_LINE}
	{
		return CSharpPreprocessorTokens.WHITE_SPACE;
	}

	[^]
	{
		return CSharpPreprocessorTokens.SIMPLE_VALUE;
	}
}

<REGION_DIRECTIVE_VALUE>
{
	{NEW_LINE}
	{
		myEnteredNewLine = true;
		yybegin(YYINITIAL);
		return CSharpPreprocessorTokens.WHITE_SPACE;
	}

	{WHITE_SPACE_NO_NEW_LINE}
	{
		return CSharpPreprocessorTokens.WHITE_SPACE;
	}

	[^]
	{
		return CSharpPreprocessorTokens.SIMPLE_VALUE;
	}
}

<NO_DIRECTIVE_VALUE>
{
	{NEW_LINE}
	{
		myEnteredNewLine = true;
		yybegin(YYINITIAL);
		return CSharpPreprocessorTokens.WHITE_SPACE;
	}

	{WHITE_SPACE_NO_NEW_LINE}
	{
		return CSharpPreprocessorTokens.WHITE_SPACE;
	}

	{SINGLE_LINE_COMMENT}
	{
		return CSharpPreprocessorTokens.COMMENT;
	}

	[^]
	{
		return CSharpPreprocessorTokens.SIMPLE_VALUE;
	}
}

<DIRECTIVE>
{
	{WHITE_SPACE_NO_NEW_LINE}
	{
		return CSharpPreprocessorTokens.WHITE_SPACE;
	}

	{SINGLE_LINE_COMMENT}
	{
		return CSharpPreprocessorTokens.COMMENT;
	}

	"region"
	{
		yybegin(REGION_DIRECTIVE_VALUE);
		return CSharpPreprocessorTokens.REGION_KEYWORD;
	}

	"endregion"
	{
		yybegin(NO_DIRECTIVE_VALUE);
		return CSharpPreprocessorTokens.ENDREGION_KEYWORD;
	}

	"define"
	{
		yybegin(NO_DIRECTIVE_VALUE);
		return CSharpPreprocessorTokens.DEFINE_KEYWORD;
	}

	"undef"
	{
		yybegin(EXPRESSION_DIRECTIVE_VALUE);
		return CSharpPreprocessorTokens.UNDEF_KEYWORD;
	}

	"if"
	{
		yybegin(EXPRESSION_DIRECTIVE_VALUE);
		return CSharpPreprocessorTokens.IF_KEYWORD;
	}

	"endif"
	{
		yybegin(NO_DIRECTIVE_VALUE);
		return CSharpPreprocessorTokens.ENDIF_KEYWORD;
	}

	"else"
	{
		yybegin(EXPRESSION_DIRECTIVE_VALUE);
		return CSharpPreprocessorTokens.ELSE_KEYWORD;
	}

	"elif"
	{
		yybegin(EXPRESSION_DIRECTIVE_VALUE);
		return CSharpPreprocessorTokens.ELIF_KEYWORD;
	}

	{NEW_LINE}
	{
		myEnteredNewLine = true;
		yybegin(YYINITIAL);
		return CSharpPreprocessorTokens.WHITE_SPACE;
	}

	[^]
	{
		return CSharpPreprocessorTokens.BAD_CHARACTER;
	}
}

<YYINITIAL>
{
	{NEW_LINE}
	{
		myEnteredNewLine = true;
		return CSharpPreprocessorTokens.WHITE_SPACE;
	}

	{MULTI_LINE_STYLE_COMMENT}
	{
		myEnteredNewLine = false;
		return CSharpPreprocessorTokens.CSHARP_FRAGMENT;
	}

	{SINGLE_LINE_COMMENT}
	{
		myEnteredNewLine = false;
		return CSharpPreprocessorTokens.CSHARP_FRAGMENT;
	}

	"#"
	{
		if(myEnteredNewLine)
		{
			yybegin(DIRECTIVE);
			return CSharpPreprocessorTokens.SHARP;
		}
		else
		{
			return CSharpPreprocessorTokens.BAD_CHARACTER;
		}
	}

	{WHITE_SPACE_NO_NEW_LINE}
	{
		return CSharpPreprocessorTokens.CSHARP_FRAGMENT;
	}

	.
	{
		myEnteredNewLine = false;
		return CSharpPreprocessorTokens.CSHARP_FRAGMENT;
	}
}
