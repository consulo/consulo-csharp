/* The following code was generated by JFlex 1.4.4 on 12/12/17 6:00 PM */

package consulo.csharp.lang.doc.lexer;

import java.util.*;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import consulo.csharp.lang.psi.CSharpTokens;


/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.4.4
 * on 12/12/17 6:00 PM from the specification file
 * <tt>W:/_github.com/consulo/consulo-csharp/csharp-doc-psi-impl/src/consulo/csharp/lang/doc/lexer/CSharpReferenceLexer.flex</tt>
 */
public class CSharpReferenceLexer extends LexerBase {
  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0, 0
  };

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = 
    "\11\13\1\2\1\4\1\0\1\2\1\4\16\13\4\0\1\2\1\53"+
    "\1\10\1\0\1\12\1\41\1\43\1\6\1\36\1\37\1\5\1\42"+
    "\1\54\1\30\1\26\1\3\1\20\11\1\1\50\1\51\1\46\1\40"+
    "\1\47\1\55\1\11\1\15\1\22\1\15\1\24\1\27\1\23\5\12"+
    "\1\16\1\25\2\12\1\31\4\12\1\17\2\12\1\21\2\12\1\34"+
    "\1\7\1\35\1\44\1\14\1\0\1\57\1\22\1\15\1\24\1\62"+
    "\1\56\5\12\1\60\1\25\1\66\1\12\1\31\1\12\1\64\1\61"+
    "\1\63\1\65\2\12\1\21\2\12\1\32\1\45\1\33\1\52\41\13"+
    "\2\0\4\12\4\0\1\12\2\0\1\13\7\0\1\12\4\0\1\12"+
    "\5\0\27\12\1\0\37\12\1\0\u01ca\12\4\0\14\12\16\0\5\12"+
    "\7\0\1\12\1\0\1\12\21\0\160\13\5\12\1\0\2\12\2\0"+
    "\4\12\10\0\1\12\1\0\3\12\1\0\1\12\1\0\24\12\1\0"+
    "\123\12\1\0\213\12\1\0\5\13\2\0\236\12\11\0\46\12\2\0"+
    "\1\12\7\0\47\12\7\0\1\12\1\0\55\13\1\0\1\13\1\0"+
    "\2\13\1\0\2\13\1\0\1\13\10\0\33\12\5\0\3\12\15\0"+
    "\5\13\6\0\1\12\4\0\13\13\5\0\53\12\37\13\4\0\2\12"+
    "\1\13\143\12\1\0\1\12\10\13\1\0\6\13\2\12\2\13\1\0"+
    "\4\13\2\12\12\13\3\12\2\0\1\12\17\0\1\13\1\12\1\13"+
    "\36\12\33\13\2\0\131\12\13\13\1\12\16\0\12\13\41\12\11\13"+
    "\2\12\4\0\1\12\5\0\26\12\4\13\1\12\11\13\1\12\3\13"+
    "\1\12\5\13\22\0\31\12\3\13\104\0\1\12\1\0\13\12\67\0"+
    "\33\13\1\0\4\13\66\12\3\13\1\12\22\13\1\12\7\13\12\12"+
    "\2\13\2\0\12\13\1\0\7\12\1\0\7\12\1\0\3\13\1\0"+
    "\10\12\2\0\2\12\2\0\26\12\1\0\7\12\1\0\1\12\3\0"+
    "\4\12\2\0\1\13\1\12\7\13\2\0\2\13\2\0\3\13\1\12"+
    "\10\0\1\13\4\0\2\12\1\0\3\12\2\13\2\0\12\13\4\12"+
    "\7\0\1\12\5\0\3\13\1\0\6\12\4\0\2\12\2\0\26\12"+
    "\1\0\7\12\1\0\2\12\1\0\2\12\1\0\2\12\2\0\1\13"+
    "\1\0\5\13\4\0\2\13\2\0\3\13\3\0\1\13\7\0\4\12"+
    "\1\0\1\12\7\0\14\13\3\12\1\13\13\0\3\13\1\0\11\12"+
    "\1\0\3\12\1\0\26\12\1\0\7\12\1\0\2\12\1\0\5\12"+
    "\2\0\1\13\1\12\10\13\1\0\3\13\1\0\3\13\2\0\1\12"+
    "\17\0\2\12\2\13\2\0\12\13\1\0\1\12\17\0\3\13\1\0"+
    "\10\12\2\0\2\12\2\0\26\12\1\0\7\12\1\0\2\12\1\0"+
    "\5\12\2\0\1\13\1\12\7\13\2\0\2\13\2\0\3\13\10\0"+
    "\2\13\4\0\2\12\1\0\3\12\2\13\2\0\12\13\1\0\1\12"+
    "\20\0\1\13\1\12\1\0\6\12\3\0\3\12\1\0\4\12\3\0"+
    "\2\12\1\0\1\12\1\0\2\12\3\0\2\12\3\0\3\12\3\0"+
    "\14\12\4\0\5\13\3\0\3\13\1\0\4\13\2\0\1\12\6\0"+
    "\1\13\16\0\12\13\11\0\1\12\7\0\3\13\1\0\10\12\1\0"+
    "\3\12\1\0\27\12\1\0\12\12\1\0\5\12\3\0\1\12\7\13"+
    "\1\0\3\13\1\0\4\13\7\0\2\13\1\0\2\12\6\0\2\12"+
    "\2\13\2\0\12\13\22\0\2\13\1\0\10\12\1\0\3\12\1\0"+
    "\27\12\1\0\12\12\1\0\5\12\2\0\1\13\1\12\7\13\1\0"+
    "\3\13\1\0\4\13\7\0\2\13\7\0\1\12\1\0\2\12\2\13"+
    "\2\0\12\13\1\0\2\12\17\0\2\13\1\0\10\12\1\0\3\12"+
    "\1\0\51\12\2\0\1\12\7\13\1\0\3\13\1\0\4\13\1\12"+
    "\10\0\1\13\10\0\2\12\2\13\2\0\12\13\12\0\6\12\2\0"+
    "\2\13\1\0\22\12\3\0\30\12\1\0\11\12\1\0\1\12\2\0"+
    "\7\12\3\0\1\13\4\0\6\13\1\0\1\13\1\0\10\13\22\0"+
    "\2\13\15\0\60\12\1\13\2\12\7\13\4\0\10\12\10\13\1\0"+
    "\12\13\47\0\2\12\1\0\1\12\2\0\2\12\1\0\1\12\2\0"+
    "\1\12\6\0\4\12\1\0\7\12\1\0\3\12\1\0\1\12\1\0"+
    "\1\12\2\0\2\12\1\0\4\12\1\13\2\12\6\13\1\0\2\13"+
    "\1\12\2\0\5\12\1\0\1\12\1\0\6\13\2\0\12\13\2\0"+
    "\4\12\40\0\1\12\27\0\2\13\6\0\12\13\13\0\1\13\1\0"+
    "\1\13\1\0\1\13\4\0\2\13\10\12\1\0\44\12\4\0\24\13"+
    "\1\0\2\13\5\12\13\13\1\0\44\13\11\0\1\13\71\0\53\12"+
    "\24\13\1\12\12\13\6\0\6\12\4\13\4\12\3\13\1\12\3\13"+
    "\2\12\7\13\3\12\4\13\15\12\14\13\1\12\17\13\2\0\46\12"+
    "\1\0\1\12\5\0\1\12\2\0\53\12\1\0\u014d\12\1\0\4\12"+
    "\2\0\7\12\1\0\1\12\1\0\4\12\2\0\51\12\1\0\4\12"+
    "\2\0\41\12\1\0\4\12\2\0\7\12\1\0\1\12\1\0\4\12"+
    "\2\0\17\12\1\0\71\12\1\0\4\12\2\0\103\12\2\0\3\13"+
    "\40\0\20\12\20\0\125\12\14\0\u026c\12\2\0\21\12\1\0\32\12"+
    "\5\0\113\12\3\0\3\12\17\0\15\12\1\0\4\12\3\13\13\0"+
    "\22\12\3\13\13\0\22\12\2\13\14\0\15\12\1\0\3\12\1\0"+
    "\2\13\14\0\64\12\40\13\3\0\1\12\3\0\2\12\1\13\2\0"+
    "\12\13\41\0\3\13\2\0\12\13\6\0\130\12\10\0\51\12\1\13"+
    "\1\12\5\0\106\12\12\0\35\12\3\0\14\13\4\0\14\13\12\0"+
    "\12\13\36\12\2\0\5\12\13\0\54\12\4\0\21\13\7\12\2\13"+
    "\6\0\12\13\46\0\27\12\5\13\4\0\65\12\12\13\1\0\35\13"+
    "\2\0\13\13\6\0\12\13\15\0\1\12\130\0\5\13\57\12\21\13"+
    "\7\12\4\0\12\13\21\0\11\13\14\0\3\13\36\12\15\13\2\12"+
    "\12\13\54\12\16\13\14\0\44\12\24\13\10\0\12\13\3\0\3\12"+
    "\12\13\44\12\122\0\3\13\1\0\25\13\4\12\1\13\4\12\3\13"+
    "\2\12\11\0\300\12\47\13\25\0\4\13\u0116\12\2\0\6\12\2\0"+
    "\46\12\2\0\6\12\2\0\10\12\1\0\1\12\1\0\1\12\1\0"+
    "\1\12\1\0\37\12\2\0\65\12\1\0\7\12\1\0\1\12\3\0"+
    "\3\12\1\0\7\12\3\0\4\12\2\0\6\12\4\0\15\12\5\0"+
    "\3\12\1\0\7\12\16\0\5\13\32\0\5\13\20\0\2\12\23\0"+
    "\1\12\13\0\5\13\5\0\6\13\1\0\1\12\15\0\1\12\20\0"+
    "\15\12\3\0\33\12\25\0\15\13\4\0\1\13\3\0\14\13\21\0"+
    "\1\12\4\0\1\12\2\0\12\12\1\0\1\12\3\0\5\12\6\0"+
    "\1\12\1\0\1\12\1\0\1\12\1\0\4\12\1\0\13\12\2\0"+
    "\4\12\5\0\5\12\4\0\1\12\21\0\51\12\u0a77\0\57\12\1\0"+
    "\57\12\1\0\205\12\6\0\4\12\3\13\2\12\14\0\46\12\1\0"+
    "\1\12\5\0\1\12\2\0\70\12\7\0\1\12\17\0\1\13\27\12"+
    "\11\0\7\12\1\0\7\12\1\0\7\12\1\0\7\12\1\0\7\12"+
    "\1\0\7\12\1\0\7\12\1\0\7\12\1\0\40\13\57\0\1\12"+
    "\u01d5\0\3\12\31\0\11\12\6\13\1\0\5\12\2\0\5\12\4\0"+
    "\126\12\2\0\2\13\2\0\3\12\1\0\132\12\1\0\4\12\5\0"+
    "\51\12\3\0\136\12\21\0\33\12\65\0\20\12\u0200\0\u19b6\12\112\0"+
    "\u51cd\12\63\0\u048d\12\103\0\56\12\2\0\u010d\12\3\0\20\12\12\13"+
    "\2\12\24\0\57\12\1\13\4\0\12\13\1\0\31\12\7\0\1\13"+
    "\120\12\2\13\45\0\11\12\2\0\147\12\2\0\4\12\1\0\4\12"+
    "\14\0\13\12\115\0\12\12\1\13\3\12\1\13\4\12\1\13\27\12"+
    "\5\13\20\0\1\12\7\0\64\12\14\0\2\13\62\12\21\13\13\0"+
    "\12\13\6\0\22\13\6\12\3\0\1\12\4\0\12\13\34\12\10\13"+
    "\2\0\27\12\15\13\14\0\35\12\3\0\4\13\57\12\16\13\16\0"+
    "\1\12\12\13\46\0\51\12\16\13\11\0\3\12\1\13\10\12\2\13"+
    "\2\0\12\13\6\0\27\12\3\0\1\12\1\13\4\0\60\12\1\13"+
    "\1\12\3\13\2\12\2\13\5\12\2\13\1\12\1\13\1\12\30\0"+
    "\3\12\2\0\13\12\5\13\2\0\3\12\2\13\12\0\6\12\2\0"+
    "\6\12\2\0\6\12\11\0\7\12\1\0\7\12\221\0\43\12\10\13"+
    "\1\0\2\13\2\0\12\13\6\0\u2ba4\12\14\0\27\12\4\0\61\12"+
    "\u2104\0\u016e\12\2\0\152\12\46\0\7\12\14\0\5\12\5\0\1\12"+
    "\1\13\12\12\1\0\15\12\1\0\5\12\1\0\1\12\1\0\2\12"+
    "\1\0\2\12\1\0\154\12\41\0\u016b\12\22\0\100\12\2\0\66\12"+
    "\50\0\15\12\3\0\20\13\20\0\7\13\14\0\2\12\30\0\3\12"+
    "\31\0\1\12\6\0\5\12\1\0\207\12\2\0\1\13\4\0\1\12"+
    "\13\0\12\13\7\0\32\12\4\0\1\12\1\0\32\12\13\0\131\12"+
    "\3\0\6\12\2\0\6\12\2\0\6\12\2\0\3\12\3\0\2\12"+
    "\3\0\2\12\22\0\3\13\4\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7"+
    "\1\1\1\10\1\2\1\11\1\12\1\13\1\14\1\15"+
    "\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25"+
    "\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35"+
    "\1\36\3\10\1\37\1\40\1\41\1\42\1\43\2\42"+
    "\1\44\1\45\2\6\2\7\2\2\1\46\1\47\1\50"+
    "\1\51\1\52\1\53\1\54\1\55\1\56\1\57\1\60"+
    "\1\61\1\62\1\63\1\0\1\64\1\0\1\65\1\66"+
    "\1\67\1\70\3\10\1\71\1\42\1\2\1\0\1\72"+
    "\1\73\3\10\1\0\1\10\1\74\1\75\1\76";

  private static int [] zzUnpackAction() {
    int [] result = new int[89];
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
    "\0\0\0\67\0\156\0\245\0\334\0\u0113\0\u014a\0\u0181"+
    "\0\u01b8\0\u01ef\0\u0226\0\u025d\0\u0294\0\67\0\67\0\67"+
    "\0\67\0\67\0\67\0\u02cb\0\u0302\0\u0339\0\u0370\0\u03a7"+
    "\0\u03de\0\u0415\0\u044c\0\u0483\0\67\0\67\0\u04ba\0\67"+
    "\0\u04f1\0\u0528\0\u055f\0\u0596\0\u05cd\0\u0604\0\67\0\67"+
    "\0\67\0\u063b\0\u0672\0\67\0\67\0\67\0\u06a9\0\u06e0"+
    "\0\67\0\u0717\0\u074e\0\67\0\67\0\67\0\67\0\67"+
    "\0\67\0\67\0\67\0\67\0\67\0\67\0\67\0\67"+
    "\0\67\0\u0785\0\67\0\u07bc\0\67\0\67\0\67\0\67"+
    "\0\u07f3\0\u082a\0\u0861\0\67\0\u0898\0\u08cf\0\u0906\0\67"+
    "\0\67\0\u093d\0\u0974\0\u09ab\0\u09e2\0\u0a19\0\u01ef\0\u01ef"+
    "\0\u01ef";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[89];
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
    "\1\2\1\3\1\4\1\5\1\4\1\6\1\7\1\2"+
    "\1\10\1\11\1\12\1\2\4\12\1\13\5\12\1\14"+
    "\1\12\1\15\1\12\1\16\1\17\1\20\1\21\1\22"+
    "\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32"+
    "\1\33\1\34\1\35\1\36\1\37\1\40\1\41\1\42"+
    "\4\12\1\43\2\12\1\44\70\0\1\3\12\0\1\3"+
    "\1\0\1\45\1\46\1\3\2\0\1\47\1\50\1\51"+
    "\1\52\1\53\26\0\1\47\1\0\1\45\1\0\1\53"+
    "\2\0\1\46\3\0\1\4\1\0\1\4\122\0\1\54"+
    "\66\0\1\55\26\0\4\7\1\0\1\7\1\56\1\57"+
    "\57\7\4\10\1\0\2\10\1\60\1\61\56\10\12\0"+
    "\1\12\1\0\4\12\1\0\5\12\1\0\1\12\1\0"+
    "\1\12\24\0\11\12\1\0\1\12\10\0\14\12\1\0"+
    "\1\12\1\0\1\12\24\0\11\12\1\0\1\3\12\0"+
    "\1\3\1\0\1\45\1\46\1\3\1\62\1\63\1\47"+
    "\1\50\1\51\1\52\1\53\26\0\1\47\1\0\1\45"+
    "\1\0\1\53\2\0\1\46\2\0\1\52\16\0\1\52"+
    "\76\0\1\64\7\0\1\65\6\0\1\66\57\0\1\67"+
    "\6\0\1\70\57\0\1\71\66\0\1\72\1\0\1\73"+
    "\64\0\1\74\2\0\1\75\63\0\1\76\66\0\1\77"+
    "\4\0\1\100\61\0\1\101\5\0\1\102\60\0\1\103"+
    "\6\0\1\104\67\0\1\105\56\0\1\106\54\0\1\107"+
    "\26\0\1\110\12\0\1\12\10\0\14\12\1\0\1\12"+
    "\1\0\1\12\24\0\1\12\1\111\7\12\1\0\1\12"+
    "\10\0\14\12\1\0\1\12\1\0\1\12\24\0\6\12"+
    "\1\112\2\12\1\0\1\12\10\0\14\12\1\0\1\12"+
    "\1\0\1\12\24\0\7\12\1\113\1\12\17\0\1\114"+
    "\45\0\1\114\17\0\1\114\41\0\1\114\7\0\1\52"+
    "\12\0\1\52\3\0\1\52\2\0\1\47\1\50\1\51"+
    "\1\0\1\53\26\0\1\47\3\0\1\53\5\0\1\115"+
    "\12\0\1\115\3\0\1\115\2\0\1\47\1\50\1\51"+
    "\2\0\1\115\11\0\1\115\13\0\1\47\10\0\4\7"+
    "\1\0\62\7\4\10\1\0\62\10\1\0\1\116\12\0"+
    "\2\116\1\45\1\46\1\116\1\0\3\116\1\0\1\117"+
    "\1\116\26\0\2\116\1\45\1\0\1\116\2\0\1\46"+
    "\2\0\1\63\12\0\1\63\1\0\1\45\1\46\1\63"+
    "\37\0\1\45\4\0\1\46\41\0\1\120\66\0\1\121"+
    "\27\0\1\12\10\0\14\12\1\0\1\12\1\0\1\12"+
    "\24\0\2\12\1\122\6\12\1\0\1\12\10\0\14\12"+
    "\1\0\1\12\1\0\1\12\24\0\7\12\1\123\1\12"+
    "\1\0\1\12\10\0\14\12\1\0\1\12\1\0\1\12"+
    "\24\0\2\12\1\124\6\12\1\0\1\115\12\0\1\115"+
    "\3\0\1\115\2\0\1\47\1\50\1\51\30\0\1\47"+
    "\11\0\1\116\12\0\2\116\1\45\1\46\1\116\1\0"+
    "\3\116\1\0\1\125\1\116\1\0\1\53\24\0\2\116"+
    "\1\45\1\0\1\116\2\0\1\46\2\0\1\125\12\0"+
    "\2\125\2\0\1\125\1\0\3\125\2\0\1\125\26\0"+
    "\2\125\2\0\1\125\5\0\1\12\10\0\14\12\1\0"+
    "\1\12\1\0\1\12\24\0\3\12\1\126\5\12\1\0"+
    "\1\12\10\0\14\12\1\0\1\12\1\0\1\12\24\0"+
    "\4\12\1\127\4\12\1\0\1\12\10\0\14\12\1\0"+
    "\1\12\1\0\1\12\24\0\2\12\1\130\6\12\1\0"+
    "\1\125\12\0\2\125\2\0\1\125\1\0\3\125\2\0"+
    "\1\125\1\0\1\53\24\0\2\125\2\0\1\125\5\0"+
    "\1\12\10\0\14\12\1\0\1\12\1\0\1\12\24\0"+
    "\4\12\1\131\4\12";

  private static int [] zzUnpackTrans() {
    int [] result = new int[2640];
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
    "\1\0\1\11\13\1\6\11\11\1\2\11\1\1\1\11"+
    "\6\1\3\11\2\1\3\11\2\1\1\11\2\1\16\11"+
    "\1\0\1\11\1\0\4\11\3\1\1\11\2\1\1\0"+
    "\2\11\3\1\1\0\4\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[89];
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
    while (i < 2270) {
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
        case 53: 
          { return CSharpTokens.COLONCOLON;
          }
        case 63: break;
        case 61: 
          { return CSharpTokens.NULL_LITERAL;
          }
        case 64: break;
        case 14: 
          { return CSharpTokens.RBRACKET;
          }
        case 65: break;
        case 21: 
          { return CSharpTokens.XOR;
          }
        case 66: break;
        case 43: 
          { return CSharpTokens.PERCEQ;
          }
        case 67: break;
        case 60: 
          { return CSharpTokens.TRUE_KEYWORD;
          }
        case 68: break;
        case 30: 
          { return CSharpTokens.QUEST;
          }
        case 69: break;
        case 24: 
          { return CSharpTokens.GT;
          }
        case 70: break;
        case 10: 
          { return CSharpTokens.MINUS;
          }
        case 71: break;
        case 34: 
          { return CSharpTokens.DOUBLE_LITERAL;
          }
        case 72: break;
        case 35: 
          { return CSharpTokens.DECIMAL_LITERAL;
          }
        case 73: break;
        case 9: 
          { return CSharpTokens.DOT;
          }
        case 74: break;
        case 4: 
          { return CSharpTokens.DIV;
          }
        case 75: break;
        case 49: 
          { return CSharpTokens.OREQ;
          }
        case 76: break;
        case 36: 
          { return CSharpTokens.DIVEQ;
          }
        case 77: break;
        case 31: 
          { return CSharpTokens.LONG_LITERAL;
          }
        case 78: break;
        case 52: 
          { return CSharpTokens.GTEQ;
          }
        case 79: break;
        case 48: 
          { return CSharpTokens.XOREQ;
          }
        case 80: break;
        case 20: 
          { return CSharpTokens.AND;
          }
        case 81: break;
        case 17: 
          { return CSharpTokens.EQ;
          }
        case 82: break;
        case 28: 
          { return CSharpTokens.EXCL;
          }
        case 83: break;
        case 39: 
          { return CSharpTokens.MINUSEQ;
          }
        case 84: break;
        case 57: 
          { return CSharpTokens.ULONG_LITERAL;
          }
        case 85: break;
        case 54: 
          { return CSharpTokens.NTEQ;
          }
        case 86: break;
        case 26: 
          { return CSharpTokens.SEMICOLON;
          }
        case 87: break;
        case 15: 
          { return CSharpTokens.LPAR;
          }
        case 88: break;
        case 33: 
          { return CSharpTokens.FLOAT_LITERAL;
          }
        case 89: break;
        case 58: 
          { return CSharpTokens.LTLTEQ;
          }
        case 90: break;
        case 27: 
          { return CSharpTokens.TILDE;
          }
        case 91: break;
        case 55: 
          { return CSharpTokens.NULLABE_CALL;
          }
        case 92: break;
        case 44: 
          { return CSharpTokens.PLUSEQ;
          }
        case 93: break;
        case 40: 
          { return CSharpTokens.ARROW;
          }
        case 94: break;
        case 13: 
          { return CSharpTokens.LBRACKET;
          }
        case 95: break;
        case 8: 
          { return CSharpTokens.IDENTIFIER;
          }
        case 96: break;
        case 32: 
          { return CSharpTokens.UINTEGER_LITERAL;
          }
        case 97: break;
        case 37: 
          { return CSharpTokens.MULEQ;
          }
        case 98: break;
        case 23: 
          { return CSharpTokens.LT;
          }
        case 99: break;
        case 41: 
          { return CSharpTokens.EQEQ;
          }
        case 100: break;
        case 45: 
          { return CSharpTokens.PLUSPLUS;
          }
        case 101: break;
        case 6: 
          { return CSharpTokens.CHARACTER_LITERAL;
          }
        case 102: break;
        case 3: 
          { return CSharpTokens.WHITE_SPACE;
          }
        case 103: break;
        case 29: 
          { return CSharpTokens.COMMA;
          }
        case 104: break;
        case 18: 
          { return CSharpTokens.PERC;
          }
        case 105: break;
        case 16: 
          { return CSharpTokens.RPAR;
          }
        case 106: break;
        case 50: 
          { return CSharpTokens.OROR;
          }
        case 107: break;
        case 38: 
          { return CSharpTokens.MINUSMINUS;
          }
        case 108: break;
        case 59: 
          { return CSharpTokens.GTGTEQ;
          }
        case 109: break;
        case 2: 
          { return CSharpTokens.INTEGER_LITERAL;
          }
        case 110: break;
        case 5: 
          { return CSharpTokens.MUL;
          }
        case 111: break;
        case 62: 
          { return CSharpTokens.FALSE_KEYWORD;
          }
        case 112: break;
        case 25: 
          { return CSharpTokens.COLON;
          }
        case 113: break;
        case 22: 
          { return CSharpTokens.OR;
          }
        case 114: break;
        case 11: 
          { return CSharpTokens.LBRACE;
          }
        case 115: break;
        case 12: 
          { return CSharpTokens.RBRACE;
          }
        case 116: break;
        case 47: 
          { return CSharpTokens.ANDAND;
          }
        case 117: break;
        case 51: 
          { return CSharpTokens.LTEQ;
          }
        case 118: break;
        case 7: 
          { return CSharpTokens.STRING_LITERAL;
          }
        case 119: break;
        case 1: 
          { return CSharpTokens.BAD_CHARACTER;
          }
        case 120: break;
        case 46: 
          { return CSharpTokens.ANDEQ;
          }
        case 121: break;
        case 56: 
          { return CSharpTokens.QUESTQUEST;
          }
        case 122: break;
        case 42: 
          { return CSharpTokens.DARROW;
          }
        case 123: break;
        case 19: 
          { return CSharpTokens.PLUS;
          }
        case 124: break;
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