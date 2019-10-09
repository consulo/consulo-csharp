package consulo.csharp.lang.lexer;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import consulo.csharp.cfs.lang.CfsTokens;

%%

%{
  private IElementType myArgumentElementType;
  private int myInsideParenthesesBalance;

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
		if(myInsideParenthesesBalance > 0)
		{
			return myArgumentElementType;
		}
		else
		{
			yybegin(FORMAT_WAIT);
			return CfsTokens.COLON;
		}
   }

   "}" { yybegin(YYINITIAL); return CfsTokens.END; }

   [^]   { return myArgumentElementType; }
}

<FORMAT_WAIT>
{
   "}" { yybegin(YYINITIAL); return CfsTokens.END; }

   [^]   { return CfsTokens.FORMAT; }
}