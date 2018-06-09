/* The following code was generated by JFlex 1.4.4 on 6/9/18 3:43 PM */

package consulo.csharp.lang.lexer;

import java.util.*;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import consulo.csharp.lang.psi.CSharpPreprocesorTokens;


/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.4.4
 * on 6/9/18 3:43 PM from the specification file
 * <tt>W:/_github.com/consulo/consulo-csharp/csharp-psi-impl/src/main/java/consulo/csharp/lang/lexer/_CSharpMacroLexer.flex</tt>
 */
public class _CSharpMacroLexer extends LexerBase {
  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int MACRO_EXPRESSION = 4;
  public static final int YYINITIAL = 0;
  public static final int MACRO_ENTERED = 2;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0,  0,  1,  1,  2, 2
  };

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = 
    "\11\3\1\1\1\5\1\0\1\1\1\5\16\3\4\0\1\1\1\30"+
    "\1\0\1\6\1\2\1\0\1\31\1\0\1\26\1\27\5\0\1\4"+
    "\12\3\7\0\32\2\4\0\1\2\1\0\1\23\2\2\1\7\1\10"+
    "\1\11\1\16\1\2\1\12\2\2\1\20\1\24\1\13\1\17\1\22"+
    "\1\2\1\15\1\21\1\2\1\14\1\2\1\25\3\2\1\0\1\32"+
    "\2\0\41\3\2\0\4\2\4\0\1\2\2\0\1\3\7\0\1\2"+
    "\4\0\1\2\5\0\27\2\1\0\37\2\1\0\u01ca\2\4\0\14\2"+
    "\16\0\5\2\7\0\1\2\1\0\1\2\21\0\160\3\5\2\1\0"+
    "\2\2\2\0\4\2\10\0\1\2\1\0\3\2\1\0\1\2\1\0"+
    "\24\2\1\0\123\2\1\0\213\2\1\0\5\3\2\0\236\2\11\0"+
    "\46\2\2\0\1\2\7\0\47\2\7\0\1\2\1\0\55\3\1\0"+
    "\1\3\1\0\2\3\1\0\2\3\1\0\1\3\10\0\33\2\5\0"+
    "\3\2\15\0\5\3\6\0\1\2\4\0\13\3\5\0\53\2\37\3"+
    "\4\0\2\2\1\3\143\2\1\0\1\2\10\3\1\0\6\3\2\2"+
    "\2\3\1\0\4\3\2\2\12\3\3\2\2\0\1\2\17\0\1\3"+
    "\1\2\1\3\36\2\33\3\2\0\131\2\13\3\1\2\16\0\12\3"+
    "\41\2\11\3\2\2\4\0\1\2\5\0\26\2\4\3\1\2\11\3"+
    "\1\2\3\3\1\2\5\3\22\0\31\2\3\3\104\0\1\2\1\0"+
    "\13\2\67\0\33\3\1\0\4\3\66\2\3\3\1\2\22\3\1\2"+
    "\7\3\12\2\2\3\2\0\12\3\1\0\7\2\1\0\7\2\1\0"+
    "\3\3\1\0\10\2\2\0\2\2\2\0\26\2\1\0\7\2\1\0"+
    "\1\2\3\0\4\2\2\0\1\3\1\2\7\3\2\0\2\3\2\0"+
    "\3\3\1\2\10\0\1\3\4\0\2\2\1\0\3\2\2\3\2\0"+
    "\12\3\4\2\7\0\1\2\5\0\3\3\1\0\6\2\4\0\2\2"+
    "\2\0\26\2\1\0\7\2\1\0\2\2\1\0\2\2\1\0\2\2"+
    "\2\0\1\3\1\0\5\3\4\0\2\3\2\0\3\3\3\0\1\3"+
    "\7\0\4\2\1\0\1\2\7\0\14\3\3\2\1\3\13\0\3\3"+
    "\1\0\11\2\1\0\3\2\1\0\26\2\1\0\7\2\1\0\2\2"+
    "\1\0\5\2\2\0\1\3\1\2\10\3\1\0\3\3\1\0\3\3"+
    "\2\0\1\2\17\0\2\2\2\3\2\0\12\3\1\0\1\2\17\0"+
    "\3\3\1\0\10\2\2\0\2\2\2\0\26\2\1\0\7\2\1\0"+
    "\2\2\1\0\5\2\2\0\1\3\1\2\7\3\2\0\2\3\2\0"+
    "\3\3\10\0\2\3\4\0\2\2\1\0\3\2\2\3\2\0\12\3"+
    "\1\0\1\2\20\0\1\3\1\2\1\0\6\2\3\0\3\2\1\0"+
    "\4\2\3\0\2\2\1\0\1\2\1\0\2\2\3\0\2\2\3\0"+
    "\3\2\3\0\14\2\4\0\5\3\3\0\3\3\1\0\4\3\2\0"+
    "\1\2\6\0\1\3\16\0\12\3\11\0\1\2\7\0\3\3\1\0"+
    "\10\2\1\0\3\2\1\0\27\2\1\0\12\2\1\0\5\2\3\0"+
    "\1\2\7\3\1\0\3\3\1\0\4\3\7\0\2\3\1\0\2\2"+
    "\6\0\2\2\2\3\2\0\12\3\22\0\2\3\1\0\10\2\1\0"+
    "\3\2\1\0\27\2\1\0\12\2\1\0\5\2\2\0\1\3\1\2"+
    "\7\3\1\0\3\3\1\0\4\3\7\0\2\3\7\0\1\2\1\0"+
    "\2\2\2\3\2\0\12\3\1\0\2\2\17\0\2\3\1\0\10\2"+
    "\1\0\3\2\1\0\51\2\2\0\1\2\7\3\1\0\3\3\1\0"+
    "\4\3\1\2\10\0\1\3\10\0\2\2\2\3\2\0\12\3\12\0"+
    "\6\2\2\0\2\3\1\0\22\2\3\0\30\2\1\0\11\2\1\0"+
    "\1\2\2\0\7\2\3\0\1\3\4\0\6\3\1\0\1\3\1\0"+
    "\10\3\22\0\2\3\15\0\60\2\1\3\2\2\7\3\4\0\10\2"+
    "\10\3\1\0\12\3\47\0\2\2\1\0\1\2\2\0\2\2\1\0"+
    "\1\2\2\0\1\2\6\0\4\2\1\0\7\2\1\0\3\2\1\0"+
    "\1\2\1\0\1\2\2\0\2\2\1\0\4\2\1\3\2\2\6\3"+
    "\1\0\2\3\1\2\2\0\5\2\1\0\1\2\1\0\6\3\2\0"+
    "\12\3\2\0\4\2\40\0\1\2\27\0\2\3\6\0\12\3\13\0"+
    "\1\3\1\0\1\3\1\0\1\3\4\0\2\3\10\2\1\0\44\2"+
    "\4\0\24\3\1\0\2\3\5\2\13\3\1\0\44\3\11\0\1\3"+
    "\71\0\53\2\24\3\1\2\12\3\6\0\6\2\4\3\4\2\3\3"+
    "\1\2\3\3\2\2\7\3\3\2\4\3\15\2\14\3\1\2\17\3"+
    "\2\0\46\2\1\0\1\2\5\0\1\2\2\0\53\2\1\0\u014d\2"+
    "\1\0\4\2\2\0\7\2\1\0\1\2\1\0\4\2\2\0\51\2"+
    "\1\0\4\2\2\0\41\2\1\0\4\2\2\0\7\2\1\0\1\2"+
    "\1\0\4\2\2\0\17\2\1\0\71\2\1\0\4\2\2\0\103\2"+
    "\2\0\3\3\40\0\20\2\20\0\125\2\14\0\u026c\2\2\0\21\2"+
    "\1\0\32\2\5\0\113\2\3\0\3\2\17\0\15\2\1\0\4\2"+
    "\3\3\13\0\22\2\3\3\13\0\22\2\2\3\14\0\15\2\1\0"+
    "\3\2\1\0\2\3\14\0\64\2\40\3\3\0\1\2\3\0\2\2"+
    "\1\3\2\0\12\3\41\0\3\3\2\0\12\3\6\0\130\2\10\0"+
    "\51\2\1\3\1\2\5\0\106\2\12\0\35\2\3\0\14\3\4\0"+
    "\14\3\12\0\12\3\36\2\2\0\5\2\13\0\54\2\4\0\21\3"+
    "\7\2\2\3\6\0\12\3\46\0\27\2\5\3\4\0\65\2\12\3"+
    "\1\0\35\3\2\0\13\3\6\0\12\3\15\0\1\2\130\0\5\3"+
    "\57\2\21\3\7\2\4\0\12\3\21\0\11\3\14\0\3\3\36\2"+
    "\15\3\2\2\12\3\54\2\16\3\14\0\44\2\24\3\10\0\12\3"+
    "\3\0\3\2\12\3\44\2\122\0\3\3\1\0\25\3\4\2\1\3"+
    "\4\2\3\3\2\2\11\0\300\2\47\3\25\0\4\3\u0116\2\2\0"+
    "\6\2\2\0\46\2\2\0\6\2\2\0\10\2\1\0\1\2\1\0"+
    "\1\2\1\0\1\2\1\0\37\2\2\0\65\2\1\0\7\2\1\0"+
    "\1\2\3\0\3\2\1\0\7\2\3\0\4\2\2\0\6\2\4\0"+
    "\15\2\5\0\3\2\1\0\7\2\16\0\5\3\32\0\5\3\20\0"+
    "\2\2\23\0\1\2\13\0\5\3\5\0\6\3\1\0\1\2\15\0"+
    "\1\2\20\0\15\2\3\0\33\2\25\0\15\3\4\0\1\3\3\0"+
    "\14\3\21\0\1\2\4\0\1\2\2\0\12\2\1\0\1\2\3\0"+
    "\5\2\6\0\1\2\1\0\1\2\1\0\1\2\1\0\4\2\1\0"+
    "\13\2\2\0\4\2\5\0\5\2\4\0\1\2\21\0\51\2\u0a77\0"+
    "\57\2\1\0\57\2\1\0\205\2\6\0\4\2\3\3\2\2\14\0"+
    "\46\2\1\0\1\2\5\0\1\2\2\0\70\2\7\0\1\2\17\0"+
    "\1\3\27\2\11\0\7\2\1\0\7\2\1\0\7\2\1\0\7\2"+
    "\1\0\7\2\1\0\7\2\1\0\7\2\1\0\7\2\1\0\40\3"+
    "\57\0\1\2\u01d5\0\3\2\31\0\11\2\6\3\1\0\5\2\2\0"+
    "\5\2\4\0\126\2\2\0\2\3\2\0\3\2\1\0\132\2\1\0"+
    "\4\2\5\0\51\2\3\0\136\2\21\0\33\2\65\0\20\2\u0200\0"+
    "\u19b6\2\112\0\u51cd\2\63\0\u048d\2\103\0\56\2\2\0\u010d\2\3\0"+
    "\20\2\12\3\2\2\24\0\57\2\1\3\4\0\12\3\1\0\31\2"+
    "\7\0\1\3\120\2\2\3\45\0\11\2\2\0\147\2\2\0\4\2"+
    "\1\0\4\2\14\0\13\2\115\0\12\2\1\3\3\2\1\3\4\2"+
    "\1\3\27\2\5\3\20\0\1\2\7\0\64\2\14\0\2\3\62\2"+
    "\21\3\13\0\12\3\6\0\22\3\6\2\3\0\1\2\4\0\12\3"+
    "\34\2\10\3\2\0\27\2\15\3\14\0\35\2\3\0\4\3\57\2"+
    "\16\3\16\0\1\2\12\3\46\0\51\2\16\3\11\0\3\2\1\3"+
    "\10\2\2\3\2\0\12\3\6\0\27\2\3\0\1\2\1\3\4\0"+
    "\60\2\1\3\1\2\3\3\2\2\2\3\5\2\2\3\1\2\1\3"+
    "\1\2\30\0\3\2\2\0\13\2\5\3\2\0\3\2\2\3\12\0"+
    "\6\2\2\0\6\2\2\0\6\2\11\0\7\2\1\0\7\2\221\0"+
    "\43\2\10\3\1\0\2\3\2\0\12\3\6\0\u2ba4\2\14\0\27\2"+
    "\4\0\61\2\u2104\0\u016e\2\2\0\152\2\46\0\7\2\14\0\5\2"+
    "\5\0\1\2\1\3\12\2\1\0\15\2\1\0\5\2\1\0\1\2"+
    "\1\0\2\2\1\0\2\2\1\0\154\2\41\0\u016b\2\22\0\100\2"+
    "\2\0\66\2\50\0\15\2\3\0\20\3\20\0\7\3\14\0\2\2"+
    "\30\0\3\2\31\0\1\2\6\0\5\2\1\0\207\2\2\0\1\3"+
    "\4\0\1\2\13\0\12\3\7\0\32\2\4\0\1\2\1\0\32\2"+
    "\13\0\131\2\3\0\6\2\2\0\6\2\2\0\6\2\2\0\3\2"+
    "\3\0\2\2\3\0\2\2\22\0\3\3\4\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\3\0\1\1\1\2\2\1\1\3\1\4\1\5\1\6"+
    "\2\1\1\7\10\10\1\11\1\12\4\10\1\13\21\10"+
    "\1\14\1\15\5\10\1\16\1\10\1\17\1\20\3\10"+
    "\1\21\1\10\1\22\1\23\2\10\1\24\1\10\1\25";

  private static int [] zzUnpackAction() {
    int [] result = new int[69];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\33\0\66\0\121\0\154\0\207\0\242\0\275"+
    "\0\121\0\121\0\121\0\330\0\363\0\u010e\0\u0129\0\u0144"+
    "\0\u015f\0\u017a\0\u0195\0\u01b0\0\u01cb\0\u01e6\0\121\0\121"+
    "\0\u0201\0\u021c\0\u0237\0\u0252\0\u0129\0\u026d\0\u0288\0\u02a3"+
    "\0\u02be\0\u02d9\0\u02f4\0\u030f\0\u032a\0\u0345\0\u0360\0\u037b"+
    "\0\u0396\0\u03b1\0\u03cc\0\u03e7\0\u0402\0\u041d\0\u0129\0\u0129"+
    "\0\u0438\0\u0453\0\u046e\0\u0489\0\u04a4\0\u0129\0\u04bf\0\u0129"+
    "\0\u0129\0\u04da\0\u04f5\0\u0510\0\u0129\0\u052b\0\u0129\0\u0129"+
    "\0\u0546\0\u0561\0\u0129\0\u057c\0\u0129";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[69];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\4\1\5\2\4\1\6\1\5\1\7\25\4\1\5"+
    "\1\10\1\4\1\6\1\5\1\4\17\10\6\4\1\5"+
    "\1\10\1\4\1\6\1\5\1\4\17\10\1\11\1\12"+
    "\1\13\1\14\1\15\34\0\1\5\3\0\1\5\31\0"+
    "\1\16\30\0\1\17\4\0\1\20\1\21\1\17\1\22"+
    "\1\17\1\23\1\24\4\17\1\25\2\17\1\26\7\0"+
    "\2\10\3\0\17\10\36\0\1\27\33\0\1\30\5\16"+
    "\1\0\25\16\2\0\2\17\3\0\17\17\7\0\2\17"+
    "\3\0\1\17\1\31\15\17\7\0\2\17\3\0\4\17"+
    "\1\32\1\17\1\33\2\17\1\34\5\17\7\0\2\17"+
    "\3\0\2\17\1\35\14\17\7\0\2\17\3\0\4\17"+
    "\1\36\12\17\7\0\2\17\3\0\1\17\1\37\15\17"+
    "\7\0\2\17\3\0\6\17\1\40\10\17\7\0\2\17"+
    "\3\0\14\17\1\41\2\17\7\0\2\17\3\0\2\17"+
    "\1\42\14\17\7\0\2\17\3\0\1\43\16\17\7\0"+
    "\2\17\3\0\6\17\1\44\10\17\7\0\2\17\3\0"+
    "\3\17\1\45\6\17\1\46\4\17\7\0\2\17\3\0"+
    "\1\47\16\17\7\0\2\17\3\0\7\17\1\50\7\17"+
    "\7\0\2\17\3\0\14\17\1\51\2\17\7\0\2\17"+
    "\3\0\6\17\1\52\10\17\7\0\2\17\3\0\3\17"+
    "\1\53\13\17\7\0\2\17\3\0\3\17\1\54\2\17"+
    "\1\55\10\17\7\0\2\17\3\0\10\17\1\56\6\17"+
    "\7\0\2\17\3\0\2\17\1\57\14\17\7\0\2\17"+
    "\3\0\1\17\1\60\15\17\7\0\2\17\3\0\1\17"+
    "\1\61\15\17\7\0\2\17\3\0\3\17\1\62\13\17"+
    "\7\0\2\17\3\0\7\17\1\63\7\17\7\0\2\17"+
    "\3\0\4\17\1\64\12\17\7\0\2\17\3\0\4\17"+
    "\1\65\12\17\7\0\2\17\3\0\2\17\1\66\14\17"+
    "\7\0\2\17\3\0\1\17\1\67\15\17\7\0\2\17"+
    "\3\0\6\17\1\70\10\17\7\0\2\17\3\0\2\17"+
    "\1\71\14\17\7\0\2\17\3\0\10\17\1\72\6\17"+
    "\7\0\2\17\3\0\15\17\1\73\1\17\7\0\2\17"+
    "\3\0\3\17\1\74\13\17\7\0\2\17\3\0\1\17"+
    "\1\75\15\17\7\0\2\17\3\0\7\17\1\76\7\17"+
    "\7\0\2\17\3\0\4\17\1\77\12\17\7\0\2\17"+
    "\3\0\14\17\1\100\2\17\7\0\2\17\3\0\4\17"+
    "\1\101\12\17\7\0\2\17\3\0\3\17\1\102\13\17"+
    "\7\0\2\17\3\0\7\17\1\103\7\17\7\0\2\17"+
    "\3\0\10\17\1\104\6\17\7\0\2\17\3\0\4\17"+
    "\1\105\12\17\5\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[1431];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;
  private static final char[] EMPTY_BUFFER = new char[0];
  private static final int YYEOF = -1;

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\3\0\1\11\4\1\3\11\13\1\2\11\55\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[69];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private CharSequence zzBuffer = "";

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the textposition at the last state to be included in yytext */
  private int zzPushbackPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /**
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  private IElementType myTokenType;
  private int myState;

  /** denotes if the user-EOF-code has already been executed */
  private boolean zzEOFDone;



  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    char [] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 2214) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }

  @Override
  public IElementType getTokenType() {
    if (myTokenType == null) locateToken();
    return myTokenType;
  }

  @Override
  public final int getTokenStart(){
    if (myTokenType == null) locateToken();
    return zzStartRead;
  }

  @Override
  public final int getTokenEnd(){
    if (myTokenType == null) locateToken();
    return getTokenStart() + yylength();
  }

  @Override
  public void advance() {
    if (myTokenType == null) locateToken();
    myTokenType = null;
  }

  @Override
  public int getState() {
    if (myTokenType == null) locateToken();
    return myState;
  }

  @Override
  public void start(final CharSequence buffer, int startOffset, int endOffset, final int initialState) {
    reset(buffer, startOffset, endOffset, initialState);
    myTokenType = null;
  }

   @Override
   public CharSequence getBufferSequence() {
     return zzBuffer;
   }

   @Override
   public int getBufferEnd() {
     return zzEndRead;
   }

  public void reset(CharSequence buffer, int start, int end,int initialState){
    zzBuffer = buffer;
    zzCurrentPos = zzMarkedPos = zzStartRead = start;
    zzPushbackPos = 0;
    zzAtEOF  = false;
    zzAtBOL = true;
    zzEndRead = end;
    yybegin(initialState);
    myTokenType = null;
  }

   protected void locateToken() {
     if (myTokenType != null) return;
     try {
       myState = yystate();
       myTokenType = advanceImpl();
     }
     catch (java.io.IOException e) { /*Can't happen*/ }
     catch (Error e) {
       // add lexer class name to the error
       final Error error = new Error(getClass().getName() + ": " + e.getMessage());
       error.setStackTrace(e.getStackTrace());
       throw error;
     }
   }

   /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   *
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {
    return true;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final CharSequence yytext() {
    return zzBuffer.subSequence(zzStartRead, zzMarkedPos);
  }


  /**
   * Returns the character at position <tt>pos</tt> from the
   * matched text.
   *
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch.
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer.charAt(zzStartRead+pos);
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of
   * yypushback(int) and a match-all fallback rule) this method
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Contains user EOF-code, which will be executed exactly once,
   * when the end of file is reached
   */
  private void zzDoEOF() {
    if (!zzEOFDone) {
      zzEOFDone = true;
    
    }
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public IElementType advanceImpl() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    CharSequence zzBufferL = zzBuffer;
    char [] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

      zzState = ZZ_LEXSTATE[zzLexicalState];


      zzForAction: {
        while (true) {

          if (zzCurrentPosL < zzEndReadL)
            zzInput = zzBufferL.charAt(zzCurrentPosL++);
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = zzBufferL.charAt(zzCurrentPosL++);
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          int zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
        case 20: 
          { yybegin(MACRO_ENTERED); return CSharpPreprocesorTokens.WARNING_KEYWORD;
          }
        case 22: break;
        case 2: 
          { return CSharpPreprocesorTokens.WHITE_SPACE;
          }
        case 23: break;
        case 18: 
          { yybegin(MACRO_ENTERED); return CSharpPreprocesorTokens.MACRO_REGION_KEYWORD;
          }
        case 24: break;
        case 16: 
          { yybegin(MACRO_ENTERED); return CSharpPreprocesorTokens.MACRO_UNDEF_KEYWORD;
          }
        case 25: break;
        case 8: 
          { yybegin(MACRO_EXPRESSION); return CSharpPreprocesorTokens.ILLEGAL_KEYWORD;
          }
        case 26: break;
        case 13: 
          { yybegin(MACRO_ENTERED); return CSharpPreprocesorTokens.MACRO_ELSE_KEYWORD;
          }
        case 27: break;
        case 15: 
          { yybegin(MACRO_ENTERED); return CSharpPreprocesorTokens.ERROR_KEYWORD;
          }
        case 28: break;
        case 9: 
          { return CSharpPreprocesorTokens.ANDAND;
          }
        case 29: break;
        case 3: 
          { return CSharpPreprocesorTokens.IDENTIFIER;
          }
        case 30: break;
        case 19: 
          { yybegin(MACRO_ENTERED); return CSharpPreprocesorTokens.PRAGMA_KEYWORD;
          }
        case 31: break;
        case 21: 
          { yybegin(MACRO_ENTERED); return CSharpPreprocesorTokens.MACRO_ENDREGION_KEYWORD;
          }
        case 32: break;
        case 5: 
          { return CSharpPreprocesorTokens.RPAR;
          }
        case 33: break;
        case 7: 
          { return CSharpPreprocesorTokens.LINE_COMMENT;
          }
        case 34: break;
        case 17: 
          { yybegin(MACRO_ENTERED); return CSharpPreprocesorTokens.MACRO_DEFINE_KEYWORD;
          }
        case 35: break;
        case 1: 
          { return CSharpPreprocesorTokens.BAD_CHARACTER;
          }
        case 36: break;
        case 12: 
          { yybegin(MACRO_EXPRESSION); return CSharpPreprocesorTokens.MACRO_ELIF_KEYWORD;
          }
        case 37: break;
        case 14: 
          { yybegin(MACRO_ENTERED); return CSharpPreprocesorTokens.MACRO_ENDIF_KEYWORD;
          }
        case 38: break;
        case 10: 
          { return CSharpPreprocesorTokens.OROR;
          }
        case 39: break;
        case 6: 
          { return CSharpPreprocesorTokens.EXCL;
          }
        case 40: break;
        case 11: 
          { yybegin(MACRO_EXPRESSION); return CSharpPreprocesorTokens.MACRO_IF_KEYWORD;
          }
        case 41: break;
        case 4: 
          { return CSharpPreprocesorTokens.LPAR;
          }
        case 42: break;
        default:
          if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
            zzAtEOF = true;
            zzDoEOF();
            return null;
          }
          else {
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
