package consulo.csharp.cfs.impl.lexer;

import consulo.language.lexer.LexerBase;
import consulo.language.ast.IElementType;
import consulo.dotnet.cfs.lang.CfsTokens;

%%

%{
  private IElementType myArgumentElementType;
  private int myInsideParenthesesBalance;
  // braces inside the argument - {x switch { 1 => "one", _ => "other" }}
  private int myInsideBracesBalance;

  public CSharpInterpolationStringLexer(IElementType argumentElementType) {
     myArgumentElementType = argumentElementType;
  }
%}

%public
%class CSharpInterpolationStringLexer
%extends LexerBase
%unicode
%function advanceImpl
%type IElementType
%eof{  return;
%eof}

%state ARGUMENT_WAIT
%state FORMAT_WAIT

%%

<YYINITIAL>
{
   "{{" { return CfsTokens.TEXT; }

   "{" { yybegin(ARGUMENT_WAIT); return CfsTokens.START; }

   [^]   { return CfsTokens.TEXT; }
}

<ARGUMENT_WAIT>
{
	"("
	{
		myInsideParenthesesBalance ++;
		return myArgumentElementType;
	}

	")"
	{
		myInsideParenthesesBalance --;
		return myArgumentElementType;
	}

	":"
	{
		if(myInsideParenthesesBalance > 0 || myInsideBracesBalance > 0)
		{
			return myArgumentElementType;
		}
		else
		{
			yybegin(FORMAT_WAIT);
			return CfsTokens.COLON;
		}
   }

	"{"
	{
		myInsideBracesBalance ++;
		return myArgumentElementType;
	}

	"}"
	{
		if(myInsideBracesBalance > 0)
		{
			myInsideBracesBalance --;
			return myArgumentElementType;
		}
		yybegin(YYINITIAL);
		return CfsTokens.END;
	}

   [^]   { return myArgumentElementType; }
}

<FORMAT_WAIT>
{
   "}" { yybegin(YYINITIAL); return CfsTokens.END; }

   [^]   { return CfsTokens.FORMAT; }
}