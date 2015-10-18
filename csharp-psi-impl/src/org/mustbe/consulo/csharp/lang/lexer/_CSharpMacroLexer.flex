package org.mustbe.consulo.csharp.lang.lexer;

import java.util.*;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroTokens;

%%

%{
  private boolean myEnteredNewLine = true;
%}

%class _CSharpMacroLexer
%extends LexerBase
%unicode
%function advanceImpl
%type IElementType
%eof{  return;
%eof}

%state EXPRESSION_DIRECTIVE_VALUE
%state SIMPLE_DIRECTIVE_VALUE
%state NO_DIRECTIVE_VALUE
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
	"("                  { return CSharpMacroTokens.LPAR; }

	")"                  { return CSharpMacroTokens.RPAR; }

	"!"                  { return CSharpMacroTokens.EXCL; }

	"&&"                 { return CSharpMacroTokens.ANDAND; }

	"||"                 { return CSharpMacroTokens.OROR; }

	{IDENTIFIER}         { return CSharpMacroTokens.IDENTIFIER; }

	{NEW_LINE}
	{
		myEnteredNewLine = true;
		yybegin(YYINITIAL);
		return CSharpMacroTokens.CSHARP_FRAGMENT;
	}

	{WHITE_SPACE_NO_NEW_LINE}
	{
		return CSharpMacroTokens.WHITE_SPACE;
	}

	[^]
	{
		return CSharpMacroTokens.SIMPLE_VALUE;
	}
}

<SIMPLE_DIRECTIVE_VALUE>
{
	{NEW_LINE}
	{
		myEnteredNewLine = true;
		yybegin(YYINITIAL);
		return CSharpMacroTokens.CSHARP_FRAGMENT;
	}

	{WHITE_SPACE_NO_NEW_LINE}
	{
		return CSharpMacroTokens.WHITE_SPACE;
	}

	[^]
	{
		return CSharpMacroTokens.SIMPLE_VALUE;
	}
}

<NO_DIRECTIVE_VALUE>
{
	{NEW_LINE}
	{
		myEnteredNewLine = true;
		yybegin(YYINITIAL);
		return CSharpMacroTokens.CSHARP_FRAGMENT;
	}

	{SINGLE_LINE_COMMENT}
	{
		return CSharpMacroTokens.COMMENT;
	}

	[^]
	{
		return CSharpMacroTokens.SIMPLE_VALUE;
	}
}

<DIRECTIVE>
{
	{WHITE_SPACE_NO_NEW_LINE}
	{
		return CSharpMacroTokens.WHITE_SPACE;
	}

	{SINGLE_LINE_COMMENT}
	{
		return CSharpMacroTokens.COMMENT;
	}

	"region"
	{
		yybegin(SIMPLE_DIRECTIVE_VALUE);
		return CSharpMacroTokens.REGION_KEYWORD;
	}

	"endregion"
	{
		yybegin(NO_DIRECTIVE_VALUE);
		return CSharpMacroTokens.ENDREGION_KEYWORD;
	}

	"define"
	{
		yybegin(SIMPLE_DIRECTIVE_VALUE);
		return CSharpMacroTokens.DEFINE_KEYWORD;
	}

	"undef"
	{
		yybegin(SIMPLE_DIRECTIVE_VALUE);
		return CSharpMacroTokens.UNDEF_KEYWORD;
	}

	"if"
	{
		yybegin(EXPRESSION_DIRECTIVE_VALUE);
		return CSharpMacroTokens.IF_KEYWORD;
	}

	"endif"
	{
		yybegin(NO_DIRECTIVE_VALUE);
		return CSharpMacroTokens.ENDIF_KEYWORD;
	}

	"else"
	{
		yybegin(EXPRESSION_DIRECTIVE_VALUE);
		return CSharpMacroTokens.ELSE_KEYWORD;
	}

	"elif"
	{
		yybegin(EXPRESSION_DIRECTIVE_VALUE);
		return CSharpMacroTokens.ELIF_KEYWORD;
	}

	{NEW_LINE}
	{
		myEnteredNewLine = true;
		yybegin(YYINITIAL);
		return CSharpMacroTokens.CSHARP_FRAGMENT;
	}

	[^]
	{
		return CSharpMacroTokens.BAD_CHARACTER;
	}
}

<YYINITIAL>
{
	{NEW_LINE}
	{
		myEnteredNewLine = true;
		return CSharpMacroTokens.CSHARP_FRAGMENT;
	}

	{MULTI_LINE_STYLE_COMMENT}
	{
		myEnteredNewLine = false;
		return CSharpMacroTokens.CSHARP_FRAGMENT;
	}

	{SINGLE_LINE_COMMENT}
	{
		myEnteredNewLine = false;
		return CSharpMacroTokens.CSHARP_FRAGMENT;
	}

	"#"
	{
		if(myEnteredNewLine)
		{
			yybegin(DIRECTIVE);
			return CSharpMacroTokens.SHARP;
		}
		else
		{
			return CSharpMacroTokens.BAD_CHARACTER;
		}
	}

	{WHITE_SPACE_NO_NEW_LINE}
	{
		return CSharpMacroTokens.CSHARP_FRAGMENT;
	}

	.
	{
		myEnteredNewLine = false;
		return CSharpMacroTokens.CSHARP_FRAGMENT;
	}
}
