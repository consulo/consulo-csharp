 /* It's an automatically generated code. Do not modify it. */
package org.mustbe.consulo.csharp.lang.doc.lexer;

import com.intellij.psi.tree.IElementType;
import com.intellij.lexer.LexerBase;
import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocTokenType;

%%

%{
  private int myPrevState = YYINITIAL;

  public int yyprevstate() {
    return myPrevState;
  }

  private int popState(){
    final int prev = myPrevState;
    myPrevState = YYINITIAL;
    return prev;
  }

  protected void pushState(int state){
    myPrevState = state;
  }
%}

%unicode
%class __CSharpDocLexer
%extends LexerBase
%function advanceImpl
%type IElementType
%eof{  return;
%eof}

%state TAG
%state END_TAG
%xstate COMMENT
%state ATTR_LIST
%state ATTR
%state ATTR_VALUE_START
%state ATTR_VALUE_DQ
%state ATTR_VALUE_SQ
%state C_COMMENT_START
%state C_COMMENT_END

ALPHA=[:letter:]
DIGIT=[0-9]
WS=[\ \n\r\t\f]
S={WS}+

NAME=({ALPHA}|"_")({ALPHA}|{DIGIT}|"_"|"."|"-")*(":"({ALPHA}|"_")?({ALPHA}|{DIGIT}|"_"|"."|"-")*)?

END_COMMENT="-->"

%%

"<!--" { yybegin(COMMENT); return CSharpDocTokenType.XML_COMMENT_START; }
<COMMENT> "[" { return CSharpDocTokenType.XML_COMMENT_CHARACTERS; }
<COMMENT> "<![" { return CSharpDocTokenType.XML_COMMENT_CHARACTERS; }
<COMMENT> {END_COMMENT} { yybegin(YYINITIAL); return CSharpDocTokenType.XML_COMMENT_END; }
<COMMENT> [^\-]|(-[^\-]) { return CSharpDocTokenType.XML_COMMENT_CHARACTERS; }
<COMMENT> [^] { return CSharpDocTokenType.XML_BAD_CHARACTER; }

<C_COMMENT_START> [^] { yybegin(COMMENT); return CSharpDocTokenType.XML_COMMENT_CHARACTERS; }
<C_COMMENT_START,C_COMMENT_END> {END_COMMENT} { yybegin(YYINITIAL); return CSharpDocTokenType.XML_COMMENT_END; }
<C_COMMENT_END> [^] { yybegin(COMMENT); return CSharpDocTokenType.XML_COMMENT_CHARACTERS; }

<YYINITIAL> "<" { yybegin(TAG); return CSharpDocTokenType.XML_START_TAG_START; }
<TAG> {NAME} { yybegin(ATTR_LIST); pushState(TAG); return CSharpDocTokenType.XML_NAME; }
<TAG> "/>" { yybegin(YYINITIAL); return CSharpDocTokenType.XML_EMPTY_ELEMENT_END; }
<TAG> ">" { yybegin(YYINITIAL); return CSharpDocTokenType.XML_TAG_END; }

<YYINITIAL> "</" { yybegin(END_TAG); return CSharpDocTokenType.XML_END_TAG_START; }
<END_TAG> {NAME} { return CSharpDocTokenType.XML_NAME; }
<END_TAG> ">" { yybegin(YYINITIAL); return CSharpDocTokenType.XML_TAG_END; }

<ATTR_LIST> {NAME} {yybegin(ATTR); return CSharpDocTokenType.XML_NAME;}
<ATTR> "=" { return CSharpDocTokenType.XML_EQ;}
<ATTR> "'" { yybegin(ATTR_VALUE_SQ); return CSharpDocTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER;}
<ATTR> "\"" { yybegin(ATTR_VALUE_DQ); return CSharpDocTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER;}
<ATTR> [^\ \n\r\t\f] {yybegin(ATTR_LIST); yypushback(yylength()); }

<ATTR_VALUE_DQ>{
  "\"" { yybegin(ATTR_LIST); return CSharpDocTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER;}
  [^] { return CSharpDocTokenType.XML_ATTRIBUTE_VALUE_TOKEN;}
}

<ATTR_VALUE_SQ>{
  "'" { yybegin(ATTR_LIST); return CSharpDocTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER;}
  [^] { return CSharpDocTokenType.XML_ATTRIBUTE_VALUE_TOKEN;}
}

<YYINITIAL> {S} { return CSharpDocTokenType.XML_REAL_WHITE_SPACE; }
<TAG, END_TAG, ATTR_LIST, ATTR> {S} { return CSharpDocTokenType.TAG_WHITE_SPACE; }
<YYINITIAL> ([^< \n\r\t\f])* { return CSharpDocTokenType.XML_DATA_CHARACTERS; }
<YYINITIAL> [^<\ \n\r\t\f] { return CSharpDocTokenType.XML_DATA_CHARACTERS; }

[^] { if(yystate() == YYINITIAL){
        return CSharpDocTokenType.XML_BAD_CHARACTER;
      }
      else yybegin(popState()); yypushback(yylength());
      }
