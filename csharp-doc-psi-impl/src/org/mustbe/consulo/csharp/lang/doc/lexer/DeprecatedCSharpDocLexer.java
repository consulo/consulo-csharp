package org.mustbe.consulo.csharp.lang.doc.lexer;

import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocTokenType;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.LookAheadLexer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 15.09.14
 *
 * TODO [VISTALL] merge with CSharpDocLexer
 */
public class DeprecatedCSharpDocLexer extends LookAheadLexer
{
	private boolean myNewLine = true;

	public DeprecatedCSharpDocLexer()
	{
		super(new CSharpDocLexer());
	}

	@Override
	protected void lookAhead(Lexer baseLexer)
	{
		IElementType tokenType = baseLexer.getTokenType();
		if(tokenType == CSharpDocTokenType.TAG_WHITE_SPACE || tokenType == CSharpDocTokenType.XML_REAL_WHITE_SPACE)
		{
			CharSequence tokenSequence = baseLexer.getTokenSequence();
			if(StringUtil.containsLineBreak(tokenSequence))
			{
				myNewLine = true;
				super.lookAhead(baseLexer);
				return;
			}
		}

		if(myNewLine)
		{
			if(tokenType == CSharpDocTokenType.XML_DATA_CHARACTERS)
			{
				CharSequence tokenSequence = baseLexer.getTokenSequence();
				if(StringUtil.equals(tokenSequence, "///"))
				{
					myNewLine = false;
					advanceAs(baseLexer, CSharpDocTokenType.DOC_LINE_START);
					return;
				}
			}
		}

		super.lookAhead(baseLexer);
	}
}
