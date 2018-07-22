package consulo.csharp.lang.lexer;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import consulo.csharp.cfs.lang.CfsTokens;

%%

%{
  private IElementType myArgumentElementType;

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

%%

<YYINITIAL>
{
   "{" { yybegin(ARGUMENT_WAIT); return CfsTokens.START; }

   [^]   { return CfsTokens.TEXT; }
}

<ARGUMENT_WAIT>
{
   "}" { yybegin(YYINITIAL); return CfsTokens.END; }

   [^]   { return myArgumentElementType; }
}